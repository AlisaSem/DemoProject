package demo;

import static demo.steps.WebDriverRunner.find;

import demo.database.dao.UsersDao;
import demo.steps.MailReader;
import demo.steps.WebDriverRunner;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import java.util.Properties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Feature("Регистрация по email")
@SpringBootTest
public class RegistrationStartTest {

  private static final Logger log = LoggerFactory.getLogger(RegistrationStartTest.class);
  private static final WebDriver driver = new ChromeDriver();
  private final UsersDao usersDao;
  private Properties mailProperties;
  private String email;

  private static final String REGISTRATION_EMAIL = "testreg@example.ru";
  private static final String INVITED_USER_EMAIL = "test2@example.ru";

  @Autowired
  RegistrationStartTest(UsersDao usersDao) {
    this.usersDao = usersDao;
  }

  @BeforeEach
  void openSignUpPage() throws Exception {
    mailProperties = MailReader.loadMailProperties();
    email = mailProperties.getProperty("test.registration.email");
    usersDao.deleteUsers(email);
    usersDao.deleteUsers(REGISTRATION_EMAIL);
    WebDriverRunner.runDriver(driver, "");
    log.info("Страница регистрации открыта");
  }

  @DisplayName("Регистрация пользователя по коду из письма")
  @Description(
      "Пользователь запрашивает код на почту, вводит его и завершает регистрацию ником и паролем")
  @Severity(SeverityLevel.CRITICAL)
  @Test
  void shouldRegisterUserByEmailCode() {
    Assertions.assertThat(email)
        .as("Укажите test.registration.email в src/test/resources/mail.properties")
        .isNotBlank();

    String nick = mailProperties.getProperty("test.registration.nick");
    String password = mailProperties.getProperty("test.registration.password");
    Assertions.assertThat(nick)
        .as("Укажите test.registration.nick в src/test/resources/mail.properties")
        .isNotBlank();
    Assertions.assertThat(password)
        .as("Укажите test.registration.password в src/test/resources/mail.properties")
        .isNotBlank();

    Allure.parameter("email", email);
    Allure.parameter("nick", nick);

    WebDriverRunner.editMailForReg(driver, email);
    WebDriverRunner.clickSendCode(driver);
    log.info("Запрошена отправка кода на {}", email);

    String code = MailReader.waitForConfirmationCode(mailProperties, email);
    log.info("Код подтверждения из письма: {}", code);
    Assertions.assertThat(code)
        .as("Код подтверждения должен быть найден в последнем письме")
        .isNotBlank()
        .matches("\\d{6}");

    WebDriverRunner.enterConfirmationCode(driver, code);
    WebDriverRunner.clickRegistration(driver);

    WebDriverRunner.checkRegistrationFields(driver, nick, password, password);
    WebDriverRunner.clickRegistration(driver);

    Assertions.assertThat(find(driver, "//*[@id='profile_user_name']").getText()).isEqualTo(nick);
  }

  @DisplayName("Восстановление пароля по ссылке из письма")
  @Description("Пользователь получает ссылку для восстановление пароля на почту, кликает на ее и восстанавливает пароль")
  @Severity(SeverityLevel.CRITICAL)
  @Test
  void RestoreAccess() {
    var nick = "testPost";
    var password = "testPostDemo";

    WebDriverRunner.changeLang(driver);
    WebDriverRunner.openReg(driver);
    WebDriverRunner.editMailForReg(driver, email);
    WebDriverRunner.clickSendCode(driver);

    String code = WebDriverRunner.waitForDbConfirmationCode(usersDao, email);
    WebDriverRunner.enterConfirmationCode(driver, code);
    WebDriverRunner.clickRegistration(driver);

    WebDriverRunner.checkRegistrationFields(driver, nick, password, password);
    WebDriverRunner.clickRegistration(driver);
    WebDriverRunner.clickLogOut(driver);

    Assertions.assertThat(email)
        .as("Укажите test.registration.email в src/test/resources/mail.properties")
        .isNotBlank();
    WebDriverRunner.enterInSite(driver);
    WebDriverRunner.clickRestoreAccess(driver);
    WebDriverRunner.editMailForReg(driver, email);
    WebDriverRunner.clickRestoreAccessButton(driver);

    String link = MailReader.waitForLink(mailProperties, email);
    log.info("Cсылка из письма: {}", link);
    driver.get(link);

    WebDriverRunner.newPasswordAfterReset(driver, "resetDemo", "resetDemo");
    WebDriverRunner.savePasswordAfterReset(driver);

    Assertions.assertThat(find(driver, "//*[@id='profile_user_name']").getText())
        .as("После регистрации в профиле должен отображаться ник")
        .isEqualTo(nick);
  }

  @DisplayName("Приглашение в команду незарегистриврованного пользователя по ссылке из письма")
  @Description("Пользователь приглашает в команду ранее не зарегестрированнного пользователя")
  @Severity(SeverityLevel.CRITICAL)
  @Test
  void InvitationUnregisteredUserByEmail() {
    var nick = "testreg";
    var password = "testregDemo";

    WebDriverRunner.changeLang(driver);
    WebDriverRunner.openReg(driver);
    WebDriverRunner.editMailForReg(driver, REGISTRATION_EMAIL);
    WebDriverRunner.clickSendCode(driver);

    String code = WebDriverRunner.waitForDbConfirmationCode(usersDao, REGISTRATION_EMAIL);
    WebDriverRunner.enterConfirmationCode(driver, code);
    WebDriverRunner.clickRegistration(driver);

    WebDriverRunner.checkRegistrationFields(driver, nick, password, password);
    WebDriverRunner.clickRegistration(driver);

    var emailForInvitationUser = INVITED_USER_EMAIL;
    WebDriverRunner.addUserInTeam(driver, emailForInvitationUser);
    WebDriverRunner.clickLogOut(driver);
  }

  @AfterEach
  void deleteRegisteredUser() {
    if (email != null && !email.isBlank()) {
      usersDao.deleteUsers(email);
    }
  }

  @AfterAll
  static void closeBrowser() throws InterruptedException {
    WebDriverRunner.quitDriver(driver);
  }
}
