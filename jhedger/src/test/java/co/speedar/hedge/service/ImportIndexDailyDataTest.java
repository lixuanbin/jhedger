package co.speedar.hedge.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class ImportIndexDailyDataTest {
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Test
	public void testAll() throws IOException {
		List<String> sqls = new ArrayList<>();
		String sqlTpl = "insert ignore INTO `hedger2`.`index_day`(`index_id`,`trade_date`,`index_name`,"
				+ "`max_price`,`min_price`,`open_price`,`close_price`,`increase`,`increase_rt`,"
				+ "`volume`,`volume2`) VALUES ('%s','%s','%s',%s,%s,%s,%s,%s,%s,%s,%f);";
		File szzs = new File("/Users/lixuanbin/Downloads/000905.csv");
		InputStream input = new FileInputStream(szzs);
		List<String> lines = IOUtils.readLines(input);
		IOUtils.closeQuietly(input);
		for (String line : lines) {
			String[] ss = line.split(",");
			String sql = String.format(sqlTpl, ss[1], ss[0], ss[2], ss[4], ss[5], ss[6], ss[3], ss[8], ss[9], ss[10],
					Float.valueOf(ss[11]));
			sqls.add(sql);
		}
		String[] strings = new String[sqls.size()];
		jdbcTemplate.batchUpdate(sqls.toArray(strings));
	}
}
