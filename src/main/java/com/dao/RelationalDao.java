package com.dao;

import com.Domain.TableDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 2017-01-08.
 */
@Repository
public class RelationalDao {

    @Autowired
    public RelationalDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }


    private JdbcTemplate jdbcTemplate;


    public List<List<Map<String, Object>>> readAllTables(List<TableDetail> tableDetailList) throws SQLException {

        List<List<Map<String, Object>>> allData = new ArrayList<>();

        for(TableDetail tableDetail: tableDetailList) {
            allData.add(readTableData(tableDetail));
        }

        return allData;
    }

    public List<Map<String, Object>> readTableData(TableDetail table) throws SQLException {

        String tableName = TableDetail.schemaName+"."+ table.getTableName();

        String query = String.format("SELECT * from " + tableName);

        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(query);

//        return jdbcTemplate.getDataSource().getConnection().createStatement().executeQuery("SELECT * from " + TableDetail.schemaName+"."+ table.getTableName());

        return rows;
    }


}


