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
		assertFalse(CrawlerHelper.sdf.format(now) + " should be trading time.", CrawlerHelper.isNotOpen(now));
		now = new GregorianCalendar(2015, 9, 8, 11, 30, 30).getTime();
		assertTrue(CrawlerHelper.sdf.format(now) + " should not be trading time.", CrawlerHelper.isNotOpen(now));
		now = new GregorianCalendar(2015, 9, 8, 8, 30, 30).getTime();
		assertTrue(CrawlerHelper.sdf.format(now) + " should not be trading time.", CrawlerHelper.isNotOpen(now));
		now = new GregorianCalendar(2015, 9, 8, 15, 3, 30).getTime();
		assertTrue(CrawlerHelper.sdf.format(now) + " should not be trading time.", CrawlerHelper.isNotOpen(now));
	}
}
