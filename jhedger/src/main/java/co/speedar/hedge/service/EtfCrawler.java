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
public class EtfCrawler {
	protected static final Logger log = Logger.getLogger(EtfCrawler.class);
	protected static final String hostPath = "http://www.jisilu.cn/jisiludata/etf.php";
	protected static final int volumnFence = 500;
	protected static final float discountRateFence = -3;
	protected static final float increaseRateFence = 9;

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
			String json = getEtfJson(fireDate);
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
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject j = jsonArray.getJSONObject(i);
			JSONObject cell = j.getJSONObject("cell");
			String fundId = cell.getString("fund_id");
			String fundName = cell.getString("fund_nm");
			float price = Float.valueOf(cell.getString("price"));
			float volume = Float.valueOf(cell.getString("volume"));
			if (volume < volumnFence) {
				continue;
			}
			float increaseRate = Float.valueOf(StringUtils.removeEnd(cell.getString("increase_rt"), "%"));
			float discountRate = Float.valueOf(StringUtils.removeEnd(cell.getString("discount_rt"), "%"));
			String indexId = cell.getString("index_id");
			String indexName = cell.getString("index_nm");
			float indexIncreaseRate = StringUtils.length(cell.getString("index_increase_rt")) > 1
					? Float.valueOf(StringUtils.removeEnd(cell.getString("index_increase_rt"), "%")) : 0;
			if (discountRate < discountRateFence && increaseRate < increaseRateFence) {
				sb.append(String.format("%s, %s, %.2f%%, %.2f%%, %.3f\n%s, %s, %.2f%%;\n\n", fundId, fundName,
						increaseRate, discountRate, price, indexId, indexName, indexIncreaseRate));
			}
		}
		if (StringUtils.isNotBlank(sb.toString())) {
			log.info(sb.toString());
			final String title = "etf lower flow!" + CrawlerHelper.sdf.format(new Date());
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
		headerMap.put("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:37.0) Gecko/20100101 Firefox/37.0");
		headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
		headerMap.put("Accept-Language", "en-US,en;q=0.5");
		headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		headerMap.put("Cache-Control", "no-cache");
		headerMap.put("Pragma", "no-cache");
		String json = HttpClientUtil.getStringFromHost(hostPath, paramMap, headerMap, "utf-8");
		return json;
	}
}
