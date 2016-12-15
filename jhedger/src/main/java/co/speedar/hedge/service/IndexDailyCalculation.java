package co.speedar.hedge.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import co.speedar.hedge.util.CrawlerHelper;

@Service
public class IndexDailyCalculation {
	protected static final Logger log = Logger.getLogger(IndexDailyCalculation.class);

	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	/**
	 * calculate index ma for date
	 * 
	 * @param date
	 */
	public void calculateDate(Date date) {
		String day = DateFormatUtils.format(date, CrawlerHelper.dateFormatPattern);
		String queryIndexIdSql = "select distinct index_id from `hedger2`.`index_day` where trade_date=?";
		try {
			List<String> indexIds = jdbcTemplate.queryForList(queryIndexIdSql, String.class, day);
			if (indexIds != null && !indexIds.isEmpty()) {
				for (String indexId : indexIds) {
					log.info(String.format("Calculating moving average of index[%s] at day[%s].", indexId, day));
					Map<String, String> map = new HashMap<>();
					map.put("close_price", "price_ma");
					map.put("volume", "volume_ma");
					for (Map.Entry<String, String> entry : map.entrySet()) {
						String queryAverageSql = String.format(
								"select %s from `hedger2`.`index_day` where "
										+ "index_id=? and trade_date<=? order by trade_date desc limit 250",
								entry.getKey());
						List<Float> list = jdbcTemplate.queryForList(queryAverageSql, Float.class, indexId, day);
						if (list != null && !list.isEmpty()) {
							float current = list.get(0);
							float average = 0;
							float bias = 0;
							int[] mas = { 5, 10, 20, 30, 60, 120, 250 };
							for (int ma : mas) {
								if (list.size() >= ma) {
									average = calculateAverage(list, ma);
									bias = (current - average) / average;
									updateAverage(entry.getValue() + ma, indexId, day, ma, average);
									updateAverage("price_bias" + ma, indexId, day, ma, bias);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	public int updateAverage(String name, String indexId, String day, int ma, float average) {
		String sql = String.format("update `hedger2`.`index_day` set %s=? where index_id=? and trade_date=?", name);
		return jdbcTemplate.update(sql, average, indexId, day);
	}

	public float calculateAverage(List<Float> list, int ma) {
		if (list == null || list.size() < ma) {
			throw new RuntimeException("list size not big enough for calculating the averge " + ma);
		}
		float sum = 0;
		for (int i = 0; i < ma; i++) {
			sum += list.get(i);
		}
		return sum / ma;
	}

	@Scheduled(cron = "1 33 19 * * MON-FRI")
	public void calculate() {
		Date date = new Date();
		if (CrawlerHelper.isTradeDate(date)) {
			calculateDate(date);
		}
	}
}
