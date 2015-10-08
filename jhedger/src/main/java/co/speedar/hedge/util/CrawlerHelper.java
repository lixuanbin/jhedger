/**
 * 
 */
package co.speedar.hedge.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * @author ben
 *
 */
public class CrawlerHelper {
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
		elevenThirty.set(Calendar.MINUTE, 30);
		elevenThirty.set(Calendar.SECOND, 0);
		Calendar thirteen = Calendar.getInstance();
		thirteen.setTime(nowDate);
		thirteen.set(Calendar.HOUR_OF_DAY, 13);
		thirteen.set(Calendar.MINUTE, 0);
		thirteen.set(Calendar.SECOND, 0);
		Calendar fifteen = Calendar.getInstance();
		fifteen.setTime(nowDate);
		fifteen.set(Calendar.HOUR_OF_DAY, 15);
		fifteen.set(Calendar.MINUTE, 0);
		fifteen.set(Calendar.SECOND, 0);
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
}
