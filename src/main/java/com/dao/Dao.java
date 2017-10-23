package com.dao;

import com.Domain.TableDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

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


    public ResultSet readTableData(TableDetail table) throws SQLException {
        return jdbcTemplate.getDataSource().getConnection().createStatement().executeQuery("SELECT * from " + TableDetail.schemaName+"."+ table.table);
    }


}


