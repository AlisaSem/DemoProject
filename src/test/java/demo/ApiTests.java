package demo;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiTests {

  static final Logger log = LoggerFactory.getLogger(ApiTests.class);

  private static final String BASE_URL = "https://example.ru/api/auth/signin";

  @BeforeEach
  public void setUp() {
    RestAssured.baseURI = BASE_URL;
    RestAssured.replaceFiltersWith(new AllureRestAssured());
  }

  @Test
  public void negativeTestAutorization() {
    String signIn =
        """
        {"email":"mePost@example.ru",
        "password":"testDemo",
        "invitationUid":null,
        "lsUid":"f2a6dd12-5b64-4566-92d3-d85345011165",
        "rememberMe":false}
        """;

    Response response =
        RestAssured.given().contentType(ContentType.JSON).body(signIn).header("Instance-Uid"
                ,"7c4b8be7-37ec-41f6-ae3b-4a2e5c5a287b")
            .post(BASE_URL);

    log.info(response.asString());
    Assertions.assertEquals(401, response.getStatusCode());
  }
}
