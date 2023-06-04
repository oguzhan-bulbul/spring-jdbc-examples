package ouz.springjdbc;

import com.ouz.springjdbc.Product;
import com.ouz.springjdbc.Region;
import com.ouz.springjdbc.SpringJdbcDao;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {com.ouz.springjdbc.SpringJdbcApplication.class})
class SpringJdbcApplicationTests {

  @Autowired private SpringJdbcDao springJdbcDao;

  @Test
  void testInsertRegion() {

    springJdbcDao.insertRegion(8, "Marmara");
  }

  @Test
  void testUpdateRegion() {

    springJdbcDao.updateRegion(1, "Kuymak");
  }

  @Test
  void testDeleteRegion() {

    springJdbcDao.deleteProductById(97);
  }

  @Test
  void testGetProductCountByCategoryId() {

    int productCountByCategoryId = springJdbcDao.getProductCountByCategoryId(1);
    System.out.println(productCountByCategoryId);
  }

  @Test
  void testGetProductNameByCategoryId() {

    String productNameByProductId = springJdbcDao.getProductNameByProductId(11);
    System.out.println(productNameByProductId);
  }

  @Test
  void testGetProductByProductId() {

    Product productByProductId = springJdbcDao.getProductByProductId(11);
    System.out.println(productByProductId);
  }

  @Test
  void testGetAllProducts() {

    List<Product> allProducts = springJdbcDao.getAllProducts();
    allProducts.forEach(product -> System.out.println(product.getProductName()));
  }

  @Test
  void testGetAllProductsBySupplierIdWithPreparedStatement() {

    List<Product> allProducts = springJdbcDao.getAllProductsBySupplierIdWithPreparedStatement();
    allProducts.forEach(product -> System.out.println(product.getProductName()));
  }

  @Test
  void testRunSql() {

    springJdbcDao.runSql("UPDATE PRODUCTS SET product_name = 'Trabzonspor' where product_id = 2 ");
  }

  @Test
  void testGetAllProductsWithNamedJdbc() {

    List<Product> allProductsBySupplierIdWithNamedJdbc =
        springJdbcDao.getAllProductsBySupplierIdWithNamedJdbc();

    allProductsBySupplierIdWithNamedJdbc.forEach(
        product -> System.out.println(product.getProductName()));
  }

  @Test
  void testBatchUpdateRegionDescByRegionIdWithJdbc() {

    List<Region> regions = new ArrayList<>();
    regions.add(new Region(1, "AAAAAAAA"));
    regions.add(new Region(2, "BBBBBBBBB"));
    regions.add(new Region(3, "CCCCCCCCC"));

    Integer totalEffectedRow = springJdbcDao.batchUpdateRegionDescByRegionIdWithJdbc(regions);
    System.out.println(totalEffectedRow);
  }

  @Test
  void testBatchUpdateRegionDescByRegionIdWithNamedJdbc() {

    List<Region> regions = new ArrayList<>();
    regions.add(new Region(1, "Trabzon"));
    regions.add(new Region(2, "Arakli"));
    regions.add(new Region(3, "Arsin"));
    regions.add(new Region(4, "Arsin"));

    Integer totalEffectedRow = springJdbcDao.batchUpdateRegionDescByRegionIdWithNamedJdbc(regions);
    System.out.println(totalEffectedRow);
  }

  @Test
  void testBatchUpdateRegionDescByRegionIdWithMultipleBatches() {

    List<Region> regions = new ArrayList<>();
    regions.add(new Region(1, "Vakfikebir"));
    regions.add(new Region(2, "Akcaabat"));
    regions.add(new Region(3, "Besikduzu"));
    regions.add(new Region(4, "Hamsikoy"));

    springJdbcDao.batchUpdateRegionDescByRegionIdWithMultipleBatches(regions);
  }

  @Test
  void testInsertRegionWithSimpleJdbcInsert() {

    Integer effectedRowCount = springJdbcDao.insertRegionWithSimpleJdbcInsert(11, "Trabzon");
    System.out.println(effectedRowCount);
  }

  @Test
  void testGetProductsBetweenDateFunction() {

    Integer orderCounts =
        springJdbcDao.getProductsBetweenDateFunction(
            Date.valueOf("1996-07-08"), Date.valueOf("1996-07-20"));
    System.out.println(orderCounts);
  }

  @Test
  void testGetRegionByRegionId() {

    Region regionByRegionId = springJdbcDao.getRegionByRegionId(1);
    System.out.println(
        regionByRegionId.getRegionId() + " : " + regionByRegionId.getRegionDescription());
  }

  @Test
  void testGetAllProductsWithResultSetExtractor() {

    List<Product> allProductsWithResultSetExtractor =
        springJdbcDao.getAllProductsWithResultSetExtractor();
    allProductsWithResultSetExtractor.forEach(System.out::println);
  }

  @Test
  public void testGetOrderCountBetweenDatesWithStoredProcedure() {
    Integer orderCountBetweenDatesWithStoredProcedure = springJdbcDao.getOrderCountBetweenDatesWithStoredProcedure(
        Date.valueOf("1996-07-08"), Date.valueOf("1996-07-20"));
    System.out.println(orderCountBetweenDatesWithStoredProcedure);
  }

  @Test
  public void testGetProductsBetweenDateProcedure() {
    Integer orderCountBetweenDatesWithStoredProcedure = springJdbcDao.getProductsBetweenDateProcedure(
        Date.valueOf("1996-07-08"), Date.valueOf("1996-07-20"));
    System.out.println(orderCountBetweenDatesWithStoredProcedure);
  }

  @Test
  void testUpdateProductWithSqlUpdate() {
    int effectedRow = springJdbcDao.updateRegionWithSqlUpdate(1, "Karadeniz");
    System.out.println(effectedRow);
  }

  @Test
  void testInsertAdvancedData() throws SQLException, IOException {

    springJdbcDao.insertAdvancedData();
  }

  @Test
  void testReadAdvancedData() throws SQLException, IOException {

    springJdbcDao.readAdvancedData();
  }

}
