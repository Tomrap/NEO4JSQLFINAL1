package com.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 2017-01-08.
 */
@Repository
public class Dao {

    private JdbcOperations jdbcOperations;

    @Autowired
    public Dao(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }


    public List<Map<String,Object>> get() {
        return jdbcOperations.query("SELECT * FROM sakila.actor", new RowMapper<Map<String,Object>>() {

            public Map<String, Object> mapRow(ResultSet resultSet, int i) throws SQLException {
                Map<String, Object> map = new HashMap<>();
                int numberOfCOlumns = resultSet.getMetaData().getColumnCount();
//                resultSet.getMetaData().getColumnName()
                for(int j = 1; j <numberOfCOlumns+1; j++) {
                    map.put(resultSet.getMetaData().getColumnName(j),resultSet.getObject(j));
                }

                return map;
            }
        });
    }


}


