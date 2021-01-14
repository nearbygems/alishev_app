package kz.nearbygems.dao;

import kz.nearbygems.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PersonDAO {

  private final JdbcTemplate jdbcTemplate;

  private static final String INDEX = "SELECT * FROM Person";
  private static final String SHOW = "SELECT * FROM Person WHERE id = ?";
  private static final String SAVE = "INSERT INTO Person(name, age, email) VALUES(?, ?, ?)";
  private static final String UPDATE = "UPDATE Person SET name=?, age=?, email=? WHERE id=?";
  private static final String DELETE = "DELETE FROM Person WHERE id=?";

  @Autowired
  public PersonDAO(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Person> index() {
    return jdbcTemplate.query(INDEX, new BeanPropertyRowMapper<>(Person.class));
  }

  public Person show(int id) {
    return jdbcTemplate.query(SHOW, new Object[]{id}, new BeanPropertyRowMapper<>(Person.class))
      .stream().findAny().orElse(null);
  }

  public void save(Person person) {
    jdbcTemplate.update(SAVE, person.getName(), person.getAge(),
      person.getEmail());
  }

  public void update(int id, Person updatedPerson) {
    jdbcTemplate.update(UPDATE, updatedPerson.getName(),
      updatedPerson.getAge(), updatedPerson.getEmail(), id);
  }

  public void delete(int id) {
    jdbcTemplate.update(DELETE, id);
  }

  public void testMultipleUpdate() {
    List<Person> people = create1000People();

    long before = System.currentTimeMillis();

    for (Person person : people) {
      jdbcTemplate.update("INSERT INTO Person VALUES(?, ?, ?, ?)",
        person.getId(), person.getName(), person.getAge(), person.getEmail());
    }

    long after = System.currentTimeMillis();
    System.out.println("Time: " + (after - before));
  }

  public void testBatchUpdate() {
    List<Person> people = create1000People();

    long before = System.currentTimeMillis();

    jdbcTemplate.batchUpdate("INSERT INTO Person VALUES(?, ?, ?, ?)",
      new BatchPreparedStatementSetter() {
        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
          ps.setInt(1, people.get(i).getId());
          ps.setString(2, people.get(i).getName());
          ps.setInt(3, people.get(i).getAge());
          ps.setString(4, people.get(i).getEmail());
        }

        @Override
        public int getBatchSize() {
          return people.size();
        }
      });

    long after = System.currentTimeMillis();
    System.out.println("Time: " + (after - before));
  }

  private List<Person> create1000People() {
    List<Person> people = new ArrayList<>();

    for (int i = 0; i < 1000; i++)
      people.add(new Person(i, "Name" + i, 30, "test" + i + "@mail.ru"));

    return people;
  }
}
