/**
 * 
 */
package co.speedar.hedge.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
	protected static final int volumnFence = 500;
	protected static final float fundbDiscountRateFence = 15;
	protected static final float lowerDiscountRate = -2.5f;
	protected static final float upperDiscountRate = 5;
	protected static final float fundbIncreaseRateFence = 9;
	protected static final float fundbLowerRecalcRateFence = 10;

	@Scheduled(cron = "1 1/3 9-11,13-14 * * MON-FRI")
	public void execute() {
		Date fireDate = new Date();
		try {
			craw(fireDate);
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
			checkAndNotify(json);
		}
	}

	/**
	 * @param json
	 */
	public void checkAndNotify(String json) {
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
			float fundbDiscountRate = Float.valueOf(StringUtils.removeEnd(cell.getString("fundb_discount_rt"), "%"));
			if (fundbVolumn < volumnFence || fundbDiscountRate > fundbDiscountRateFence) {
				// 成交量太小或者B端溢价率太高直接pass
				continue;
			}
			float fundbIncreaseRate = Float.valueOf(StringUtils.removeEnd(cell.getString("fundb_increase_rt"), "%"));
			float fundbLowerRecalcRate = StringUtils.length(cell.getString("fundb_lower_recalc_rt")) > 1
					? Float.valueOf(StringUtils.removeEnd(cell.getString("fundb_lower_recalc_rt"), "%")) : 100;
			String indexId = cell.getString("fundb_index_id");
			String indexName = cell.getString("fundb_index_name");
			float indexIncreaseRate = Float
					.valueOf(StringUtils.removeEnd(cell.getString("fundb_index_increase_rt"), "%"));
			float baseDiscountRate = Float.valueOf(StringUtils.removeEnd(cell.getString("fundb_base_est_dis_rt"), "%"));
			if (baseDiscountRate < lowerDiscountRate && fundbIncreaseRate < fundbIncreaseRateFence
					&& fundbLowerRecalcRate > fundbLowerRecalcRateFence) {
				lowerSb.append(String.format("%s, %s, %.2f%%, %.2f%%, %.2f%%, %.3f\n\t%s, %s, %.2f%%\n\n", fundbId,
						fundbName, fundbIncreaseRate, fundbDiscountRate, baseDiscountRate, currentPrice, indexId,
						indexName, indexIncreaseRate));
			}
			if (baseDiscountRate > upperDiscountRate && fundbLowerRecalcRate > fundbLowerRecalcRateFence) {
				upperSb.append(String.format("%s, %s, %.2f%%, %.2f%%, %.2f%%, %.3f\n\t%s, %s, %.2f%%\n\n", fundbId,
						fundbName, fundbIncreaseRate, fundbDiscountRate, baseDiscountRate, currentPrice, indexId,
						indexName, indexIncreaseRate));
			}
		}
		if (StringUtils.isNotBlank(lowerSb.toString())) {
			log.info(lowerSb.toString());
			final String title = "Fundb lower flow!" + CrawlerHelper.sdf.format(new Date());
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
			final String title = "Fundb upper flow!" + CrawlerHelper.sdf.format(new Date());
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
		headerMap.put("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:37.0) Gecko/20100101 Firefox/37.0");
		headerMap.put("Accept", "*/*");
		headerMap.put("Cache-Control", "no-cache");
		headerMap.put("Pragma", "no-cache");
		String hostPath = "http://www.jisilu.cn/data/sfnew/fundb_list/";
		String json = HttpClientUtil.getStringFromHost(hostPath, paramMap, headerMap, "utf-8");
		return json;
	}
}
