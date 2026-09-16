package demo.database.dao;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Доступ к таблицам пользователей и подтверждения почты. */
@Repository
public class UsersDao {
  private final JdbcTemplate usersDbJdbcTemplate;

  /**
   * Создаёт DAO с JDBC-шаблоном основной БД.
   *
   * @param usersDbJdbcTemplate шаблон для запросов к demoDb
   */
  @Autowired
  public UsersDao(@Qualifier("demoDb") JdbcTemplate usersDbJdbcTemplate) {
    this.usersDbJdbcTemplate = usersDbJdbcTemplate;
  }

  /**
   * Возвращает пользователя по идентификатору.
   *
   * @param id идентификатор пользователя
   * @return строка пользователя
   */
  public Map<String, Object> getUser(String id) {
    String sql = "select * from users u where u.id = ?";
    var res = usersDbJdbcTemplate.queryForList(sql, Integer.parseInt(id));
    return res.get(0);
  }

  /**
   * Возвращает запись с кодом подтверждения по email.
   *
   * @param mail адрес электронной почты
   * @return строка с кодом
   */
  public Map<String, Object> getCodeByMail(String mail) {
    String sql = "SELECT code FROM email_confirmations where email = ?";
    var res = usersDbJdbcTemplate.queryForList(sql, mail);
    return res.get(0);
  }

  /**
   * Ищет код подтверждения по email.
   *
   * @param mail адрес электронной почты
   * @return код или {@code null}, если записи нет
   */
  public String findConfirmationCode(String mail) {
    String sql = "SELECT code FROM email_confirmations where email = ?";
    var res = usersDbJdbcTemplate.queryForList(sql, mail);
    if (res.isEmpty() || res.get(0).get("code") == null) {
      return null;
    }
    return res.get(0).get("code").toString();
  }

  /**
   * Удаляет пользователей с указанным email.
   *
   * @param mail адрес электронной почты
   * @return число удалённых строк
   */
  public long deleteUsers(String mail) {
    String sql = "delete from users where email=?";
    return usersDbJdbcTemplate.update(sql, mail);
  }

  /**
   * Возвращает приглашение сотрудника по email пользователя.
   *
   * @param email адрес электронной почты
   * @return строка с invitation_uid
   */
  public Map<String, Object> getInvitationUser(String email) {
    String sql =
        "SELECT employees.invitation_uid From employees JOIN users ON  users.uid ="
            + " employees.user_uid WHERE email=?";
    var res = usersDbJdbcTemplate.queryForList(sql, email);
    return res.get(1);
  }
}
