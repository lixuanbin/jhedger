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

import co.speedar.hedge.dao.EtfDao;
import co.speedar.hedge.util.CrawlerHelper;
import co.speedar.hedge.util.HttpClientUtil;
import co.speedar.hedge.util.SwingUtil;

/**
 * @author lixuanbin
 *
 */
@Service
public class EtfCrawler {
	protected static final Logger log = Logger.getLogger(EtfCrawler.class);
	// These params should be configurable, tune them yourself!
	/**
	 * 成交量阀值，>=
	 */
	protected static final int volumnFence = 300;

	/**
	 * 溢价率阀值，<=
	 */
	protected static final float discountRateFence = -2;

	/**
	 * 涨幅阀值，<=
	 */
	protected static final float increaseRateFence = 9;

	/**
	 * 是否提醒过
	 */
	private Set<String> hasNotifiedSet = new ConcurrentSkipListSet<>();

	/**
	 * 上个交易日成交量大于500万的基金id列表
	 */
	private List<String> lastTradeOver5MFunds;

	@PostConstruct
	public void init() {
		try {
			lastTradeOver5MFunds = dao.queryLastTradeVolumeOver5M();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	@Scheduled(cron = "1 1 9 * * MON-FRI")
	public void lastDay() {
		try {
			lastTradeOver5MFunds = dao.queryLastTradeVolumeOver5M();
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	@Autowired
	private EtfDao dao;

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

	@Scheduled(cron = "12 28 9 * * MON-FRI")
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
			String json = getEtfJson(fireDate);
			log.info(json);
			checkAndNotify(json);
			List<Map<String, Object>> etfList = CrawlerHelper.buildEtfListFromJson(fireDate, json);
			dao.batchInsertEtfDetail(etfList);
		}
	}

	/**
	 * @param json
	 */
	public void checkAndNotify(String json) {
		JSONObject jsonObject = new JSONObject(json);
		JSONArray jsonArray = jsonObject.getJSONArray("rows");
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject j = jsonArray.getJSONObject(i);
			JSONObject cell = j.getJSONObject("cell");
			String fundId = cell.getString("fund_id");
			String fundName = cell.getString("fund_nm");
			float price = Float.valueOf(cell.getString("price"));
			float volume = Float.valueOf(cell.getString("volume"));
			if ((volume > volumnFence || (lastTradeOver5MFunds != null
					&& !lastTradeOver5MFunds.isEmpty() && lastTradeOver5MFunds.contains(fundId)))
					&& !hasNotifiedSet.contains(fundId)) {
				float increaseRate = StringUtils.isNumeric(StringUtils.removeEnd(
						cell.getString("increase_rt"), "%")) ? Float.valueOf(StringUtils.removeEnd(
						cell.getString("increase_rt"), "%")) : 0;
				float discountRate = StringUtils.isNumeric(StringUtils.removeEnd(
						cell.getString("discount_rt"), "%")) ? Float.valueOf(StringUtils.removeEnd(
						cell.getString("discount_rt"), "%")) : 0;
				String indexId = cell.getString("index_id");
				String indexName = cell.getString("index_nm");
				float indexIncreaseRate = StringUtils.length(cell.getString("index_increase_rt")) > 1 ? Float
						.valueOf(StringUtils.removeEnd(cell.getString("index_increase_rt"), "%"))
						: 0;
				if (discountRate < discountRateFence && increaseRate < increaseRateFence) {
					hasNotifiedSet.add(fundId);
					sb.append(String.format(
							"%s, %s, 涨幅：%.2f%%, 溢价率：%.2f%%, 现价：%.3f\n%s, %s, 涨幅：%.2f%%;\n\n",
							fundId, fundName, increaseRate, discountRate, price, indexId,
							indexName, indexIncreaseRate));
				}
			}
		}
		if (StringUtils.isNotBlank(sb.toString())) {
			log.info(sb.toString());
			final String title = "etf lower flow! " + CrawlerHelper.sdf.format(new Date());
			final String content = sb.toString();
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
	public String getEtfJson(Date fireDate) {
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("___t", String.valueOf(fireDate.getTime()));
		Map<String, String> headerMap = new HashMap<>();
		headerMap
				.put("User-Agent",
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:37.0) Gecko/20100101 Firefox/37.0");
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap.put("Accept-Language", "en-US,en;q=0.5");
		headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		headerMap.put("Cache-Control", "no-cache");
		headerMap.put("Pragma", "no-cache");
		// Find yourself a better source.
		String hostPath = "http://www.jisilu.cn/jisiludata/etf.php";
		String json = HttpClientUtil.getStringFromHost(hostPath, paramMap, headerMap, "utf-8");
		return json;
	}
}
