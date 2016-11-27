package co.speedar.hedge.service;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import co.speedar.hedge.util.CrawlerHelper;

@Service
public class EtfDailyCalculation {
	protected static final Logger log = Logger.getLogger(EtfDailyCalculation.class);
	@Autowired
	private VelocityEngine velocityEngine;

	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	public void calculateDate(Date date) {
		String day = DateFormatUtils.format(date, CrawlerHelper.dateFormatPattern);
		log.info(day);
		Date yesterday = DateUtils.addDays(date, -1);
		Date tomorrow = DateUtils.addDays(date, 1);
		Set<String> partitions = new HashSet<String>();
		partitions.add("p" + DateFormatUtils.format(date, CrawlerHelper.yearMonthFormatPattern));
		partitions.add("p" + DateFormatUtils.format(yesterday, CrawlerHelper.yearMonthFormatPattern));
		partitions.add("p" + DateFormatUtils.format(tomorrow, CrawlerHelper.yearMonthFormatPattern));
		log.info(partitions);
		String vm = "sql/etf_daily.sql";
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("day", day);
		map.put("partition", StringUtils.join(partitions, ","));
		String sql = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, vm, "utf-8", map);
		try {
			jdbcTemplate.execute(sql);
		} catch (Exception e) {
			log.error(e, e);
		}
	}
}
