package com.ouz.springjdbc;

import java.sql.Date;
import java.sql.Types;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.object.StoredProcedure;

public class GetOrdersCountBetweenDateProcedure extends StoredProcedure {

  public GetOrdersCountBetweenDateProcedure(DataSource dataSource) {
    setDataSource(dataSource);
    setFunction(false);
    setSql("get_how_many_orders_between_dates_procedure");
    declareParameter(new SqlParameter("date1", Types.DATE));
    declareParameter(new SqlParameter("date2", Types.DATE));
    declareParameter(new SqlOutParameter("orderCount", Types.INTEGER));
    compile();
  }

  public Integer execute(Date date1, Date date2) {

    MapSqlParameterSource parameterSource =
        new MapSqlParameterSource()
            .addValue("date1", date1)
            .addValue("date2", date2);

    Map<String, Object> result = super.execute(date1, date2);


    return (Integer) result.get("orderCount");
  }
}
