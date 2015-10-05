package co.speedar.hedge.service;

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
		lofCrawler.craw();
	}

	@Test
	public void testEtf() {
		etfCrawler.craw();
	}
}
