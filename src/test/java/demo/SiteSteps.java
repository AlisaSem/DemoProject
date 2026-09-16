package demo;

import static demo.steps.WebDriverRunner.find;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import demo.database.dao.UsersDao;
import demo.steps.WebDriverRunner;
import io.qameta.allure.Step;
import jdk.jfr.Name;
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

@SpringBootTest
public class SiteSteps {

  private static final Logger log = LoggerFactory.getLogger(SiteSteps.class);
  private static final String REGISTRATION_EMAIL = "testreg@example.ru";
  private static final String INVITED_USER_EMAIL = "test2@example.ru";
  private static final String AUTH_INVITED_USER_EMAIL = "test3@example.ru";

  private final UsersDao usersDao;

  static WebDriver driver1 = new ChromeDriver();
  static WebDriver driver2 = new ChromeDriver();

  @Autowired
  SiteSteps(UsersDao usersDao) {
    this.usersDao = usersDao;
  }

  @BeforeEach
  public void enterSite() {
    usersDao.deleteUsers(REGISTRATION_EMAIL);
    usersDao.deleteUsers(INVITED_USER_EMAIL);
    usersDao.deleteUsers(AUTH_INVITED_USER_EMAIL);
    WebDriverRunner.runDriver(driver1, "https://example.ru");
    WebDriverRunner.runDriver(driver2, "https://example.ru");
    log.info("Сайт открыт");
  }

  @DisplayName("Регистрация пользователя")
  @Description("Проверка регистрации пользователя")
  @Severity(SeverityLevel.CRITICAL)
  @Step("Регистрация пользователя")
  @Test
  public void regOnSite() {
    var nick = "testreg";
    var password = "testregDemo";

    WebDriverRunner.changeLang(driver1);
    WebDriverRunner.openReg(driver1);
    WebDriverRunner.editMailForReg(driver1, REGISTRATION_EMAIL);
    WebDriverRunner.clickSendCode(driver1);

    String code = WebDriverRunner.waitForDbConfirmationCode(usersDao, REGISTRATION_EMAIL);
    WebDriverRunner.enterConfirmationCode(driver1, code);
    WebDriverRunner.clickRegistration(driver1);

    WebDriverRunner.checkRegistrationFields(driver1, nick, password, password);
    WebDriverRunner.clickRegistration(driver1);

    Assertions.assertThat(find(driver1, "//*[@id='profile_user_name']").getText())
        .as("После регистрации в профиле должен отображаться ник")
        .isEqualTo(nick);
  }

  @Name("Регистрация")
  @Step("Проверка входа по приглашению незарегистрированного пользователя")
  @Test
  public void enterUser() {
    var email = "test1@example.ru";
    var password = " test1Demo ";
    WebDriverRunner.changeLang(driver1);
    WebDriverRunner.enterInSite(driver1);
    WebDriverRunner.editMail(driver1, email);
    WebDriverRunner.editPassword(driver1, password);
    WebDriverRunner.enterAuthUser(driver1);
    var emailForInvitationUser = INVITED_USER_EMAIL;
    WebDriverRunner.addUserInTeam(driver1, emailForInvitationUser);
    var mailCode = usersDao.getInvitationUser(emailForInvitationUser);
    var getCode = mailCode.get("invitation_uid").toString();
    var uriForInvitation = "https://example.ru/i/" + getCode;
    WebDriverRunner.runDriver(driver2, uriForInvitation);
    var nick = "test2";
    var passwordForUser2 = "test2Demo";
    var repeatPasswordForUser2 = "test2Demo";
    WebDriverRunner.checkRegistrationFields(
        driver2, nick, passwordForUser2, repeatPasswordForUser2);
    WebDriverRunner.clickRegistration(driver2);
    WebDriverRunner.clickAccept(driver2);
    Assertions.assertThat(find(driver2, "//*[@id='profile_user_name']").getText()).isEqualTo(nick);
  }

  @Name("Регистрация")
  @Test
  @Step("Проверка приглашения в команду зарегистрированного пользователя")
  public void inviteAuthUser() throws InterruptedException {
    var email = "test1@example.ru";
    var password = " test1Demo ";
    var emailForInvitationUser = AUTH_INVITED_USER_EMAIL;
    var nick = "test3";
    var passwordForInvitationUser = "test3Demo";
    var repeatPassword = "test3Demo";
    WebDriverRunner.enterInSite(driver1);
    WebDriverRunner.editMail(driver1, email);
    WebDriverRunner.editPassword(driver1, password);
    WebDriverRunner.enterAuthUser(driver1);
    WebDriverRunner.openReg(driver2);
    WebDriverRunner.editMailForReg(driver2, emailForInvitationUser);
    WebDriverRunner.clickRegistration(driver2);
    WebDriverRunner.getCodeFromMail(driver2, usersDao, emailForInvitationUser);
    WebDriverRunner.clickRegistration(driver2);
    WebDriverRunner.checkRegistrationFields(
        driver2, nick, passwordForInvitationUser, repeatPassword);
    WebDriverRunner.clickRegistration(driver2);
    WebDriverRunner.addUserInTeam(driver1, emailForInvitationUser);
    var mailCode = usersDao.getInvitationUser(emailForInvitationUser);
    var getCode = mailCode.get("invitation_uid").toString();
    var uriForInvitation = "https://example.ru/jjj/" + getCode;
    WebDriverRunner.runDriver(driver2, uriForInvitation);
    WebDriverRunner.clickAccept(driver2);
    Assertions.assertThat(find(driver1, "//*[@id='select_org']").getText())
        .as("После приглашения пользователь должен видеть новую организацию")
        .isEqualTo(email);
  }
  @Name("Регистрация")
  @Test
  @Step("Негативный тест. Проверка невозможности входа незарегистрированного пользователя")
  public void enterNoAuthUser(){
    var email = "noAuth@example.ru";
    var password = " noAuth643 ";
    WebDriverRunner.enterInSite(driver1);
    WebDriverRunner.editMail(driver1, email);
    WebDriverRunner.editPassword(driver1, password);
    WebDriverRunner.enterAuthUser(driver1);
    String sourceCode = driver1.getPageSource();
    Assertions.assertThat(sourceCode.contains("//*[contains(text(),'Неверный e-mail или пароль!') or contains(text()," +
            "'Invalid email or password!')]"))
        .as("Пользователь не может войти по данному паролю");
  }

  @AfterEach
  void cleanupCreatedUsers() {
    usersDao.deleteUsers(REGISTRATION_EMAIL);
    usersDao.deleteUsers(INVITED_USER_EMAIL);
    usersDao.deleteUsers(AUTH_INVITED_USER_EMAIL);
  }

  @AfterAll
  public static void after() throws InterruptedException {
    WebDriverRunner.quitDriver(driver1);
    WebDriverRunner.quitDriver(driver2);
  }
}
