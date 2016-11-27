/**
 * 
 */
package co.speedar.hedge.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author ben
 *
 */
public class CrawlerHelper {
	public static final String dateTimeFormatPattern = "yyyy-MM-dd HH:mm:ss";
	public static final String dateFormatPattern = "yyyy-MM-dd";
	public static final String yearMonthFormatPattern = "yyyyMM";
	protected static final Logger log = Logger.getLogger(CrawlerHelper.class);

	/**
	 * 根据日期时间判断是否未开盘.<br/>
	 * 注意:返回为false不一定就是交易时间,还需判断是否公众假日.
	 * 
	 * @return
	 */
	public static boolean isNotOpen(Date nowDate) {
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

	public static List<Map<String, Object>> buildLofListFromJson(Date fireDate, String json) {
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
}
