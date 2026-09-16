package demo.steps;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DataBaseSteps {

  private final DataBaseCheck dataBaseCheck;

  @Autowired
  DataBaseSteps(DataBaseCheck dataBaseCheck) {
    this.dataBaseCheck = dataBaseCheck;
  }

  @Test
  public void findMail() {
    String mail = "salisa4227@gmail.com";
    dataBaseCheck.findByEmail(mail);
  }
}
