package demo;

import demo.database.dao.UsersDao;
import demo.steps.DbSteps;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DataBaseTests {

  private final UsersDao usersDao;

  @Autowired
  DataBaseTests(UsersDao usersDao) {
    this.usersDao = usersDao;
  }

  @Test
  void getId() {
    var id = "1";
    var res = DbSteps.checkData(usersDao, id);
    Assertions.assertThat(res.get("nick")).isEqualTo("Denis");
  }
}
