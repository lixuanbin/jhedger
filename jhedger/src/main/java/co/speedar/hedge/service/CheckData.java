package co.speedar.hedge.service;

import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import co.speedar.hedge.util.CrawlerHelper;
import co.speedar.hedge.util.SwingUtil;

@Service
public class CheckData {
	protected static final Logger log = Logger.getLogger(CheckData.class);
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Scheduled(cron = "1 1 10 * * MON-FRI")
	public void doCheck() {
		Date date = new Date();
		if (!CrawlerHelper.isNotOpen(date) && CrawlerHelper.isTradeDate(date)) {
			boolean isOk = false;
			String checkSql = String.format("select max(craw_datetime) from `hedger2`.`etf_detail` partition(%s)",
					"p" + DateFormatUtils.format(date, CrawlerHelper.yearMonthFormatPattern));
			try {
				Date crawDate = jdbcTemplate.queryForObject(checkSql, Date.class);
				if (DateUtils.isSameDay(date, crawDate) && date.getTime() - crawDate.getTime() <= 300000) {
					isOk = true;
				}
			} catch (Exception e) {
				log.error(e, e);
			}
			if (!isOk) {
				SwingUtil.showDialog("Fund crawler problem!", "No etf data for today, please check!", 500, 300);
			}
		}
	}
}
