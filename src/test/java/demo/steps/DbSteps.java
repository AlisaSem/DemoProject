package demo.steps;

import demo.database.dao.UsersDao;
import io.qameta.allure.Step;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbSteps {
  private static final Logger LOG = LoggerFactory.getLogger(DbSteps.class);

  @Step("Проверить данные в БД")
  public static Map<String, Object> checkData(UsersDao usersDao, String id) {
    var result = usersDao.getUser(id);
    LOG.info("data  - {}", result.get("nick"));
    return result;
  }
}
