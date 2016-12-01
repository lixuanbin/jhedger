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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
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

	protected static final String batchInsertFundbDetailSql = "replace INTO `hedger2`.`lof_fundb_detail`(`fundb_id`,`fundb_name`,"
			+ "`fundb_current_price`,`fundb_volume`,`fundb_increase_rt`,`fundb_value`,`fundb_est_val`,`fundb_discount_rt`,"
			+ "`fundb_price_leverage_rt`,`fundb_net_leverage_rt`,`fundb_lower_recalc_rt`,`fundb_nav_datetime`,`fundb_upper_recalc_rt`,"
			+ "`fundb_index_id`,`fundb_index_increase_rt`,`fundb_base_est_dis_rt`,`craw_datetime`)VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	protected static final String batchInsertFundaDetailSql = "replace INTO `hedger2`.`lof_funda_detail` (`funda_id`, `funda_nav_datetime`, "
			+ "`craw_datetime`, `funda_name`, `funda_current_price`, `funda_increase_rt`, `funda_volume`, `funda_value`, `funda_discount_rt`, "
			+ "`coupon_descr_s`, `funda_coupon`, `funda_coupon_next`, `funda_profit_rt_next`, `funda_left_year`, `funda_index_id`, `funda_index_name`, "
			+ "`funda_index_increase_rt`, `funda_lower_recalc_rt`, `lower_recalc_profit_rt`, `fundb_upper_recalc_rt`, `funda_base_est_dis_rt`, "
			+ "`funda_base_est_dis_rt_t1`, `funda_base_est_dis_rt_t2`, `funda_amount`, `funda_amount_increase`, `abrate`, `next_recalc_dt`, "
			+ "`left_recalc_year`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	protected static final String lastTradeVolumeOver10MSql = "select distinct fundb_id from hedger2.lof_fundb_detail where DATE(fundb_nav_datetime)="
			+ "(select DATE(fundb_nav_datetime) as lastTradeDay from hedger2.lof_fundb_detail where DATE(fundb_nav_datetime)<CURDATE()-1 order by fundb_nav_datetime desc limit 1) and fundb_volume>1000;";

	protected static final String lastTradeVolumeOver5MSql = "select distinct fundb_id from hedger2.lof_fundb_detail where DATE(fundb_nav_datetime)="
			+ "(select DATE(fundb_nav_datetime) as lastTradeDay from hedger2.lof_fundb_detail where DATE(fundb_nav_datetime)<CURDATE()-1 order by fundb_nav_datetime desc limit 1) and fundb_volume>500;";

	protected static final String lastTradeBaseMaxDiscountSqlTpl = "select max(fundb_base_est_dis_rt) from hedger2.lof_fundb_detail where fundb_id='%s' "
			+ "and DATE(fundb_nav_datetime)=(select DATE(fundb_nav_datetime) as lastTradeDay from hedger2.lof_fundb_detail where DATE(fundb_nav_datetime)<CURDATE()-1 order by fundb_nav_datetime desc limit 1) "
			+ "and TIME(fundb_nav_datetime)>'14:45:00';";

	protected static final String lastLastTradeBaseMaxDiscountSqlTpl = "select max(fundb_base_est_dis_rt) from hedger2.lof_fundb_detail where fundb_id='%s' "
			+ "and DATE(fundb_nav_datetime)=(select DATE(fundb_nav_datetime) as lastTradeDay from hedger2.lof_fundb_detail where DATE(fundb_nav_datetime)<CURDATE()-2 order by fundb_nav_datetime desc limit 1) "
			+ "and TIME(fundb_nav_datetime)>'14:45:00';";

	/**
	 * Insert a list of fundb details.
	 * 
	 * @param lofList
	 */
	public void batchInsertFundbDetail(final List<Map<String, Object>> lofList, final String fireDay) {
		jdbcTemplate.batchUpdate(batchInsertFundbDetailSql, new BatchPreparedStatementSetter() {
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
				ps.setString(17, fireDay);
			}

			@Override
			public int getBatchSize() {
				return lofList.size();
			}
		});
	}

	/**
	 * Insert a list of funda details.
	 * 
	 * @param lofList
	 */
	public void batchInsertFundaDetail(final List<Map<String, Object>> lofList, final String fireDay) {
		jdbcTemplate.batchUpdate(batchInsertFundaDetailSql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Map<String, Object> map = lofList.get(i);
				ps.setString(1, String.valueOf(map.get("funda_id")));
				ps.setString(2, String.valueOf(map.get("funda_nav_datetime")));
				ps.setString(3, String.valueOf(map.get("craw_datetime")));
				ps.setString(4, String.valueOf(map.get("funda_name")));
				ps.setFloat(5, (Float) map.get("funda_current_price"));
				ps.setFloat(6, (Float) map.get("funda_increase_rt"));
				ps.setFloat(7, (Float) map.get("funda_volume"));
				ps.setFloat(8, (Float) map.get("funda_value"));
				ps.setFloat(9, (Float) map.get("funda_discount_rt"));
				ps.setString(10, String.valueOf(map.get("coupon_descr_s")));
				ps.setFloat(11, (Float) map.get("funda_coupon"));
				ps.setFloat(12, (Float) map.get("funda_coupon_next"));
				ps.setFloat(13, (Float) map.get("funda_profit_rt_next"));
				ps.setString(14, String.valueOf(map.get("funda_left_year")));
				ps.setString(15, String.valueOf(map.get("funda_index_id")));
				ps.setString(16, String.valueOf(map.get("funda_index_name")));
				ps.setFloat(17, (Float) map.get("funda_index_increase_rt"));
				ps.setFloat(18, (Float) map.get("funda_lower_recalc_rt"));
				ps.setFloat(19, (Float) map.get("lower_recalc_profit_rt"));
				ps.setFloat(20, (Float) map.get("fundb_upper_recalc_rt"));
				ps.setFloat(21, (Float) map.get("funda_base_est_dis_rt"));
				ps.setFloat(22, (Float) map.get("funda_base_est_dis_rt_t1"));
				ps.setFloat(23, (Float) map.get("funda_base_est_dis_rt_t2"));
				ps.setFloat(24, (Float) map.get("funda_amount"));
				ps.setFloat(25, (Float) map.get("funda_amount_increase"));
				ps.setString(26, String.valueOf(map.get("abrate")));
				ps.setString(27, String.valueOf(map.get("next_recalc_dt")));
				ps.setFloat(28, (Float) map.get("left_recalc_year"));
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
				if (rs.next()) {
					return rs.getString("fundb_id");
				} else {
					return "";
				}
			}
		});
	}

	/**
	 * 上个交易日成交量大于千万的基金id
	 * 
	 * @return
	 */
	public List<String> queryLastTradeVolumeOver5M() {
		return jdbcTemplate.query(lastTradeVolumeOver5MSql, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				if (rs.next()) {
					return rs.getString("fundb_id");
				} else {
					return "";
				}
			}
		});
	}

	/**
	 * 上个交易日收盘前的母基溢价率
	 * 
	 * @param fundb_id
	 * @return
	 */
	public float queryLastTradeBaseDiscountRate(String fundb_id) {
		return jdbcTemplate.query(String.format(lastTradeBaseMaxDiscountSqlTpl, fundb_id),
				new ResultSetExtractor<Float>() {
					@Override
					public Float extractData(ResultSet rs) throws SQLException, DataAccessException {
						if (rs.next()) {
							return rs.getFloat(1);
						} else {
							return new Float(0);
						}
					}
				});
	}

	/**
	 * 上上个交易日收盘前的母基溢价率
	 * 
	 * @param fundb_id
	 * @return
	 */
	public float queryLastLastTradeBaseDiscountRate(String fundb_id) {
		return jdbcTemplate.query(String.format(lastLastTradeBaseMaxDiscountSqlTpl, fundb_id),
				new ResultSetExtractor<Float>() {
					@Override
					public Float extractData(ResultSet rs) throws SQLException, DataAccessException {
						if (rs.next()) {
							return rs.getFloat(1);
						} else {
							return new Float(0);
						}
					}
				});
	}
}
