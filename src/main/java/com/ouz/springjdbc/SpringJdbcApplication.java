package com.ouz.springjdbc;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringJdbcApplication {

  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(SpringJdbcApplication.class, args);

    SpringJdbcDao springJdbcDao = (SpringJdbcDao) ctx.getBean("springJdbcDao");
    //    List<Product> allProducts = productDao.getAllProductsWithNamedJdbc();
    //    allProducts.forEach(product -> System.out.println(product.getProductName()));

    //    productDao.insertRegion(5, "Turkey");

//    Integer productsBetweenDate =
//        springJdbcDao.getOrderCountBetweenDatesWithStoredProcedure(
//            Date.valueOf("1996-07-08"), Date.valueOf("1996-07-10"));
//    System.out.println(productsBetweenDate);

    //    springJdbcDao.getRegionByRegionId(2);

    try {
//      springJdbcDao.insertAdvancedData();
      springJdbcDao.readAdvancedData();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
