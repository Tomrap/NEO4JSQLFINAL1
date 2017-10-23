package com.dao;

import com.Domain.TableDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 2017-01-08.
 */
@Repository
public class Dao {

    @Autowired
    public Dao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }


    private JdbcTemplate jdbcTemplate;


    public List<Map<String, Object>> readTableData(TableDetail table) throws SQLException {

        String tableName = TableDetail.schemaName+"."+ table.getTableName();

        String query = String.format("SELECT * from " + tableName);

        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(query);

//        return jdbcTemplate.getDataSource().getConnection().createStatement().executeQuery("SELECT * from " + TableDetail.schemaName+"."+ table.getTableName());

        return rows;
    }


}


