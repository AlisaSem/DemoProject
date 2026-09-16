package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** Точка входа Spring Boot-приложения. */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients
public class Application {

  /**
   * Запускает приложение.
   *
   * @param args аргументы командной строки
   */
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
