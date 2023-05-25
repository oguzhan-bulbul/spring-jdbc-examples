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
    SpringApplication.run(SpringJdbcApplication.class, args);
  }
}
