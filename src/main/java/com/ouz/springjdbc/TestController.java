package com.ouz.springjdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
  @Autowired SpringJdbcDao springJdbcDao;

  @PostMapping("/")
  public void test() throws SQLException, IOException, ExecutionException, InterruptedException {
    ExecutorService executorService = Executors.newCachedThreadPool();
    Future<?> submit = null;
    for (int i = 0; i < 20; i++) {
      submit =
          executorService.submit(
              () -> {
                springJdbcDao.getAllProducts();
              });
    }
    submit.get();
  }
}
