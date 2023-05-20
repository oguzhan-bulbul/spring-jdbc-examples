package com.ouz.springjdbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Product {
  private int productId;
  private String productName;
  private int supplierId;
  private int categoryId;
  private String quantityPerUnit;
  private int unitPrice;
  private int unitsInStock;
  private int unitsOnOrder;
  private int reOrderLevel;
  private int disContinued;
}
