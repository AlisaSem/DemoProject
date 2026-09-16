package demo.support;

/** Вспомогательные методы для исключений. */
public class ExceptionUtils {

  private ExceptionUtils() {
    throwInstantiationException(getClass());
  }

  /**
   * Запрещает создание экземпляра утилитарного класса.
   *
   * @param clazz класс, экземпляр которого нельзя создавать
   */
  public static void throwInstantiationException(Class<?> clazz) {
    throw new IllegalStateException("Попытка создать экземпляр неинстанцируемого класса " + clazz);
  }
}
