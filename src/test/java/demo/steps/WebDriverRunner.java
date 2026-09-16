package demo.steps;

import static org.awaitility.Awaitility.await;

import demo.database.dao.UsersDao;
import com.jasongoodwin.monads.Try;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Step;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDriverRunner {
  private static final Logger LOG = LoggerFactory.getLogger(DbSteps.class);
  private static final String RESOURCES_ROOT_PATH = "src/test/resources";

  static Duration duration = Duration.ofSeconds(20);

  // static WebDriver driver = new ChromeDriver();

  public static void runDriver(WebDriver driver, String uri) {
    WebDriverManager.chromedriver().setup();
    System.setProperty(
        "webdriver.chrome.driver", "C:\\Users\\salis\\IdeaProjects\\chromedriver.exe");
    driver.manage().window().maximize();
    driver.get(uri);
  }

  public static WebElement find(WebDriver driver, String xpathExpression) {
    WebDriverWait wait = new WebDriverWait(driver, duration);
    return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathExpression)));
  }

  public static WebElement findIn(WebElement parent, String xpathExpression)
      throws InterruptedException {
    for (int i = 0; ; i++) {
      Thread.sleep(i + 200);
      if (i >= 20) {
        throw new InterruptedException();
      }
      try {
        return parent.findElement(By.xpath(xpathExpression));
      } catch (Exception ignored) {
        // element is not in the DOM yet
      }
    }
  }

  public static void openReg(WebDriver driver) {
    find(driver, "//*[contains(text(),'SignUp') or contains(text(),'Регистрация')]").click();
  }

  public static void enterInSite(WebDriver driver) {
    find(driver, "//*[@id='top_menu_enter']").click();
  }

  public static void enterAuthUser(WebDriver driver) {
    find(driver, "//*[@id='to Enter']").click();
  }

  public static void changeLang(WebDriver driver) {
    find(driver, "//button[text()='EN']").click();
  }

  public static void editMail(WebDriver driver, String email) {
    //        find("//*[@id=\"E-mail\"]").sendKeys("salisa4227@gmail.com");
    find(driver, "//*[@id=\"E-mail\"]").sendKeys(email);
  }

  @Step("Ввести email для регистрации")
  public static void editMailForReg(WebDriver driver, String email) {
    find(driver, "//*[@id=\"E-mail\"]").sendKeys(email);
  }

  public static void editPassword(WebDriver driver, String password) {
    find(driver, "//*[@id=\"Password\"]").sendKeys(password);
  }

  @Step("Нажать кнопку регистрации")
  public static void clickRegistration(WebDriver driver) {
    find(driver, "//*[@id=\"SignUp\"]").click();
  }

  @Step("Нажать «Отправить код» на экране регистрации")
  public static void clickSendCode(WebDriver driver) {
    find(driver, "//*[@id=\"SignUp\"]").click();
  }

  @Step("Ввести код подтверждения из письма")
  public static void enterConfirmationCode(WebDriver driver, String code) {
    find(driver, "//*[@id=\"Confirmation code\"]").sendKeys(code);
  }

  public static void clickAccept(WebDriver driver) {
    find(driver, "//*[@id='to Accept']").click();
  }

  @Step("Запросить восстановление пароля")
  public static void clickRestoreAccess(WebDriver driver) {
    find(driver, "//*[@id='restore_access']/div").click();
  }

  @Step("Нажать на кнопку запрос восстановления пароля")
  public static void clickRestoreAccessButton(WebDriver driver) {
    find(driver, "//*[@id='restore_access']").click();
  }

  @Step("Нажать кнопку выход")
  public static void clickLogOut(WebDriver driver){
    find(driver, "//*[@id='lm_logout_label']").click();
  }

  @Step("Дождаться код подтверждения в БД")
  public static String waitForDbConfirmationCode(UsersDao usersDao, String mail) {
    return await()
        .atMost(Duration.ofSeconds(60))
        .pollInterval(Duration.ofSeconds(1))
        .ignoreExceptions()
        .until(() -> usersDao.findConfirmationCode(mail), code -> code != null && !code.isBlank());
  }

  @Step("Проверить код из мейла")
  public static Map<String, Object> getCodeFromMail(
      WebDriver driver, UsersDao usersDao, String mail) {
    var confirmationCodeField = find(driver, "//*[@id=\"Confirmation code\"]");
    var sendCode = waitForDbConfirmationCode(usersDao, mail);
    LOG.info("code  - {}", sendCode);
    confirmationCodeField.sendKeys(sendCode);
    return Map.of("code", sendCode);
  }

  @Step("Проверить, что появились поля регистрации после ввода кода")
  public static void checkRegistrationFields(
      WebDriver driver, String nick, String password, String repeatPassword) {
    find(driver, "//*[@id=\"Nick\"]").sendKeys(nick);
    find(driver, "//*[@id=\"Password\"]").sendKeys(password);
    find(driver, "//*[@id=\"Repeat password\"]").sendKeys(repeatPassword);
  }

  @Step("Ввести новый пароль после восстановления доступа")
      public static void newPasswordAfterReset
      (WebDriver driver, String password, String repeatPassword) {
    find(driver, "//*[@id='Password']").sendKeys(password);
    find(driver, "//*[@id='Repeat password']").sendKeys(repeatPassword);
  }

  @Step("Сохранить новый пароль после восстановления")
  public static void savePasswordAfterReset(WebDriver driver) {
    find(driver, "//*[@id='save_new_password']").click();
  }


  @Step("Добавить участника в команду")
  public static void addUserInTeam(WebDriver driver, String mail) {
    find(driver, "//*[@id='select_org']").click();
    find(driver, "//*[@data-component='PopoverBox']").click();
    find(driver, "//*[@id='employees_btn']").click();
    find(driver, "//*[@id='invite_employee_btn']").click();
    find(driver, "//*[@id='E-mail']").sendKeys(mail);
    find(driver, "//*[@id='Send an Invitation']").click();
  }



  @Step("Сообщения в чате")
  public static void massagesAndFilesInChat(WebDriver driver, String massage)
      throws InterruptedException {
    find(driver, "//*[@id='NewComment']").sendKeys(massage);
    find(driver, "//*[@id='>']").click();
    String massageElement = find(driver, "//*[text()='" + massage + "']").getText();
    Assertions.assertThat(massageElement).isEqualTo(massage);
  }

  private static List<File> getAllFilesInTestResources(String path) {
    return Try.ofFailable(() -> Paths.get(path))
        .map(Files::walk)
        .orElseThrow(
            () -> new RuntimeException("Cant get files in testResources by path - " + path))
        .filter(Files::isRegularFile)
        .map(Path::toFile)
        .collect(Collectors.toList());
  }

  public static void quitDriver(WebDriver driver) throws InterruptedException {
    Thread.sleep(2000);
    driver.quit();
  }
}
