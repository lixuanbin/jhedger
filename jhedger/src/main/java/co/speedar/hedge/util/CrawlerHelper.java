/**
 * 
 */
package co.speedar.hedge.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author ben
 *
 */
public class CrawlerHelper {
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void main(String[] args) {
		Calendar now = Calendar.getInstance();
		System.out.println(now.get(Calendar.DAY_OF_WEEK));
		Calendar nineThirty = Calendar.getInstance();
		nineThirty.set(Calendar.HOUR_OF_DAY, 9);
		nineThirty.set(Calendar.MINUTE, 30);
		nineThirty.set(Calendar.SECOND, 0);
		Calendar elevenThirty = Calendar.getInstance();
		elevenThirty.set(Calendar.HOUR_OF_DAY, 11);
		elevenThirty.set(Calendar.MINUTE, 30);
		elevenThirty.set(Calendar.SECOND, 0);
		Calendar thirteen = Calendar.getInstance();
		thirteen.set(Calendar.HOUR_OF_DAY, 13);
		thirteen.set(Calendar.MINUTE, 0);
		thirteen.set(Calendar.SECOND, 0);
		System.out.println(nineThirty.getTime().toString());
		System.out.println(elevenThirty.getTime().toString());
		System.out.println(thirteen.getTime().toString());
	}

	/**
	 * 是否未开盘
	 * 
	 * @return
	 */
	public static boolean notOpen() {
		Calendar nineThirty = Calendar.getInstance();
		nineThirty.set(Calendar.HOUR_OF_DAY, 9);
		nineThirty.set(Calendar.MINUTE, 30);
		nineThirty.set(Calendar.SECOND, 0);
		Calendar elevenThirty = Calendar.getInstance();
		elevenThirty.set(Calendar.HOUR_OF_DAY, 11);
		elevenThirty.set(Calendar.MINUTE, 30);
		elevenThirty.set(Calendar.SECOND, 0);
		Calendar thirteen = Calendar.getInstance();
		thirteen.set(Calendar.HOUR_OF_DAY, 13);
		thirteen.set(Calendar.MINUTE, 0);
		thirteen.set(Calendar.SECOND, 0);
		Calendar now = Calendar.getInstance();
		if (now.get(Calendar.DAY_OF_WEEK) == 1 || now.get(Calendar.DAY_OF_WEEK) == 7 || now.before(nineThirty)
				|| (now.after(elevenThirty) && now.before(thirteen))) {
			return true;
		} else {
			return false;
		}
	}
}
