package demo.steps;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MailReaderTest {

  @Test
  void extractConfirmationCodeFromexampleHtmlEmail() {
    String html =
        """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; color: #222; line-height: 1.5;">
              <p>You are registering in example.</p>
              <p>Use this confirmation code:</p>
              <p style="margin: 24px 0; font-size: 28px; font-weight: bold;">698748</p>
              <p>The code is valid for 10 minutes.</p>
              <p>If you did not start registration, simply ignore this email.</p>
            </body>
            </html>
            """;

    assertThat(MailReader.extractConfirmationCode(html)).isEqualTo("698748");
  }

  @Test
  void extractConfirmationCodeFromRussianHtmlEmail() {
    String html =
        """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; color: #222; line-height: 1.5;">
              <p>Вы регистрируетесь в example.</p>
              <p>Используйте этот код подтверждения:</p>
              <p style="margin: 24px 0; font-size: 28px; font-weight: bold;">514623</p>
              <p>Код действует 10 минут.</p>
              <p>Если вы не начинали регистрацию, просто проигнорируйте это письмо.</p>
            </body>
            </html>
            """;

    assertThat(MailReader.extractConfirmationCode(html)).isEqualTo("514623");
  }

  @Test
  void extractConfirmationCodeFromPlainText() {
    String text =
        "You are registering in example. Use this confirmation code: 123456 The code is valid"
            + " for 10 minutes.";
    assertThat(MailReader.extractConfirmationCode(text)).isEqualTo("123456");
  }

  @Test
  void extractRestoreAccessLink() {
    String html =
        """
            <!DOCTYPE html>
              <html>
              <body style="font-family: Arial, sans-serif; color: #222; line-height: 1.5;">
                <p>Вы запросили восстановление пароля в example.</p>
                 <p>Нажмите кнопку ниже, чтобы задать новый пароль:</p>
                  <p style="margin:24px 0">
                  <a href="https://example.ru/password_reset/31b8ed10-beec-4938-8451-d5226c3b75d0"
                  style="background:#2e7d32;color:#fff;padding:12px 20px;text-decoration:none;border-radius:8px;display:inline-block"
                  target="_blank" data-saferedirecturl="https://www.google.com/url?q=https://example.ru/password_reset/31b8ed10-beec-4938-8451-d5226c3b75d0&amp;
                  source=gmail&amp;ust=1789293296856000&amp;usg=AOvVaw381IJGEPvYK5kaCx98yajj">Восстановить доступ</a>
                  </p>
            </body>
            </html>
            """;
    assertThat(MailReader.extractConfirmationCode(html)).isEqualTo("//example.ru/password_reset/31b8ed10-beec-4938-8451-d5226c3b75d0");
  }
}
