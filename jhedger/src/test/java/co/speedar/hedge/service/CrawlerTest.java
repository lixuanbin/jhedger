package co.speedar.hedge.service;

import java.util.Date;
import java.util.GregorianCalendar;

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
	public void testLof() {
		Date fireDate = new GregorianCalendar(2015, 9, 7, 10, 30, 30).getTime();
		lofCrawler.craw(fireDate);
		System.out.println(fireDate);
	}

	@Test
	public void testEtf() {
		Date fireDate = new GregorianCalendar(2015, 9, 7, 10, 30, 30).getTime();
		etfCrawler.craw(fireDate);
		System.out.println(fireDate);
	}
}
