package com.ouz.springjdbc;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
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
    jdbcTemplate.update(UPDATE_PRODUCT_NAME_BY_ID, productName, productId);
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
                ps.setInt(2, regions.get(i).getRegionId());
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
          ps.setInt(2, region.getRegionId());
        });
  }

  /**
   * SimpleJdbcInsert sinifi, bir insert sorgusunu daha kolay yapabilmemiz adina metadata
   * bilgilerinin tutuldugu bir siniftir.
   */
  public Integer insertRegionWithSimpleJdbcInsert(Integer regionId, String regionDescription) {

    SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("REGION");
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("region_id", regionId);
    parameters.put("region_description", regionDescription);

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
   */
  public Integer getProductsBetweenDateProcedure(Date date1, Date date2) {
    SimpleJdbcCall simpleJdbcCall =
        new SimpleJdbcCall(dataSource)
            .withProcedureName("get_how_many_orders_between_dates_procedure")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(new SqlParameter("date1", Types.DATE))
            .declareParameters(new SqlParameter("date2", Types.DATE))
            .declareParameters(new SqlOutParameter("orderCount",Types.INTEGER));
    int orderCount = 0;
    SqlParameterSource in =
        new MapSqlParameterSource().addValue("date1", date1).addValue("date2", date2).addValue("orderCount",orderCount);

    Map<String, Object> result = simpleJdbcCall.execute(in);

    return (Integer) result.get("orderCount");
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
          try {
            Thread.sleep(10000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
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
   * SqlUpdate sinifi bize tekrar kullanilabilen bir sql update statement'i saglar. Bu sinif thread
   * safe bir siniftir
   */
  public int updateRegionWithSqlUpdate(int regionId, String regionDescription) {
    UpdateRegionName updateRegionName = new UpdateRegionName(dataSource);
    return updateRegionName.execute(regionId, regionDescription);
  }

  /**
   * //TODO : Procedure icin sql olusturulurken postgresql ile alakali {} sorunu var.
   * DB ortaminda bulunan stored procedure nesnelerini java nesnelestirmek icin kullanilan siniftir.
   */
  public Integer getOrderCountBetweenDatesWithStoredProcedure(Date date1, Date date2) {
    GetOrdersCountBetweenDateProcedure getOrderCountBetweenDatesProcedure =
        new GetOrdersCountBetweenDateProcedure(dataSource);

    List<SqlParameter> sqlParameters =
        List.of(
            new SqlParameter("date1", Types.DATE),
            new SqlParameter("date2", Types.DATE),
            new SqlOutParameter("orderCount",Types.INTEGER));
    Map<String, Object> call = jdbcTemplate.call(
        con -> {
          CallableStatement callableStatement =
              con.prepareCall("call get_how_many_orders_between_dates_procedure(?, ?,?)");
          callableStatement.registerOutParameter(3, Types.INTEGER);
          callableStatement.setDate(1, date1);
          callableStatement.setDate(2, date2);

          return callableStatement;
        },
        sqlParameters);
//    return (Integer) call.get("orderCount");
    return getOrderCountBetweenDatesProcedure.execute(date1, date2);
  }

  /** Spring JDBC bize clob , blob , xml vb gelismis veri tipleri ile calisabilme imkani saglar. */
  public void insertAdvancedData() throws SQLException, IOException {
    DefaultLobHandler lobHandler = new DefaultLobHandler();
    File txtFile = new File("D:\\PersonalRepo\\springJDBC\\src\\main\\resources\\large.txt");
    File jpgFile = new File("D:\\PersonalRepo\\springJDBC\\src\\main\\resources\\jpegimage.jpg");
    InputStream txtFileInputStream = new FileInputStream(txtFile);

    InputStream jpgFileInputStream = new FileInputStream(jpgFile);

    InputStreamReader clobReader = new InputStreamReader(txtFileInputStream);

    Connection conn = dataSource.getConnection();

    SQLXML sqlxml = conn.createSQLXML();

    Writer writer = sqlxml.setCharacterStream();
    BufferedReader in =
        new BufferedReader(
            new FileReader("D:\\PersonalRepo\\springJDBC\\src\\main\\resources\\products.xml"));
    String xml = null;
    while ((xml = in.readLine()) != null) {
      writer.write(xml);
    }

    jdbcTemplate.execute(
        "INSERT INTO advanced_data_type (clob_data, blob_data, xml_data) VALUES (?, ?, ?)",
        new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
          @Override
          protected void setValues(PreparedStatement ps, LobCreator lobCreator)
              throws SQLException, DataAccessException {

            lobCreator.setClobAsCharacterStream(ps, 1, clobReader, (int) txtFile.length());
            lobCreator.setBlobAsBinaryStream(ps, 2, jpgFileInputStream, (int) jpgFile.length());
            ps.setSQLXML(3, sqlxml);
          }
        });
  }

  public void readAdvancedData() throws SQLException, IOException {
    DefaultLobHandler lobHandler = new DefaultLobHandler();

    jdbcTemplate.query(
        "select * from advanced_data_type",
        (rs, rowNum) -> {
          String clobData = lobHandler.getClobAsString(rs, "clob_data");
          byte[] blobData = lobHandler.getBlobAsBytes(rs, "blob_data");
          SQLXML sqlxml1 = rs.getSQLXML("xml_data");

          String xmlData = sqlxml1.getString();
          System.out.println(clobData);
          System.out.println("==============================================");
          System.out.println(xmlData);

          ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(blobData);
          BufferedImage bufferedImage;
          try {
            bufferedImage=ImageIO.read(byteArrayInputStream);
            ImageIO.write(bufferedImage,"jpg",new File("D:\\PersonalRepo\\springJDBC\\src\\main\\resources\\newFile.jpg"));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          return "resultMap";
        });
  }
}
