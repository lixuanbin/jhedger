package co.speedar.hedge.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import co.speedar.hedge.util.CrawlerHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class LofDaoTest {
	@Autowired
	private LofDao dao;

	@Test
	@Ignore
	public void testBatchInsert() throws IOException {
		Resource resource = new ClassPathResource("lofSample.json");
		String json = IOUtils.toString(resource.getInputStream());
		assertNotNull("lof sample should not be null", json);
		List<Map<String, Object>> lofList = CrawlerHelper.buildLofListFromJson(
				new GregorianCalendar(2015, 8, 30, 16, 30, 30).getTime(), json);
		assertTrue("lof list should not be empty", lofList != null && !lofList.isEmpty());
		dao.batchInsertLofDetail(lofList);
		IOUtils.closeQuietly(resource.getInputStream());
	}

	@Test
	public void testQueryLastTradeVolumeOver10M() {
		List<String> idList = dao.queryLastTradeVolumeOver10M();
		assertNotNull("should not be null", idList);
		for (String id : idList) {
			System.out.println(id);
		}
	}
	
	@Test
	public void testQueryLastTradeVolumeOver5M() {
		List<String> idList = dao.queryLastTradeVolumeOver5M();
		assertNotNull("should not be null", idList);
		for (String id : idList) {
			System.out.println(id);
		}
	}
	
	@Test
	public void testQueryLastTradeBaseDiscountRate() {
		Float lastBaseDiscount = dao.queryLastTradeBaseDiscountRate("150056");
		assertNotNull("should not be null.", lastBaseDiscount);
		System.out.println(lastBaseDiscount);
	}
}
