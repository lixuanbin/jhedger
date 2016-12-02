package co.speedar.hedge.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateFormatUtils;
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
public class EtfDaoTest {
	@Autowired
	private EtfDao etfDao;

	@Autowired
	private LofDao lofDao;

	@Test
	@Ignore
	public void testBatchInsert() throws IOException {
		Resource resource = new ClassPathResource("etfSample.json");
		String json = IOUtils.toString(resource.getInputStream());
		assertNotNull("etf sample should not be null", json);
		List<Map<String, Object>> etfList = CrawlerHelper
				.buildEtfListFromJson(new GregorianCalendar(2015, 8, 30, 16, 30, 30).getTime(), json);
		assertTrue("etf list should not be empty", etfList != null && !etfList.isEmpty());
		etfDao.batchInsertEtfDetail(etfList, DateFormatUtils.format(new GregorianCalendar(2015, 8, 30, 16, 30, 30),
				CrawlerHelper.dateTimeFormatPattern));
		IOUtils.closeQuietly(resource.getInputStream());
	}

	@Test
	public void testQueryLastTradeVolumeOver10M() {
		List<String> idList = etfDao.queryLastTradeVolumeOver10M();
		assertNotNull("should not be null", idList);
		for (String id : idList) {
			System.out.println(id);
		}
	}

	@Test
	public void readLogFileAndInsert() throws IOException {
		// String logFolderPath = "/Users/lixuanbin/Documents/jhedger_logs";
		String logFolderPath = "/Users/lixuanbin/Documents/jhedger_logs_2016-11-24";
		File logFolderFile = new File(logFolderPath);
		File[] logFiles = logFolderFile.listFiles();
		Pattern dateTimePattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}");
		for (File file : logFiles) {
			InputStream input = new FileInputStream(file);
			List<String> lines = IOUtils.readLines(input, "utf-8");
			IOUtils.closeQuietly(input);
			System.out.println("processing file: " + file.getName());
			String fireDateTime = null;
			for (String line : lines) {
				try {
					Matcher matcher = dateTimePattern.matcher(line);
					if (matcher.find()) {
						fireDateTime = matcher.group(0);
					}
					if (StringUtils.contains(line, "page") && StringUtils.contains(line, "{")
							&& StringUtils.contains(line, "}")) {
						if (StringUtils.contains(line, "fundb_id")) {
							List<Map<String, Object>> lofList = CrawlerHelper.buildFundbListFromJson(new Date(), line);
							lofDao.batchInsertFundbDetail(lofList, fireDateTime);
						} else if (StringUtils.contains(line, "fund_id")) {
							List<Map<String, Object>> etfList = CrawlerHelper.buildEtfListFromJson(new Date(), line);
							etfDao.batchInsertEtfDetail(etfList, fireDateTime);
						}
					}
				} catch (Exception e) {
					System.out.println(line);
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void testIsNumeric() {
		System.out.println(NumberUtils.isNumber("0.16"));
	}
}
