/**
 * 
 */
package co.speedar.hedge.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * @author ben
 *
 */
@Repository
public class LofDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	protected static final String batchInsertLofDetailSql = "insert ignore INTO `hedger`.`lof_fundb_detail`(`fundb_id`,`fundb_name`,"
			+ "`fundb_current_price`,`fundb_volume`,`fundb_increase_rt`,`fundb_value`,`fundb_est_val`,`fundb_discount_rt`,"
			+ "`fundb_price_leverage_rt`,`fundb_net_leverage_rt`,`fundb_lower_recalc_rt`,`fundb_nav_datetime`,`fundb_upper_recalc_rt`,"
			+ "`fundb_index_id`,`fundb_index_increase_rt`,`fundb_base_est_dis_rt`)VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	protected static final String lastTradeVolumeOver10MSql = "select distinct fundb_id from hedger.lof_fundb_detail where DATE(fundb_nav_datetime)="
			+ "(select DATE(fundb_nav_datetime) as lastTradeDay from hedger.lof_fundb_detail where DATE(fundb_nav_datetime)<CURDATE()-1 order by updateTime desc limit 1) and fundb_volume>1000;";

	/**
	 * Insert a list of lof details.
	 * 
	 * @param lofList
	 */
	public void batchInsertLofDetail(final List<Map<String, Object>> lofList) {
		jdbcTemplate.batchUpdate(batchInsertLofDetailSql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Map<String, Object> map = lofList.get(i);
				ps.setString(1, String.valueOf(map.get("fundb_id")));
				ps.setString(2, String.valueOf(map.get("fundb_name")));
				ps.setFloat(3, (Float) map.get("fundb_current_price"));
				ps.setFloat(4, (Float) map.get("fundb_volume"));
				ps.setFloat(5, (Float) map.get("fundb_increase_rt"));
				ps.setFloat(6, (Float) map.get("fundb_value"));
				ps.setFloat(7, (Float) map.get("fundb_est_val"));
				ps.setFloat(8, (Float) map.get("fundb_discount_rt"));
				ps.setFloat(9, (Float) map.get("fundb_price_leverage_rt"));
				ps.setFloat(10, (Float) map.get("fundb_net_leverage_rt"));
				ps.setFloat(11, (Float) map.get("fundb_lower_recalc_rt"));
				ps.setString(12, String.valueOf(map.get("fundb_nav_datetime")));
				ps.setFloat(13, (Float) map.get("fundb_upper_recalc_rt"));
				ps.setString(14, String.valueOf(map.get("fundb_index_id")));
				ps.setFloat(15, (Float) map.get("fundb_index_increase_rt"));
				ps.setFloat(16, (Float) map.get("fundb_base_est_dis_rt"));
			}

			@Override
			public int getBatchSize() {
				return lofList.size();
			}
		});
	}

	/**
	 * 上个交易日成交量大于千万的基金id
	 * 
	 * @return
	 */
	public List<String> queryLastTradeVolumeOver10M() {
		return jdbcTemplate.query(lastTradeVolumeOver10MSql, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString("fundb_id");
			}
		});
	}
}
