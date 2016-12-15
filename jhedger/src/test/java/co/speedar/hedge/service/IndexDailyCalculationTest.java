package co.speedar.hedge.service;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class IndexDailyCalculationTest {
	@Autowired
	private IndexDailyCalculation service;

	@Test
	public void testCalculate() throws ParseException {
		Date start = new GregorianCalendar(2016, 11, 1).getTime();
		Date end = Calendar.getInstance().getTime();
		while (start.before(end)) {
			service.calculateDate(start);
			start = DateUtils.addDays(start, 1);
		}
	}
}
