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
import org.apache.commons.lang.time.DateFormatUtils;
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
	/**
	 * 当日成交量阀值，>=
	 */
	protected static final int volumnFence = 600;

	/**
	 * B端溢价率阀值，<=
	 */
	protected static final float fundbDiscountRateFence = 30;

	/**
	 * 做折价时候的整体溢价率阀值，<=
	 */
	protected static final float lowerDiscountRate = -1f;

	/**
	 * 做溢价时的整体溢价率阀值，>=
	 */
	protected static final float upperDiscountRate = 1.5f;

	/**
	 * B端涨幅阀值，<=
	 */
	protected static final float fundbIncreaseRateFence = 9;

	/**
	 * B端下折距离阀值，>=
	 */
	protected static final float fundbLowerRecalcRateFence = 10;

	/**
	 * 上两个交易日的母基整体溢价率阀值，<=
	 */
	protected static final float lastBaseDiscountRateFence = 1.5f;

	/**
	 * 是否已经提醒过
	 */
	private Set<String> hasNotifiedSet = new ConcurrentSkipListSet<>();

	/**
	 * 上个交易日B端成交量过5百万的id列表
	 */
	private List<String> lastTradeOver5MFunds;

	// fenji detail url:
	// https://www.jisilu.cn/jisiludata/StockFenJiDetail.php?qtype=hist&display=table&fund_id=160718&___t=1480481613332
	public static final String fundaHostPath = "https://www.jisilu.cn/data/sfnew/funda_list/";
	public static final String fundbHostPath = "https://www.jisilu.cn/data/sfnew/fundb_list/";
	public static final String fundmHostPath = "https://www.jisilu.cn/data/sfnew/fundm_list/";

	private volatile boolean isTradeDateInit;
	private volatile boolean isTradeDate;

	@Autowired
	private LofDao dao;

	@PostConstruct
	public void init() {
		try {
			isTradeDateInit = false;
			isTradeDate = false;
			lastTradeOver5MFunds = null;
			lastTradeOver5MFunds = dao.queryLastTradeVolumeOver5M();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Scheduled(cron = "1 2/2 9-11,13-14 * * MON-FRI")
	public void execute() {
		try {
			Date fireDate = new Date();
			if (!isTradeDateInit) {
				isTradeDate = CrawlerHelper.isTradeDate(fireDate);
				isTradeDateInit = true;
			}
			if (isTradeDate) {
				craw(fireDate);
			}
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

	@Scheduled(cron = "1 1 9 * * MON-FRI")
	public void lastDay() {
		init();
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
			String json = getLofJson(fundbHostPath, fireDate);
			log.info(json);
			checkAndNotify(json, fireDate);
			String fireDay = DateFormatUtils.format(fireDate, CrawlerHelper.dateTimeFormatPattern);
			List<Map<String, Object>> lofList = CrawlerHelper.buildFundbListFromJson(fireDate, json);
			dao.batchInsertFundbDetail(lofList, fireDay);
			json = getLofJson(fundaHostPath, fireDate);
			log.info(json);
			lofList = CrawlerHelper.buildFundaListFromJson(fireDate, json);
			dao.batchInsertFundaDetail(lofList, fireDay);
			json = getLofJson(fundmHostPath, fireDate);
			log.info(json);
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
			float fundbDiscountRate = Float.valueOf(StringUtils.removeEnd(cell.getString("fundb_discount_rt"), "%"));
			if ((fundbVolumn > volumnFence || (lastTradeOver5MFunds != null && !lastTradeOver5MFunds.isEmpty()
					&& lastTradeOver5MFunds.contains(fundbId))) && !hasNotifiedSet.contains(fundbId)) {
				float fundbIncreaseRate = StringUtils
						.isNumeric(StringUtils.removeEnd(cell.getString("fundb_increase_rt"), "%"))
								? Float.valueOf(StringUtils.removeEnd(cell.getString("fundb_increase_rt"), "%")) : 0;
				float fundbLowerRecalcRate = StringUtils.length(cell.getString("fundb_lower_recalc_rt")) > 1
						? Float.valueOf(StringUtils.removeEnd(cell.getString("fundb_lower_recalc_rt"), "%")) : 100;
				String indexId = cell.getString("fundb_index_id");
				String indexName = cell.getString("fundb_index_name");
				float indexIncreaseRate = StringUtils
						.isNumeric(StringUtils.removeEnd(cell.getString("fundb_index_increase_rt"), "%"))
								? Float.valueOf(StringUtils.removeEnd(cell.getString("fundb_index_increase_rt"), "%"))
								: 0;
				float baseDiscountRate = StringUtils
						.isNumeric(StringUtils.removeEnd(cell.getString("fundb_base_est_dis_rt"), "%"))
								? Float.valueOf(StringUtils.removeEnd(cell.getString("fundb_base_est_dis_rt"), "%"))
								: 0;
				if (baseDiscountRate < lowerDiscountRate && fundbIncreaseRate < fundbIncreaseRateFence
						&& fundbLowerRecalcRate > fundbLowerRecalcRateFence) {
					hasNotifiedSet.add(fundbId);
					lowerSb.append(String.format(
							"%s, %s, 涨幅：%.2f%%, 溢价率：%.2f%%, 母基溢价率：%.2f%%, 现价：%.3f\n\t%s, %s, 涨幅：%.2f%%\n\n", fundbId,
							fundbName, fundbIncreaseRate, fundbDiscountRate, baseDiscountRate, currentPrice, indexId,
							indexName, indexIncreaseRate));
				}
				float lastBaseDiscountRate = dao.queryLastTradeBaseDiscountRate(fundbId);
				float lastLastBaseDiscountRate = dao.queryLastLastTradeBaseDiscountRate(fundbId);
				if (baseDiscountRate > upperDiscountRate && lastBaseDiscountRate < lastBaseDiscountRateFence
						&& lastLastBaseDiscountRate < lastBaseDiscountRateFence
						&& fundbLowerRecalcRate > fundbLowerRecalcRateFence
						&& fireDate.after(CrawlerHelper.setTimeOfDate(fireDate, 14, 30, 0))) {
					hasNotifiedSet.add(fundbId);
					upperSb.append(String.format(
							"%s, %s, 涨幅：%.2f%%, 溢价率：%.2f%%, 母基溢价率：%.2f%%, 现价：%.3f\n\t%s, %s, 涨幅：%.2f%%\n\n", fundbId,
							fundbName, fundbIncreaseRate, fundbDiscountRate, baseDiscountRate, currentPrice, indexId,
							indexName, indexIncreaseRate));
				}
			}
		}
		if (StringUtils.isNotBlank(lowerSb.toString())) {
			log.info(lowerSb.toString());
			final String title = "Fundb lower flow! "
					+ DateFormatUtils.format(new Date(), CrawlerHelper.dateTimeFormatPattern);
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
			final String title = "Fundb upper flow! "
					+ DateFormatUtils.format(new Date(), CrawlerHelper.dateTimeFormatPattern);
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
	public static String getLofJson(String hostPath, Date fireDate) {
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("___t", String.valueOf(fireDate.getTime()));
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:37.0) Gecko/20100101 Firefox/37.0");
		headerMap.put("Accept", "*/*");
		headerMap.put("Cache-Control", "no-cache");
		headerMap.put("Pragma", "no-cache");
		// Find yourself a better source.
		String json = HttpClientUtil.getStringFromHost(hostPath, paramMap, headerMap, "utf-8");
		return json;
	}
}
