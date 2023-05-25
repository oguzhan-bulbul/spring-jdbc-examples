package com.ouz.springjdbc;

import java.sql.Types;
import javax.sql.DataSource;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

public class UpdateRegionName extends SqlUpdate {

  public UpdateRegionName(DataSource dataSource) {
    setDataSource(dataSource);
    setSql("UPDATE REGION SET region_description = ? WHERE region_id = ?");
    declareParameter(new SqlParameter("region_description", Types.VARCHAR));
    declareParameter(new SqlParameter("region_id", Types.INTEGER));
    compile();
  }

  public int execute(int regionId, String regionDescription) {
    return update(regionDescription, regionId);
  }
}
