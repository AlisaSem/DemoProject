package demo.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

/** Чтение ресурсов с classpath. */
public class FileUtils {
  private FileUtils() {
    ExceptionUtils.throwInstantiationException(getClass());
  }

  /**
   * Возвращает содержимое ресурса относительно класса.
   *
   * @param clazz класс, относительно которого ищется ресурс
   * @param relativePath путь к ресурсу
   * @return содержимое файла или исходный путь, если ресурс не прочитан
   * @throws IOException если не удалось прочитать файл
   */
  public static String getResourceContent(Class<?> clazz, String relativePath) throws IOException {
    try {
      URI scriptUri =
          Optional.ofNullable(clazz.getResource(relativePath))
              .orElseThrow(() -> new Exception("Не найден файл " + relativePath))
              .toURI();
      try (BufferedReader br = Files.newBufferedReader(Path.of(scriptUri))) {
        return br.lines().collect(Collectors.joining());
      }

    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return relativePath;
  }
}
