package demo.database;

import com.zaxxer.hikari.HikariDataSource;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

/** Конфигурация JDBC-шаблонов для тестовой БД. */
@Configuration
public class DatabaseConfiguration {

  private final DatabaseConnectionConfiguration databaseConnectionConfiguration;

  /**
   * Создаёт конфигурацию на основе свойств подключения.
   *
   * @param databaseConnectionConfiguration параметры источников данных
   */
  public DatabaseConfiguration(DatabaseConnectionConfiguration databaseConnectionConfiguration) {
    this.databaseConnectionConfiguration = databaseConnectionConfiguration;
  }

  /**
   * JDBC-шаблон основной базы demo.
   *
   * @return шаблон запросов
   */
  @Bean
  @Lazy
  public JdbcTemplate demoDb() {
    return getDb("demoDb");
  }

  /**
   * Создаёт JDBC-шаблон для указанного сервиса.
   *
   * @param serviceName имя сервиса в конфигурации
   * @return шаблон запросов
   */
  public JdbcTemplate getDb(String serviceName) {
    return databaseConnectionConfiguration.getDatasources().stream()
        .findFirst()
        .map(this::createDataSource)
        .map(dataSource -> new JdbcTemplate((DataSource) dataSource, true))
        .orElseThrow(() -> new RuntimeException("Сервис с именем '" + serviceName + "' не найден"));
  }

  private DataSource createDataSource(DbArch dbArch) {
    String enrichedUrl;
    String user = "demo_user";

    String passwordDb =
        String.valueOf(
            Optional.ofNullable(dbArch.getPassword())
                .orElseGet(() -> databaseConnectionConfiguration.getDefaultConfig().getPassword()));

    String portDb =
        String.valueOf(
            Optional.ofNullable(dbArch.getPort())
                .orElseGet(() -> databaseConnectionConfiguration.getDefaultConfig().getPort()));

    enrichedUrl = dbArch.getUrl().replace("${port}", portDb);

    try {
      return DataSourceBuilder.create()
          .url(enrichedUrl)
          .username(user)
          .password(passwordDb)
          .type(HikariDataSource.class)
          .build();
    } catch (Exception e) {
      throw new RuntimeException("Ошибка при создании DataSource: " + e.getMessage(), e);
    }
  }
}
