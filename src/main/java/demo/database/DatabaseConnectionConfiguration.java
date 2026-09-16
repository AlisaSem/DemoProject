package demo.database;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/** Параметры подключения к БД из {@code database.properties}. */
@Configuration
@Component
@ConfigurationProperties(prefix = "db")
@PropertySource(value = "classpath:database.properties", encoding = "UTF-8")
public class DatabaseConnectionConfiguration {
  private List<DbArch> datasources;
  private DbDefaultConfig defaultConfig;

  public List<DbArch> getDatasources() {
    return datasources;
  }

  public void setDatasources(List<DbArch> datasources) {
    this.datasources = datasources;
  }

  public DbDefaultConfig getDefaultConfig() {
    return defaultConfig;
  }

  public void setDefaultConfig(DbDefaultConfig defaultConfig) {
    this.defaultConfig = defaultConfig;
  }

  /** Значения по умолчанию для порта и пароля. */
  public static class DbDefaultConfig {
    private String password;
    private String port;

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getPort() {
      return port;
    }

    public void setPort(String port) {
      this.port = port;
    }
  }
}
