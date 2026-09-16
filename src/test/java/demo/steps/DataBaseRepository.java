package demo.steps;

import java.util.List;

public interface DataBaseRepository {
  int save(DataBaseModel book);

  int update(DataBaseModel book);

  Long findById(Long id);

  int deleteById(Long id);

  List<DataBaseModel> findAll();

  String findByPassword(String password);

  static DataBaseModel findByEmail(String email) {
    return null;
  }

  int deleteAll();
}
