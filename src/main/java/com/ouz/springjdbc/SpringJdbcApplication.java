package com.ouz.springjdbc;

import java.sql.Date;
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

    Integer productsBetweenDate = springJdbcDao.updateRegionWithProcedure(2,"Arsin");
    System.out.println(productsBetweenDate);

//    springJdbcDao.getRegionByRegionId(2);
  }
}
