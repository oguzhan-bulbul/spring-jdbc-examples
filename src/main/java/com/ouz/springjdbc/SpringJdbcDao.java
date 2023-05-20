package com.ouz.springjdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class SpringJdbcDao {

  private static final String DELETE_PRODUCT_BY_ID = "DELETE FROM PRODUCTS WHERE PRODUCT_ID = ?";
  public static String GET_ALL_PRODUCTS_SQL = "SELECT * FROM PRODUCTS";
  public static String GET_ALL_PRODUCTS_SQL_NAMED =
      "SELECT * FROM PRODUCTS WHERE SUPPLIER_ID = :supplierId";
  public static String GET_PRODUCTS_BY_SUPPLIER_ID = "SELECT * FROM PRODUCTS WHERE SUPPLIER_ID = ?";
  public static String GET_PRODUCTS_BY_CATEGORY_ID = "SELECT * FROM PRODUCTS WHERE CATEGORY_ID = ?";
  public static String UPDATE_PRODUCT_NAME_BY_ID =
      "UPDATE PRODUCTS SET PRODUCT_NAME = ? WHERE PRODUCT_ID = ?";

  public static String INSERT_REGION_SQL =
      "INSERT INTO public.region (region_id, region_description)VALUES(?, ?)";

  @Autowired
  @Qualifier("hikariDataSourceV1")
  private DataSource dataSource;

  @Autowired
  @Qualifier("jdbcTemplateWithHikariDataSource")
  private JdbcTemplate jdbcTemplate;

  @Autowired
  @Qualifier("namedJdbcTemplateWithHikariDataSource")
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  /**
   * Temel anlamda kullanilan insert,update,delete islemlerini Spring JDBC ile asagidaki gibi
   * kolayca yapabiliriz.Kullanilan resourcelari kapama veya exception handling gibi konular Spring
   * JDBC Framework tarafindan halledilir.
   */
  public void insertRegion(Integer regionId, String regionDescription) {
    jdbcTemplate.update(INSERT_REGION_SQL, regionId, regionDescription);
  }

  public void updateRegion(Integer productId, String productName) {
    jdbcTemplate.update(UPDATE_PRODUCT_NAME_BY_ID, productId, productName);
  }

  public void deleteProductById(Integer productId) {
    jdbcTemplate.update(DELETE_PRODUCT_BY_ID, productId);
  }

  /** Querying SELECT Statements */
  public int getProductCountByCategoryId(int id) {
    return jdbcTemplate.queryForObject(
        "SELECT count(*) from products where category_id = ?", Integer.class, id);
  }

  public String getProductNameByProductId(int id) {
    return jdbcTemplate.queryForObject(
        "SELECT product_name from products where product_id = ?", String.class, id);
  }

  public Product getProductByProductId(int id) {
    return jdbcTemplate.queryForObject(
        "SELECT * FROM PRODUCTS WHERE PRODUCT_ID = ?", new ProductRowMapper(), id);
  }

  public List<Product> getAllProducts() {

    return jdbcTemplate.query(GET_ALL_PRODUCTS_SQL, new ProductRowMapper());
  }

  public List<Product> getAllProductsBySupplierIdWithPreparedStatement() {
    return jdbcTemplate.query(
        GET_PRODUCTS_BY_SUPPLIER_ID, ps -> ps.setInt(1, 8), new ProductRowMapper());
  }

  /**
   * execute() metodu ile herhangi bir sql ifadesini cagirabiliriz. Fakat bu method genellikle DDL
   * gibi operasyonlari yapmak icin kullanilir.
   */
  public void runSql(String sql) {
    jdbcTemplate.execute(sql);
  }

  public List<Product> getAllProductsBySupplierIdWithNamedJdbc() {

    Map<String, Object> parameterMap = new HashMap<>();
    parameterMap.put("supplierId", 8);

    Map<String, Integer> parameterMap2 = Collections.singletonMap("supplierId", 8);

    SqlParameterSource parameterSource = new MapSqlParameterSource("supplierId", 8);

    return namedParameterJdbcTemplate.query(
        "SELECT * FROM PRODUCTS WHERE SUPPLIER_ID = :supplierId",
        parameterMap,
        new ProductRowMapper());

    //    return namedParameterJdbcTemplate.query(
    //        "SELECT * FROM PRODUCTS WHERE SUPPLIER_ID = :supplierId",
    //        parameterSource,
    //        new ProductRowMapper());
  }

  /**
   * BATCH Process
   *
   * @return
   */
  public Integer batchUpdateRegionDescByRegionIdWithJdbc(List<Region> regions) {

    int[] effectedRowsCountForEveryQuery =
        jdbcTemplate.batchUpdate(
            "UPDATE REGION SET region_description = ? where region_id =?",
            new BatchPreparedStatementSetter() {
              @Override
              public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, regions.get(i).getRegionDescription());
                ps.setInt(2, regions.get(i).getRegiondId());
              }

              @Override
              public int getBatchSize() {
                return regions.size();
              }
            });

    int totalEffectedRow = 0;
    for (int effectedRow : effectedRowsCountForEveryQuery) {
      totalEffectedRow += effectedRow;
    }
    return totalEffectedRow;
  }

  public Integer batchUpdateRegionDescByRegionIdWithNamedJdbc(List<Region> regions) {
    int[] effectedRowsCountForEveryQuery =
        namedParameterJdbcTemplate.batchUpdate(
            "UPDATE REGION SET region_description = :regionDescription where region_id = :regionId",
            SqlParameterSourceUtils.createBatch(regions));

    int totalEffectedRow = 0;
    for (int effectedRow : effectedRowsCountForEveryQuery) {
      totalEffectedRow += effectedRow;
    }
    return totalEffectedRow;
  }

  // Belirli batch size vererek birden fazla batch islemi ayni anda yapabiliriz.
  public void batchUpdateRegionDescByRegionIdWithMultipleBatches(List<Region> regions) {
    jdbcTemplate.batchUpdate(
        "UPDATE REGION SET region_description = ? where region_id =?",
        regions,
        2,
        (ps, region) -> {
          ps.setString(1, region.getRegionDescription());
          ps.setInt(2, region.getRegiondId());
        });
  }

  /**
   * SimpleJdbcInsert sinifi, bir insert sorgusunu daha kolay yapabilmemiz adina metadata
   * bilgilerinin tutuldugu bir siniftir.
   */
  public Integer insertRegionWithSimpleJdbcInsert(Integer regionId, String regionCode) {

    SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("REGION");
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("region_id", 6);
    parameters.put("region_description", "Karadeniz");

    return simpleJdbcInsert.execute(parameters);
  }

  /**
   * SimpleJdbcCall sinifi bize tekrar kullanilabilir,multi-threaded bir sekilde calisan ve DB'de
   * stored procedure veya function cagirmamiza saglayan siniftir.
   */
  public Integer getProductsBetweenDateFunction(Date date1, Date date2) {
    SimpleJdbcCall simpleJdbcCall =
        new SimpleJdbcCall(dataSource).withFunctionName("get_how_many_orders_between_dates");

    SqlParameterSource in =
        new MapSqlParameterSource().addValue("date1", date1).addValue("date2", date2);

    return simpleJdbcCall.executeFunction(Integer.class, in);
  }

  /**
   * TODO : Postgresql de procedure cagirma problemi var.
   *
   * @param date1
   * @param date2
   * @return
   */
  public Integer getProductsBetweenDateProcedure(Date date1, Date date2) {
    SimpleJdbcCall simpleJdbcCall =
        new SimpleJdbcCall(dataSource)
            .withProcedureName("get_how_many_orders_between_dates_procedure")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("date1", Types.DATE),
                new SqlParameter("date1", Types.DATE),
                new SqlOutParameter("orderCount", Types.INTEGER));

    SqlParameterSource in =
        new MapSqlParameterSource().addValue("date1", date1).addValue("date2", date2);

    Map<String, Object> result = simpleJdbcCall.execute(in);

    return (Integer) result.get("returnvalue");
  }

  /**
   * JDBC Operasyonlarini Java Objeleri olarak modelleme SqlQuery MappingSqlQuery SqlUpdate
   * StoredProcedure
   */

  /**
   * SqlQuery , tekrar tekrar kullanilabilir , thread-safe ve sql ifadesine karsilik gelen bir
   * siniftir. SqlQuery sinifi direk olarak nadiren kullanilir. Cunku MappingSqlQuery sinifi daha
   * uygun bir impelementasyondur.
   */
  public Region getRegionByRegionId(int id) {
    RegionMappingQuery regionMappingQuery = new RegionMappingQuery(dataSource);
    return regionMappingQuery.findObject(id);
  }

  /**
   * ResultSetExtractor Sql ifadesinden donen ResultSet uzerinden islem yapmak icin kullanilan
   * interfacedir. Bu interface genellikle Spring JDBC Frameworkunun kendisi tarafindan kullanilir.
   * KUllanicilar icin RowMapper sinifi daha basit ve etkili yontem ResulSet'i islemek icin.
   */
  public List<Product> getAllProductsWithResultSetExtractor() {
    return jdbcTemplate.query(
        GET_ALL_PRODUCTS_SQL,
        rs -> {
          List<Product> productList = new ArrayList<>();
          while (rs.next()) {
            Product product = new Product();
            product.setProductId(rs.getInt(1));
            product.setProductName(rs.getString(2));
            product.setSupplierId(rs.getInt(3));
            product.setCategoryId(rs.getInt(4));
            product.setQuantityPerUnit(rs.getString(5));
            product.setUnitPrice(rs.getInt(6));
            product.setUnitsInStock(rs.getInt(7));
            product.setUnitsOnOrder(rs.getInt(8));
            product.setReOrderLevel(rs.getInt(9));
            product.setDisContinued(rs.getInt(10));
            productList.add(product);
          }
          return productList;
        });
  }

  /**
   * SqlUpdate sinifi bize tekrar kullanilabilen bir sql update statement'i saglar. Bu sinif thread
   * safe bir siniftir
   */
  public int updateProductWithSqlUpdate(int regionId, String regionDescription) {
    UpdateRegionName updateRegionName = new UpdateRegionName(dataSource);
    return updateRegionName.execute(regionId, regionDescription);
  }

  /**
   * //TODO : Procedure cagirimi problemini duzelt StoredProcedure sinifi tekrar kullanilabilen ve
   * DB ortaminda bulunan stored procedure nesnelerini java nesnelestirmek icin kullanilan siniftir.
   */
  public Integer getOrderCountBetweenDatesWithStoredProcedure(Date date1, Date date2) {
    GetOrdersCountBetweenDateProcedure getOrderCountBetweenDatesProcedure =
        new GetOrdersCountBetweenDateProcedure(dataSource);

    List<SqlParameter> sqlParameters =
        List.of(new SqlParameter("date1", Types.DATE), new SqlParameter("date2", Types.DATE));
    jdbcTemplate.call(
        con -> {
          CallableStatement callableStatement =
              con.prepareCall("{? = call get_how_many_orders_between_dates_procedure(?, ?)}");
          callableStatement.registerOutParameter(1, Types.INTEGER);
          callableStatement.setDate(2, date1);
          callableStatement.setDate(3, date2);

          return callableStatement;
        },
        sqlParameters);
    return getOrderCountBetweenDatesProcedure.execute(date1, date2);
  }
}
