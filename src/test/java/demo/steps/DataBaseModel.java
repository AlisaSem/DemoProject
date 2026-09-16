package demo.steps;

public class DataBaseModel {

  private long id;
  private String email;
  private String password;
  private String nick;

  public DataBaseModel() {}

  public DataBaseModel(long id, String email, String password, String nick) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.nick = nick;
  }

  public DataBaseModel(String email, String password, String nick) {
    this.email = email;
    this.password = password;
    this.nick = nick;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getNick() {
    return nick;
  }

  public void setNick(String nick) {
    this.nick = nick;
  }

  @Override
  public String toString() {
    return "Steps.DataBaseModel [id="
        + id
        + ", email="
        + email
        + ", password="
        + password
        + ", nick="
        + nick
        + "]";
  }
}
