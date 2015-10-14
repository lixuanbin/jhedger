/**
 * 
 */
package co.speedar.hedge.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import co.speedar.hedge.dao.LofDao;
import co.speedar.hedge.util.CrawlerHelper;
import co.speedar.hedge.util.HttpClientUtil;
import co.speedar.hedge.util.SwingUtil;

/**
 * @author ben
 *
 */
@Service
public class LofCrawler {
	protected static final Logger log = Logger.getLogger(LofCrawler.class);
	// These params should be configurable, tune them yourself!
	protected static final int volumnFence = 600;
	protected static final float fundbDiscountRateFence = 15;
	protected static final float lowerDiscountRate = -2;
	protected static final float upperDiscountRate = 3;
	protected static final float fundbIncreaseRateFence = 9;
	protected static final float fundbLowerRecalcRateFence = 10;
	protected static final float lastBaseDiscountRateFence = 1.5f;
	private Set<String> hasNotifiedSet = new ConcurrentSkipListSet<>();
	private List<String> lastTradeOver10MFunds;

	@PostConstruct
	public void init() {
		try {
			lastTradeOver10MFunds = dao.queryLastTradeVolumeOver10M();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Autowired
	private LofDao dao;

	@Scheduled(cron = "1 1/2 9-11,13-14 * * MON-FRI")
	public void execute() {
		Date fireDate = new Date();
		try {
			craw(fireDate);
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	@Scheduled(cron = "1 3 15 * * MON-FRI")
	public void lastShot() {
		Date fireDate = new Date();
		try {
			craw(fireDate);
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	@Scheduled(cron = "11 28 9 * * MON-FRI")
	public void clearNotifiedSet() {
		try {
			hasNotifiedSet.clear();
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	/**
	 * @param fireDate
	 */
	public void craw(Date fireDate) {
		if (CrawlerHelper.isNotOpen(fireDate)) {
			return;
		} else {
			String json = getLofJson(fireDate);
			log.info(json);
			checkAndNotify(json, fireDate);
			List<Map<String, Object>> lofList = CrawlerHelper.buildLofListFromJson(fireDate, json);
			dao.batchInsertLofDetail(lofList);
		}
	}

	/**
	 * @param json
	 */
	public void checkAndNotify(String json, Date fireDate) {
		JSONObject jsonObject = new JSONObject(json);
		JSONArray jsonArray = jsonObject.getJSONArray("rows");
		final StringBuffer lowerSb = new StringBuffer();
		final StringBuffer upperSb = new StringBuffer();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject j = jsonArray.getJSONObject(i);
			JSONObject cell = j.getJSONObject("cell");
			String fundbId = cell.getString("fundb_id");
			String fundbName = cell.getString("fundb_name");
			float currentPrice = Float.valueOf(cell.getString("fundb_current_price"));
			float fundbVolumn = Float.valueOf(cell.getString("fundb_volume"));
			float fundbDiscountRate = Float.valueOf(StringUtils.removeEnd(
					cell.getString("fundb_discount_rt"), "%"));
			if ((fundbVolumn > volumnFence || (lastTradeOver10MFunds != null
					&& !lastTradeOver10MFunds.isEmpty() && lastTradeOver10MFunds.contains(fundbId)))
					&& !hasNotifiedSet.contains(fundbId)) {
				float fundbIncreaseRate = Float.valueOf(StringUtils.removeEnd(
						cell.getString("fundb_increase_rt"), "%"));
				float fundbLowerRecalcRate = StringUtils.length(cell
						.getString("fundb_lower_recalc_rt")) > 1 ? Float.valueOf(StringUtils
						.removeEnd(cell.getString("fundb_lower_recalc_rt"), "%")) : 100;
				String indexId = cell.getString("fundb_index_id");
				String indexName = cell.getString("fundb_index_name");
				float indexIncreaseRate = Float.valueOf(StringUtils.removeEnd(
						cell.getString("fundb_index_increase_rt"), "%"));
				float baseDiscountRate = Float.valueOf(StringUtils.removeEnd(
						cell.getString("fundb_base_est_dis_rt"), "%"));
				if (baseDiscountRate < lowerDiscountRate
						&& fundbIncreaseRate < fundbIncreaseRateFence
						&& fundbLowerRecalcRate > fundbLowerRecalcRateFence) {
					hasNotifiedSet.add(fundbId);
					lowerSb.append(String
							.format("%s, %s, 涨幅：%.2f%%, 溢价率：%.2f%%, 母基溢价率：%.2f%%, 现价：%.3f\n\t%s, %s, 涨幅：%.2f%%\n\n",
									fundbId, fundbName, fundbIncreaseRate, fundbDiscountRate,
									baseDiscountRate, currentPrice, indexId, indexName,
									indexIncreaseRate));
				}
				float lastBaseDiscountRate = dao.queryLastTradeBaseDiscountRate(fundbId);
				if (baseDiscountRate > upperDiscountRate
						&& lastBaseDiscountRate < lastBaseDiscountRateFence
						&& fundbLowerRecalcRate > fundbLowerRecalcRateFence
						&& fireDate.after(CrawlerHelper.setTimeOfDate(fireDate, 14, 30, 0))) {
					// 两点半后才考虑做溢价
					hasNotifiedSet.add(fundbId);
					upperSb.append(String
							.format("%s, %s, 涨幅：%.2f%%, 溢价率：%.2f%%, 母基溢价率：%.2f%%, 现价：%.3f\n\t%s, %s, 涨幅：%.2f%%\n\n",
									fundbId, fundbName, fundbIncreaseRate, fundbDiscountRate,
									baseDiscountRate, currentPrice, indexId, indexName,
									indexIncreaseRate));
				}
			}
		}
		if (StringUtils.isNotBlank(lowerSb.toString())) {
			log.info(lowerSb.toString());
			final String title = "Fundb lower flow! " + CrawlerHelper.sdf.format(new Date());
			final String content = lowerSb.toString();
			Thread thread = new Thread(new Runnable() {
				public void run() {
					SwingUtil.showDialog(title, content, 500, 300);
				}
			});
			thread.start();
		}
		if (StringUtils.isNotBlank(upperSb.toString())) {
			log.info(upperSb.toString());
			final String title = "Fundb upper flow! " + CrawlerHelper.sdf.format(new Date());
			final String content = upperSb.toString();
			Thread thread = new Thread(new Runnable() {
				public void run() {
					SwingUtil.showDialog(title, content, 500, 300);
				}
			});
			thread.start();
		}
	}

	/**
	 * @return
	 */
	public String getLofJson(Date fireDate) {
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("___t", String.valueOf(fireDate.getTime()));
		Map<String, String> headerMap = new HashMap<>();
		headerMap
				.put("User-Agent",
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:37.0) Gecko/20100101 Firefox/37.0");
		headerMap.put("Accept", "*/*");
		headerMap.put("Cache-Control", "no-cache");
		headerMap.put("Pragma", "no-cache");
		// Find yourself a better source.
		String hostPath = "http://www.jisilu.cn/data/sfnew/fundb_list/";
		String json = HttpClientUtil.getStringFromHost(hostPath, paramMap, headerMap, "utf-8");
		return json;
	}
}
