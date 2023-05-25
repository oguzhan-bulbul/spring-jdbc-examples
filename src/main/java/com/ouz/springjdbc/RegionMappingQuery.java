package com.ouz.springjdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.sql.DataSource;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;

public class RegionMappingQuery extends MappingSqlQuery<Region> {

  public RegionMappingQuery(DataSource ds) {
    super(ds, "SELECT * FROM region where region_id = ?");
    declareParameter(new SqlParameter("regiond_id", Types.INTEGER));
    compile();
  }

  @Override
  protected Region mapRow(ResultSet rs, int rowNum) throws SQLException {
    Region region = new Region();
    region.setRegionId(rs.getInt("region_id"));
    region.setRegionDescription(rs.getString("region_description"));
    return region;
  }
}
