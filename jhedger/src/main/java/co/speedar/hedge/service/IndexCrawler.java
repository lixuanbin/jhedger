package co.speedar.hedge.service;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import co.speedar.hedge.util.CrawlerHelper;

@Service
public class IndexCrawler {
	protected static final Logger log = Logger.getLogger(IndexCrawler.class);
	// 399300,399966,000974,000853,399810,399005,399905,000998,399967,399973,399989,399998,399975,110010,110000,399395,399440,399006
	// http://quote.eastmoney.com/zs399006.html

	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Value("${index.id.list}")
	private String indexIdList;

	public void calculateDate(Date date) {
		String day = DateFormatUtils.format(date, CrawlerHelper.yyyyMMddPattern);
		String tradeDay = DateFormatUtils.format(date, CrawlerHelper.dateFormatPattern);
		for (String index : StringUtils.split(indexIdList, ",")) {
			log.info(String.format("Getting trade info of index[%s] at day[%s].", index, tradeDay));
			String url = String.format("http://quotes.money.163.com/trade/lsjysj_zhishu_%s.html", index);
			String insertSql = "insert ignore into hedger2.index_day (index_id, trade_date, index_name, max_price, "
					+ "min_price, open_price, close_price, increase, increase_rt, volume, volume2) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			try {
				Document doc = Jsoup.connect(url).timeout(5000).followRedirects(true).execute().parse();
				String indexName = doc.select("div[class='stock_info']>table>tbody>tr>td>div[class='name']>a").text();
				Elements trs = doc.select("div[class='inner_box']>table>tbody>tr");
				for (Element tr : trs) {
					Elements tds = tr.select("td");
					Element dt = tds.get(0);
					if (StringUtils.equals(dt.text(), day)) {
						log.info(String.format("Trade info of index[%s] at day[%s]: \r\n%s", index, tradeDay, tr.toString()));
						float openPrice = NumberUtils.toFloat(StringUtils.replace(tds.get(1).text(), ",", ""));
						float maxPrice = NumberUtils.toFloat(StringUtils.replace(tds.get(2).text(), ",", ""));
						float minPrice = NumberUtils.toFloat(StringUtils.replace(tds.get(3).text(), ",", ""));
						float closePrice = NumberUtils.toFloat(StringUtils.replace(tds.get(4).text(), ",", ""));
						float increase = NumberUtils.toFloat(StringUtils.replace(tds.get(5).text(), ",", ""));
						float increase_rt = NumberUtils.toFloat(StringUtils.replace(tds.get(6).text(), ",", ""));
						float volume = NumberUtils.toFloat(StringUtils.replace(tds.get(7).text(), ",", ""));
						float volume2 = NumberUtils.toFloat(StringUtils.replace(tds.get(8).text(), ",", ""));
						jdbcTemplate.update(insertSql, index, tradeDay, indexName, maxPrice, minPrice, openPrice,
								closePrice, increase, increase_rt, volume, volume2);
					}
				}
			} catch (IOException e) {
				log.error(String.format("Error getting trade info for index[%s] at day[%s].", index, tradeDay));
				log.error(e, e);
			}
		}
	}

	@Scheduled(cron = "1 33 18 * * MON-FRI")
	public void calculate() {
		Date date = new Date();
		if (CrawlerHelper.isTradeDate(date)) {
			calculateDate(date);
		}
	}
}
