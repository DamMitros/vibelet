package com.example.vibelet.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AnalyticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static class UserStats {
        public String username;
        public int vibeCount;
    }

    public List<UserStats> getUserPostCounts() {
        String sql = "SELECT u.username, COUNT(v.id) as count FROM users u LEFT JOIN vibes v ON u.id = v.user_id GROUP BY u.username";

        return jdbcTemplate.query(sql, new RowMapper<UserStats>() {
            @Override
            public UserStats mapRow(ResultSet rs, int rowNum) throws SQLException {
                UserStats stats = new UserStats();
                stats.username = rs.getString("username");
                stats.vibeCount = rs.getInt("count");
                return stats;
            }
        });
    }
}