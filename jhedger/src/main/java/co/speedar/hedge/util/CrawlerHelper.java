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
		Calendar now = new Calendar.Builder().setInstant(nowDate).build();
		Calendar nineThirty = new Calendar.Builder().setInstant(nowDate).build();
		nineThirty.set(Calendar.HOUR_OF_DAY, 9);
		nineThirty.set(Calendar.MINUTE, 30);
		nineThirty.set(Calendar.SECOND, 0);
		Calendar elevenThirty = new Calendar.Builder().setInstant(nowDate).build();
		elevenThirty.set(Calendar.HOUR_OF_DAY, 11);
		elevenThirty.set(Calendar.MINUTE, 30);
		elevenThirty.set(Calendar.SECOND, 0);
		Calendar thirteen = new Calendar.Builder().setInstant(nowDate).build();
		thirteen.set(Calendar.HOUR_OF_DAY, 13);
		thirteen.set(Calendar.MINUTE, 0);
		thirteen.set(Calendar.SECOND, 0);
		Calendar fifteen = new Calendar.Builder().setInstant(nowDate).build();
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
}
