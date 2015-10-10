/**
 * 
 */
package co.speedar.hedge.util;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

/**
 * @author ben
 *
 */
public class CrawlerHelperTest {
	
	@Test
	public void testNotOpen() {
		Date now = new GregorianCalendar(2015, 9, 8, 9, 30, 30).getTime();
		System.out.println(CrawlerHelper.sdf.format(now));
		assertFalse(CrawlerHelper.sdf.format(now) + " should be trading time.", CrawlerHelper.isNotOpen(now));
		now = new GregorianCalendar(2015, 9, 8, 11, 32, 30).getTime();
		System.out.println(CrawlerHelper.sdf.format(now));
		assertTrue(CrawlerHelper.sdf.format(now) + " should not be trading time.", CrawlerHelper.isNotOpen(now));
		now = new GregorianCalendar(2015, 9, 8, 8, 30, 30).getTime();
		System.out.println(CrawlerHelper.sdf.format(now));
		assertTrue(CrawlerHelper.sdf.format(now) + " should not be trading time.", CrawlerHelper.isNotOpen(now));
		now = new GregorianCalendar(2015, 9, 8, 15, 6, 30).getTime();
		System.out.println(CrawlerHelper.sdf.format(now));
		assertTrue(CrawlerHelper.sdf.format(now) + " should not be trading time.", CrawlerHelper.isNotOpen(now));
	}
}
