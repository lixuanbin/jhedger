package co.speedar.hedge.service;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class CrawlerTest {
	@Autowired
	private LofCrawler lofCrawler;

	@Autowired
	private EtfCrawler etfCrawler;

	@Test
	@Ignore
	public void testLof() {
		Date fireDate = new GregorianCalendar(2015, 9, 21, 14, 59, 30).getTime();
		lofCrawler.craw(fireDate);
		System.out.println(fireDate);
	}

	@Test
	@Ignore
	public void testEtf() {
		Date fireDate = new GregorianCalendar(2015, 9, 7, 10, 30, 30).getTime();
		etfCrawler.craw(fireDate);
		System.out.println(fireDate);
	}

	@Test
	public void testGetEtfJson() {
		Date fireDate = new GregorianCalendar(2015, 9, 10, 12, 40, 30).getTime();
		String json = etfCrawler.getEtfJson(fireDate);
		System.out.println(json);
	}

	@Test
	public void testGetLofJson() {
		Date fireDate = new GregorianCalendar(2015, 9, 10, 12, 40, 30).getTime();
		String json = lofCrawler.getLofJson(fireDate);
		System.out.println(json);
	}
}
