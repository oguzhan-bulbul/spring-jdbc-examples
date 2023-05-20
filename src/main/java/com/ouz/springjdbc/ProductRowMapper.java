package com.ouz.springjdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 * RowMapper interface'i sql sorgusundan donen ResultSet'in satirlarini mapleyemebilmek icin
 * kullanilir. ResultSetin her bir satirinin donusturulecegi objeyi tasarlar.
 */
public class ProductRowMapper implements RowMapper<Product> {

  @Override
  public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
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
    return product;
  }
}
