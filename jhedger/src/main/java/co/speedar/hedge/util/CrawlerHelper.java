/**
 * 
 */
package co.speedar.hedge.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector.SelectorParseException;

import co.speedar.hedge.service.LofCrawler;

/**
 * @author ben
 *
 */
public class CrawlerHelper {
	public static final String dateTimeFormatPattern = "yyyy-MM-dd HH:mm:ss";
	public static final String dateFormatPattern = "yyyy-MM-dd";
	public static final String yyyyMMddPattern = "yyyyMMdd";
	public static final String yearMonthFormatPattern = "yyyyMM";
	protected static final Logger log = Logger.getLogger(CrawlerHelper.class);

	/**
	 * 根据日期时间判断是否未开盘.<br/>
	 * 注意:返回为false不一定就是交易时间,还需判断是否公众假日.
	 * 
	 * @return
	 */
	public static boolean isNotOpen(Date nowDate) {
		if (isWeekend(nowDate)) {
			return true;
		}
		Calendar now = Calendar.getInstance();
		now.setTime(nowDate);
		Calendar nineThirty = Calendar.getInstance();
		nineThirty.setTime(nowDate);
		nineThirty.set(Calendar.HOUR_OF_DAY, 9);
		nineThirty.set(Calendar.MINUTE, 30);
		nineThirty.set(Calendar.SECOND, 0);
		Calendar elevenThirty = Calendar.getInstance();
		elevenThirty.setTime(nowDate);
		elevenThirty.set(Calendar.HOUR_OF_DAY, 11);
		elevenThirty.set(Calendar.MINUTE, 33);
		elevenThirty.set(Calendar.SECOND, 33);
		Calendar thirteen = Calendar.getInstance();
		thirteen.setTime(nowDate);
		thirteen.set(Calendar.HOUR_OF_DAY, 13);
		thirteen.set(Calendar.MINUTE, 0);
		thirteen.set(Calendar.SECOND, 0);
		Calendar fifteen = Calendar.getInstance();
		fifteen.setTime(nowDate);
		fifteen.set(Calendar.HOUR_OF_DAY, 15);
		fifteen.set(Calendar.MINUTE, 5);
		fifteen.set(Calendar.SECOND, 3);
		if (now.get(Calendar.DAY_OF_WEEK) == 1 || now.get(Calendar.DAY_OF_WEEK) == 7 || now.before(nineThirty)
				|| (now.after(elevenThirty) && now.before(thirteen)) || now.after(fifteen)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断指定日期是否周末
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isWeekend(Date date) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
		cal.setTime(date);
		int dow = cal.get(Calendar.DAY_OF_WEEK);
		if (dow == Calendar.SUNDAY || dow == Calendar.SATURDAY) {
			return true;
		}
		return false;
	}

	/**
	 * 判断指定日期是否交易日
	 * 
	 * @param date
	 * @return
	 */
	public static boolean isTradeDate(Date date) {
		boolean isOpen = false;
		if (isWeekend(date)) {
			return isOpen;
		}
		String[] codes = { "sh510050", "sh601398", "sh601857", "sh600653"};
		String day = DateFormatUtils.format(date, dateFormatPattern);
		if (DateUtils.isSameDay(date, Calendar.getInstance(TimeZone.getTimeZone("GMT+8")).getTime())) {
			Map<String, String> paramMap = new HashMap<>();
			String url = "http://hq.sinajs.cn/";
			Map<String, String> headerMap = new HashMap<>();
			headerMap.put("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:37.0) Gecko/20100101 Firefox/37.0");
			headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
			headerMap.put("Accept-Language", "en-US,en;q=0.5");
			headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			headerMap.put("Cache-Control", "no-cache");
			headerMap.put("Pragma", "no-cache");
			for (String code : codes) {
				paramMap.put("list", code);
				String json = HttpClientUtil.getStringFromHost(url, paramMap, headerMap, "utf-8");
				if (StringUtils.contains(json, day)) {
					isOpen = true;
					return isOpen;
				}
			}
		} else {
			String url = "http://market.finance.sina.com.cn/pricehis.php";
			Map<String, String> paramMap = new HashMap<>();
			paramMap.put("startdate", day);
			paramMap.put("enddate", day);
			for (String code : codes) {
				paramMap.put("symbol", code);
				try {
					String qurl = HttpClientUtil.buildUri(url, paramMap, "utf-8");
					Response response = Jsoup.connect(qurl)
							.header("User-Agent",
									"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0")
							.followRedirects(true).execute();
					Document doc = response.parse();
					Elements datalist = doc.select("table#datalist");
					if (datalist != null) {
						isOpen = true;
						return isOpen;
					}
				} catch (IOException e) {
					log.error(e, e);
				} catch (SelectorParseException e) {
					log.error(e, e);
				}
			}
		}
		return isOpen;
	}

	/**
	 * Return a new date object with given time params.
	 * 
	 * @param date
	 * @param hour
	 * @param minute
	 * @param second
	 * @return
	 */
	public static Date setTimeOfDate(Date date, int hour, int minute, int second) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		return cal.getTime();
	}

	public static List<Map<String, Object>> buildEtfListFromJson(Date fireDate, String json) {
		List<Map<String, Object>> etfList = new ArrayList<Map<String, Object>>();
		JSONObject jsonObject = new JSONObject(json);
		JSONArray jsonArray = jsonObject.getJSONArray("rows");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject j = jsonArray.getJSONObject(i);
			JSONObject cell = j.getJSONObject("cell");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("fund_id", cell.getString("fund_id"));
			String lastTime = cell.getString("last_time");
			String date = cell.getString("nav_dt");
			map.put("nav_datetime", date + " " + lastTime);
			map.put("fund_name", cell.getString("fund_nm"));
			map.put("index_id", cell.getString("index_id"));
			float price = Float.valueOf(cell.getString("price"));
			map.put("price", price);
			float volume = Float.valueOf(cell.getString("volume"));
			map.put("volume", volume);
			float increaseRate = Float
					.valueOf(NumberUtils.isNumber(StringUtils.removeEnd(cell.getString("increase_rt"), "%"))
							? StringUtils.removeEnd(cell.getString("increase_rt"), "%") : "-99");
			map.put("increase_rt", increaseRate);
			float indexIncreaseRate = Float
					.valueOf(NumberUtils.isNumber(StringUtils.removeEnd(cell.getString("index_increase_rt"), "%"))
							? StringUtils.removeEnd(cell.getString("index_increase_rt"), "%") : "-99");
			map.put("index_increase_rt", indexIncreaseRate);
			float estimateValue = Float.valueOf(
					NumberUtils.isNumber(cell.getString("estimate_value")) ? cell.getString("estimate_value") : "-99");
			map.put("estimate_value", estimateValue);
			float discountRate = Float
					.valueOf(NumberUtils.isNumber(StringUtils.removeEnd(cell.getString("discount_rt"), "%"))
							? StringUtils.removeEnd(cell.getString("discount_rt"), "%") : "-99");
			map.put("discount_rt", discountRate);
			try {
				float pe = Float.valueOf(NumberUtils.isNumber(cell.getString("pe")) ? cell.getString("pe") : "-99");
				map.put("pe", pe);
				float pb = Float.valueOf(NumberUtils.isNumber(cell.getString("pb")) ? cell.getString("pb") : "-99");
				map.put("pb", pb);
			} catch (Exception e) {
				map.put("pe", -99.0f);
				map.put("pb", -99.0f);
			}
			etfList.add(map);
		}
		return etfList;
	}

	public static List<Map<String, Object>> buildFundbListFromJson(Date fireDate, String json) {
		List<Map<String, Object>> lofList = new ArrayList<Map<String, Object>>();
		JSONObject jsonObject = new JSONObject(json);
		JSONArray jsonArray = jsonObject.getJSONArray("rows");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject j = jsonArray.getJSONObject(i);
			JSONObject cell = j.getJSONObject("cell");
			Map<String, Object> map = new HashMap<String, Object>();
			String fundbId = cell.getString("fundb_id");
			map.put("fundb_id", fundbId);
			String lastTime = cell.getString("last_time");
			String date = cell.getString("fundb_nav_dt");
			map.put("fundb_nav_datetime", date + " " + lastTime);
			String fundbName = cell.getString("fundb_name");
			map.put("fundb_name", fundbName);
			float currentPrice = Float.valueOf(NumberUtils.isNumber(cell.getString("fundb_current_price"))
					? cell.getString("fundb_current_price") : "-99");
			map.put("fundb_current_price", currentPrice);
			float fundbVolumn = Float.valueOf(
					NumberUtils.isNumber(cell.getString("fundb_volume")) ? cell.getString("fundb_volume") : "-99");
			map.put("fundb_volume", fundbVolumn);
			float increaseRate = Float
					.valueOf(NumberUtils.isNumber(StringUtils.removeEnd(cell.getString("fundb_increase_rt"), "%"))
							? StringUtils.removeEnd(cell.getString("fundb_increase_rt"), "%") : "-99");
			map.put("fundb_increase_rt", increaseRate);
			float fundbValue = Float.valueOf(
					NumberUtils.isNumber(cell.getString("fundb_value")) ? cell.getString("fundb_value") : "-99");
			map.put("fundb_value", fundbValue);
			float fundbEstValue = Float
					.valueOf(NumberUtils.isNumber(cell.getString("b_est_val")) ? cell.getString("b_est_val") : "-99");
			map.put("fundb_est_val", fundbEstValue);
			float fundbDiscountRate = Float
					.valueOf(NumberUtils.isNumber(StringUtils.removeEnd(cell.getString("fundb_discount_rt"), "%"))
							? StringUtils.removeEnd(cell.getString("fundb_discount_rt"), "%") : "-99");
			map.put("fundb_discount_rt", fundbDiscountRate);
			float fundbPriceRate = Float
					.valueOf(NumberUtils.isNumber(StringUtils.removeEnd(cell.getString("fundb_price_leverage_rt"), "%"))
							? StringUtils.removeEnd(cell.getString("fundb_price_leverage_rt"), "%") : "-99");
			map.put("fundb_price_leverage_rt", fundbPriceRate);
			float fundbNetRate = Float
					.valueOf(NumberUtils.isNumber(StringUtils.removeEnd(cell.getString("fundb_net_leverage_rt"), "%"))
							? StringUtils.removeEnd(cell.getString("fundb_net_leverage_rt"), "%") : "-99");
			map.put("fundb_net_leverage_rt", fundbNetRate);
			float fundbLowerRecalcRate = StringUtils.length(cell.getString("fundb_lower_recalc_rt")) > 1
					? Float.valueOf(StringUtils.removeEnd(cell.getString("fundb_lower_recalc_rt"), "%")) : 100;
			map.put("fundb_lower_recalc_rt", fundbLowerRecalcRate);
			float fundbUpperRate = Float
					.valueOf(NumberUtils.isNumber(StringUtils.removeEnd(cell.getString("fundb_upper_recalc_rt"), "%"))
							? StringUtils.removeEnd(cell.getString("fundb_upper_recalc_rt"), "%") : "-99");
			map.put("fundb_upper_recalc_rt", fundbUpperRate);
			String indexId = cell.getString("fundb_index_id");
			map.put("fundb_index_id", indexId);
			float indexIncreaseRate = Float
					.valueOf(NumberUtils.isNumber(StringUtils.removeEnd(cell.getString("fundb_index_increase_rt"), "%"))
							? StringUtils.removeEnd(cell.getString("fundb_index_increase_rt"), "%") : "-99");
			map.put("fundb_index_increase_rt", indexIncreaseRate);
			float baseDiscountRate = Float
					.valueOf(NumberUtils.isNumber(StringUtils.removeEnd(cell.getString("fundb_base_est_dis_rt"), "%"))
							? StringUtils.removeEnd(cell.getString("fundb_base_est_dis_rt"), "%") : "-99");
			map.put("fundb_base_est_dis_rt", baseDiscountRate);
			lofList.add(map);
		}
		return lofList;
	}

	public static List<Map<String, Object>> buildFundaListFromJson(Date fireDate, String json) {
		List<Map<String, Object>> lofList = new ArrayList<Map<String, Object>>();
		JSONObject jsonObject = new JSONObject(json);
		JSONArray jsonArray = jsonObject.getJSONArray("rows");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject j = jsonArray.getJSONObject(i);
			JSONObject cell = j.getJSONObject("cell");
			Map<String, Object> map = new HashMap<String, Object>();
			saturateMapFromJSONObject(map, cell);
			lofList.add(map);
		}
		return lofList;
	}

	/**
	 * Saturate map from json string
	 * 
	 * @param map
	 * @param json
	 */
	public static void saturateMapFromJSONObject(Map<String, Object> map, JSONObject json) {
		for (Object keyObj : json.keySet()) {
			String key = (String) keyObj;
			Object val = json.get(key);
			if (val != null) {
				if ((NumberUtils.isNumber(StringUtils.removeEnd(String.valueOf(val), "%"))
						&& !StringUtils.endsWith(key, "_id")) || StringUtils.contains(key, "_rt")
						|| StringUtils.contains(key, "_price")) {
					float f = Float.valueOf(NumberUtils.isNumber(StringUtils.removeEnd(String.valueOf(val), "%"))
							? StringUtils.removeEnd(String.valueOf(json.get(key)), "%") : "-99");
					map.put(key, f);
				} else if (StringUtils.contains(String.valueOf(key), "nav_dt")) {
					String dt = String.valueOf(json.get(key));
					String last_time = json.getString("last_time");
					map.put(StringUtils.replace(key, "dt", "datetime"), dt + " " + last_time);
				} else {
					if (StringUtils.contains(String.valueOf(val), "<")
							&& StringUtils.contains(String.valueOf(val), ">")) {
						int start = StringUtils.indexOf(String.valueOf(val), ">");
						int end = StringUtils.indexOf(String.valueOf(val), "</");
						map.put(key, StringUtils.substring(String.valueOf(val), start + 1, end));
					} else {
						map.put(key, String.valueOf(val));
					}
				}
			}
		}
		map.put("craw_datetime", DateFormatUtils.format(new Date(), CrawlerHelper.dateTimeFormatPattern));
	}

	public static void main(String[] args) {
		System.out.println(isTradeDate(new GregorianCalendar(2016, 10, 30).getTime()));
		System.out.println(LofCrawler.getLofJson(LofCrawler.fundmHostPath, new Date()));
	}
}
