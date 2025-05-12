package com.example.batch.repository;

import com.example.batch.model.Task;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepository {

    private Long count = 0L;
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Task> rowMapper = (ResultSet rs, int rowNum)->{
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        boolean hasTotalCount = false;

        for (int i = 1; i <= columnCount; i++) {
            if ("total_count".equalsIgnoreCase(metaData.getColumnLabel(i))) {
                hasTotalCount = true;
                break;
            }
        }
        if (hasTotalCount) {
            this.count = rs.getLong("total_count");
        }
        return new Task(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getBoolean("completed")
        );
    };

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Pair<List<Task>, Long> findAll(int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT t.*, COUNT(*) OVER() AS total_count\n" +
                "FROM task t\n" +
                "ORDER BY id\n" +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<Task> tasks = jdbcTemplate.query(sql, rowMapper, offset, size);
        return Pair.of(tasks, this.count);

    }


    public Optional<Task> findById(Long id) {
        List<Task> results = jdbcTemplate.query(
                "SELECT * FROM task WHERE id = ?",
                rowMapper,
                id
        );
        return results.stream().findFirst();
    }

    public Task save(Task task) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO task (title, description, completed) VALUES (?, ?, ?)",
                    new String[]{ "id" }
            );
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setBoolean(3, task.isCompleted());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        assert key != null;
        return new Task( key.longValue(), task.getTitle(), task.getDescription(), task.isCompleted());

    }

    public int update(Task task) {
        return jdbcTemplate.update(
                "UPDATE task SET title = ?, description = ?, completed = ? WHERE id = ?",
                task.getTitle(), task.getDescription(), task.isCompleted(), task.getId()
        );

    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM task WHERE id = ?", id);
    }
}
