create schema if not exists hedger;
CREATE TABLE if not exists `hedger`.`lof_fundb_detail` (
  `fundb_id` VARCHAR(12) NOT NULL COMMENT 'B端代码',
  `fundb_nav_datetime` DATETIME NOT NULL COMMENT '记录时间',
  `fundb_name` VARCHAR(12) NULL COMMENT 'B端名称',
  `fundb_current_price` FLOAT NULL COMMENT 'B端现价',
  `fundb_volume` FLOAT NULL COMMENT 'B端成交量',
  `fundb_increase_rt` FLOAT NULL COMMENT 'B端涨幅',
  `fundb_value` FLOAT NULL COMMENT 'B端净值(昨日)',
  `fundb_est_val` FLOAT NULL COMMENT 'B端估算净值',
  `fundb_discount_rt` FLOAT NULL COMMENT 'B端溢价率',
  `fundb_price_leverage_rt` FLOAT NULL COMMENT '价格杠杆',
  `fundb_net_leverage_rt` FLOAT NULL COMMENT 'B端净值杠杆',
  `fundb_lower_recalc_rt` FLOAT NULL COMMENT 'B端下折距离',
  `fundb_upper_recalc_rt` FLOAT NULL COMMENT 'B端上折距离',
  `fundb_index_id` VARCHAR(12) NULL COMMENT '跟踪指数',
  `fundb_index_increase_rt` FLOAT NULL COMMENT '指数涨幅',
  `fundb_base_est_dis_rt` FLOAT NULL COMMENT '母基金整体溢价率(估算值)',
  PRIMARY KEY (`fundb_id`, `fundb_nav_datetime`)  COMMENT '') 
PARTITION BY RANGE(year(fundb_nav_datetime)) (
partition p2013 values less than (2013),
partition p2014 values less than (2014),
partition p2015 values less than (2015),
partition p2016 values less than (2016),
partition p2017 values less than (2017),
partition p2018 values less than (2018),
partition p2019 values less than (2019),
partition p2020 values less than (2020)
);
CREATE TABLE if not exists `hedger`.`etf_detail` (
  `fund_id` VARCHAR(12) NOT NULL COMMENT '',
  `nav_datetime` DATETIME NOT NULL COMMENT '',
  `fund_name` VARCHAR(12) NULL COMMENT '',
  `index_id` VARCHAR(12) NULL COMMENT '',
  `price` FLOAT NULL COMMENT '',
  `volume` FLOAT NULL COMMENT '',
  `increase_rt` FLOAT NULL COMMENT '',
  `index_increase_rt` VARCHAR(45) NULL COMMENT '',
  `estimate_value` FLOAT NULL COMMENT '',
  `discount_rt` FLOAT NULL COMMENT '',
  PRIMARY KEY (`fund_id`, `nav_datetime`)  COMMENT '')
PARTITION BY RANGE(year(nav_datetime)) (
partition p2013 values less than (2013),
partition p2014 values less than (2014),
partition p2015 values less than (2015),
partition p2016 values less than (2016),
partition p2017 values less than (2017),
partition p2018 values less than (2018),
partition p2019 values less than (2019),
partition p2020 values less than (2020)
);
