package demo.steps;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@SpringBootApplication
@Component
@Repository
public class DataBaseCheck implements DataBaseRepository {

  @Autowired private JdbcTemplate jdbcTemplate;

  //    @Autowired
  //    DataBaseCheck(JdbcTemplate jdbcTemplate) {
  //        this.jdbcTemplate = jdbcTemplate;
  //    }

  @Override
  public int save(DataBaseModel book) {
    return 0;
  }

  @Override
  public int update(DataBaseModel book) {
    return 0;
  }

  @Override
  public Long findById(Long id) {
    return id;
  }

  @Override
  public int deleteById(Long id) {
    return 0;
  }

  @Override
  public List<DataBaseModel> findAll() {
    return null;
  }

  @Override
  public String findByPassword(String password) {
    return password;
  }

  public DataBaseModel findByEmail(String email) {
    try {
      DataBaseModel dataBaseModel =
          jdbcTemplate.queryForObject(
              "SELECT * FROM users WHERE email=?",
              BeanPropertyRowMapper.newInstance(DataBaseModel.class),
              email);

      return dataBaseModel;
    } catch (IncorrectResultSizeDataAccessException e) {
      return null;
    }
  }

  @Override
  public int deleteAll() {
    return 0;
  }
}
