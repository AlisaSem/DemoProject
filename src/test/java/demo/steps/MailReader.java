package demo.steps;

import static org.awaitility.Awaitility.await;

import io.qameta.allure.Step;
import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MailReader {

  private static final Logger LOG = LoggerFactory.getLogger(MailReader.class);
  private static final Pattern HTML_TAG = Pattern.compile("(?s)<[^>]+>");
  private static final Pattern CONFIRMATION_CODE =
      Pattern.compile(
          "(?:Use this confirmation code|Используйте этот код подтверждения):\\s*(\\d{6})",
          Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private static final Pattern RESTORE_ACCESS =
      Pattern.compile(
          "(?:Click the button below to set a new password"
              + "|Or follow the link"
              + "|Нажмите кнопку ниже, чтобы задать новый пароль"
              + "|Или перейдите по ссылке)"
              + "[^h]*(https?://\\S+)",
          Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  private MailReader() {}

  public static Properties loadMailProperties() throws Exception {
    Properties properties = new Properties();
    try (InputStream inputStream =
        MailReader.class.getClassLoader().getResourceAsStream("mail.properties")) {
      if (inputStream == null) {
        throw new IllegalStateException("Не найден файл mail.properties в src/test/resources");
      }
      properties.load(inputStream);
    }
    return properties;
  }

  @Step("Дождаться письма с кодом подтверждения и прочитать код")
  public static String waitForConfirmationCode(Properties mailProperties, String recipientEmail) {
    long timeoutSeconds =
        Long.parseLong(mailProperties.getProperty("mail.wait.timeout.seconds", "120"));
    long pollIntervalSeconds =
        Long.parseLong(mailProperties.getProperty("mail.wait.poll.interval.seconds", "5"));
    Instant sentAfter = Instant.now().minusSeconds(5);

    return await()
        .atMost(Duration.ofSeconds(timeoutSeconds))
        .pollInterval(Duration.ofSeconds(pollIntervalSeconds))
        .pollDelay(Duration.ofSeconds(2))
        .until(
            () -> readLatestConfirmationCode(mailProperties, recipientEmail, sentAfter),
            code -> code != null && !code.isBlank());
  }

  @Step("Дождаться письма с ссылкой и прочитать его")
  public static String waitForLink(Properties mailProperties, String recipientEmail) {
    long timeoutSeconds =
        Long.parseLong(mailProperties.getProperty("mail.wait.timeout.seconds", "120"));
    long pollIntervalSeconds =
        Long.parseLong(mailProperties.getProperty("mail.wait.poll.interval.seconds", "5"));
    Instant sentAfter = Instant.now().minusSeconds(5);

    return await()
        .atMost(Duration.ofSeconds(timeoutSeconds))
        .pollInterval(Duration.ofSeconds(pollIntervalSeconds))
        .pollDelay(Duration.ofSeconds(2))
        .until(
            () -> readLatestLinkToRestoreAccess(mailProperties, recipientEmail, sentAfter),
            href -> href != null && !href.isBlank());
  }

  private static String readLatestConfirmationCode(
      Properties mailProperties, String recipientEmail, Instant sentAfter) {
    Store store = null;
    Folder inbox = null;
    try {
      Session session = Session.getInstance(buildImapProperties(mailProperties));
      store = session.getStore("imap");
      store.connect(
          mailProperties.getProperty("mail.imap.host"),
          993,
          mailProperties.getProperty("mail.imap.username"),
          mailProperties.getProperty("mail.imap.password"));

      inbox = store.getFolder(mailProperties.getProperty("mail.imap.folder", "INBOX"));
      inbox.open(Folder.READ_ONLY);

      Message latestMessage = findLatestMessage(inbox, recipientEmail, sentAfter);
      if (latestMessage == null) {
        LOG.info("Письмо с кодом для {} пока не найдено", recipientEmail);
        return null;
      }

      String subject = latestMessage.getSubject();
      String body = getMessageText(latestMessage);
      LOG.info(
          "Найдено письмо: subject='{}', received={}", subject, latestMessage.getReceivedDate());

      String code = extractConfirmationCode(subject + "\n" + body);
      if (code != null) {
        LOG.info("Извлечён код подтверждения: {}", code);
      }
      return code;
    } catch (Exception e) {
      LOG.warn("Не удалось прочитать почту: {}", e.getMessage());
      return null;
    } finally {
      closeQuietly(inbox);
      closeQuietly(store);
    }
  }

  private static String readLatestLinkToRestoreAccess(
      Properties mailProperties, String recipientEmail, Instant sentAfter) {
    Store store = null;
    Folder inbox = null;
    try {
      Session session = Session.getInstance(buildImapProperties(mailProperties));
      store = session.getStore("imap");
      store.connect(
          mailProperties.getProperty("mail.imap.host"),
          993,
          mailProperties.getProperty("mail.imap.username"),
          mailProperties.getProperty("mail.imap.password"));

      inbox = store.getFolder(mailProperties.getProperty("mail.imap.folder", "INBOX"));
      inbox.open(Folder.READ_ONLY);

      Message latestMessage = findLatestMessage(inbox, recipientEmail, sentAfter);
      if (latestMessage == null) {
        LOG.info("Письмо с ссылкой для {} пока не найдено", recipientEmail);
        return null;
      }

      String subject = latestMessage.getSubject();
      String body = getMessageText(latestMessage);
      LOG.info(
          "Найдено письмо: subject='{}', received={}", subject, latestMessage.getReceivedDate());

      String href = extractRestoreLink(subject + "\n" + body);
      if (href != null) {
        LOG.info("Извлечёна ссылка: {}", href);
      }
      return href;
    } catch (Exception e) {
      LOG.warn("Не удалось прочитать почту: {}", e.getMessage());
      return null;
    } finally {
      closeQuietly(inbox);
      closeQuietly(store);
    }

  }


  private static Properties buildImapProperties(Properties mailProperties) {
    Properties props = new Properties();
    props.put("mail.imap.host", mailProperties.getProperty("mail.imap.host"));
    props.put("mail.imap.user", mailProperties.getProperty("mail.imap.username"));
    props.put("mail.imap.port", mailProperties.getProperty("mail.imap.port", "993"));
    props.put("mail.imap.ssl.enable", mailProperties.getProperty("mail.imap.ssl.enable", "true"));
    props.put("mail.imap.ssl.trust", "*");
    props.put(
        "mail.imap.connectiontimeout",
        mailProperties.getProperty("mail.imap.connection.timeout.ms", "10000"));
    props.put(
        "mail.imap.timeout", mailProperties.getProperty("mail.imap.read.timeout.ms", "10000"));
    return props;
  }

  private static Message findLatestMessage(Folder inbox, String recipientEmail, Instant sentAfter)
      throws Exception {
    var count = inbox.getMessageCount();
    Message message = inbox.getMessage(count);

    if (!(message instanceof MimeMessage mimeMessage)) {
      return null;
    }

    Date receivedDate = message.getReceivedDate();
    if (receivedDate == null || receivedDate.toInstant().isBefore(sentAfter)) {
      return null;
    }

    if (!isMessageForRecipient(mimeMessage, recipientEmail)) {
      return null;
    }

    return message;
  }

  private static boolean isMessageForRecipient(MimeMessage message, String recipientEmail)
      throws Exception {
    if (recipientEmail == null || recipientEmail.isBlank()) {
      return true;
    }

    var recipients = message.getAllRecipients();
    if (recipients == null) {
      return true;
    }

    String normalizedEmail = recipientEmail.trim().toLowerCase();
    for (var recipient : recipients) {
      if (recipient.toString().toLowerCase().contains(normalizedEmail)) {
        return true;
      }
    }
    return false;
  }

  static String extractConfirmationCode(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }

    String plainText = HTML_TAG.matcher(text).replaceAll(" ").replaceAll("\\s+", " ").trim();
    Matcher matcher = CONFIRMATION_CODE.matcher(plainText);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  static String extractRestoreLink(String text) {
    if (text == null || text.isBlank()) return null;
    String plainText = HTML_TAG.matcher(text).replaceAll(" ")
        .replaceAll("\\s+", " ").trim();
    Matcher matcher = RESTORE_ACCESS.matcher(plainText);
    return matcher.find() ? matcher.group(1) : null;
  }

  private static String getMessageText(Message message) throws Exception {
    Object content = message.getContent();
    if (content instanceof String text) {
      return text;
    }
    if (content instanceof Multipart multipart) {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < multipart.getCount(); i++) {
        BodyPart part = multipart.getBodyPart(i);
        if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
          builder.append(part.getContent()).append('\n');
        }
      }
      return builder.toString();
    }
    return content.toString();
  }

  private static void closeQuietly(Folder folder) {
    if (folder == null || !folder.isOpen()) {
      return;
    }
    try {
      folder.close(false);
    } catch (Exception ignored) {
      // already closed
    }
  }

  private static void closeQuietly(Store store) {
    if (store == null || !store.isConnected()) {
      return;
    }
    try {
      store.close();
    } catch (Exception ignored) {
      // already closed
    }
  }
}
