/**
 * 
 */
package co.speedar.hedge.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author ben
 *
 */
@Repository
public class EtfDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	protected static final String batchInsertEtfDetailSql = "replace INTO `hedger`.`etf_detail`(`fund_id`,`nav_datetime`,"
			+ "`fund_name`,`index_id`,`price`,`volume`,`increase_rt`,`index_increase_rt`,`estimate_value`,`discount_rt`)"
			+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	/**
	 * Insert a list of etf details.
	 * 
	 * @param etfList
	 */
	public void batchInsertEtfDetail(final List<Map<String, Object>> etfList) {
		jdbcTemplate.batchUpdate(batchInsertEtfDetailSql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Map<String, Object> map = etfList.get(i);
				ps.setString(1, String.valueOf(map.get("fund_id")));
				ps.setString(2, String.valueOf(map.get("nav_datetime")));
				ps.setString(3, String.valueOf(map.get("fund_name")));
				ps.setString(4, String.valueOf(map.get("index_id")));
				ps.setFloat(5, (Float) map.get("price"));
				ps.setFloat(6, (Float) map.get("volume"));
				ps.setFloat(7, (Float) map.get("increase_rt"));
				ps.setFloat(8, (Float) map.get("index_increase_rt"));
				ps.setFloat(9, (Float) map.get("estimate_value"));
				ps.setFloat(10, (Float) map.get("discount_rt"));
			}

			@Override
			public int getBatchSize() {
				return etfList.size();
			}
		});
	}
}
