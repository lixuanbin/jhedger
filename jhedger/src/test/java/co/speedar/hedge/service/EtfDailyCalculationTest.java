package co.speedar.hedge.service;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class EtfDailyCalculationTest {
	@Autowired
	private EtfDailyCalculation etfDaily;

	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Test
	public void testDaily() {
		etfDaily.calculateDate(new GregorianCalendar(2016, 9, 20).getTime());
	}

	@Test
	public void testAll() {
		String selectDays = "select distinct date(craw_datetime) craw_daytime from `hedger2`.`etf_detail` order by craw_daytime;";
		List<Date> dates = jdbcTemplate.queryForList(selectDays, Date.class);
		for (Date date : dates) {
			etfDaily.calculateDate(date);
		}
	}
}
