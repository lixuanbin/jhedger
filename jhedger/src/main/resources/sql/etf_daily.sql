# daily calculation of etf_details
# params: ${day}, ${partition}
INSERT INTO `hedger2`.`etf_day`(fund_id, nav_date, trade_date, fund_name, index_id, max_price, avg_price, min_price, open_price, close_price, volume, increase_rt, index_increase_rt, estimate_value, discount_rt, pe, pb)
SELECT *
FROM
  (SELECT t1.fund_id fund_id,
          t1.nav_daytime nav_date,
          t1.craw_daytime trade_date,
          t1.fund_name fund_name,
          t1.index_id index_id,
          t1.max_price max_price,
          t1.avg_price avg_price,
          t1.min_price min_price,
          t2.open_price open_price,
          t3.close_price close_price,
          t3.volume volume,
          t3.increase_rt increase_rt,
          t3.index_increase_rt index_increase_rt,
          t3.estimate_value estimate_value,
          t3.discount_rt discount_rt,
          t3.pe pe,
          t3.pb pb
   FROM
     (SELECT fund_id,
             date(nav_datetime) nav_daytime,
             date(craw_datetime) craw_daytime,
             fund_name,
             index_id,
             max(price) max_price,
             avg(price) avg_price,
             min(price) min_price
      FROM `hedger2`.`etf_detail` partition(${partition})
      WHERE craw_datetime>='${day}'
        AND craw_datetime<date_add('${day}', interval 1 DAY)
      GROUP BY fund_id,
               craw_daytime) t1
   JOIN
     (SELECT t1.fund_id,
             t1.craw_daytime,
             t1.craw_datetime,
             t1.price open_price
      FROM
        (SELECT fund_id,
                date(craw_datetime) craw_daytime,
                craw_datetime,
                price
         FROM `hedger2`.`etf_detail` partition(${partition}) WHERE craw_datetime>='${day}'
           AND craw_datetime<date_add('${day}', interval 1 DAY)) t1
      JOIN
        (SELECT fund_id,
                date(craw_datetime) craw_daytime,
                min(craw_datetime) min_time
         FROM `hedger2`.`etf_detail` partition(${partition})
         WHERE craw_datetime>='${day}'
           AND craw_datetime<date_add('${day}', interval 1 DAY)
         GROUP BY fund_id,
                  craw_daytime) t2 ON t1.fund_id=t2.fund_id
      AND t1.craw_daytime=t2.craw_daytime
      AND t1.craw_datetime=t2.min_time) t2 ON t1.fund_id=t2.fund_id
   AND t1.craw_daytime=t2.craw_daytime
   JOIN
     (SELECT t1.fund_id,
             t1.craw_daytime,
             t1.craw_datetime,
             t1.price close_price,
             t1.volume,
             t1.increase_rt,
             t1.index_increase_rt,
             t1.estimate_value,
             t1.discount_rt,
             t1.pe,
             t1.pb
      FROM
        (SELECT fund_id,
                date(craw_datetime) craw_daytime,
                craw_datetime,
                price,
                volume,
                increase_rt,
                index_increase_rt,
                estimate_value,
                discount_rt,
                pe,
                pb
         FROM `hedger2`.`etf_detail` partition(${partition}) WHERE craw_datetime>='${day}'
           AND craw_datetime<date_add('${day}', interval 1 DAY)) t1
      JOIN
        (SELECT fund_id,
                date(craw_datetime) craw_daytime,
                max(craw_datetime) max_time
         FROM `hedger2`.`etf_detail` partition(${partition})
         WHERE craw_datetime>='${day}'
           AND craw_datetime<date_add('${day}', interval 1 DAY)
         GROUP BY fund_id,
                  craw_daytime) t2 ON t1.fund_id=t2.fund_id
      AND t1.craw_daytime=t2.craw_daytime
      AND t1.craw_datetime=t2.max_time) t3 ON t1.fund_id=t3.fund_id
   AND t1.craw_daytime=t3.craw_daytime) t ON duplicate KEY
UPDATE trade_date=t.trade_date,
       fund_name=t.fund_name,
       index_id=t.index_id,
       max_price=t.max_price,
       avg_price=t.avg_price,
       min_price=t.min_price,
       open_price=t.open_price,
       close_price=t.close_price,
       volume=t.volume,
       increase_rt=t.increase_rt,
       index_increase_rt=t.index_increase_rt,
       estimate_value=t.estimate_value,
       discount_rt=t.discount_rt,
       pe=t.pe,
       pb=t.pb;