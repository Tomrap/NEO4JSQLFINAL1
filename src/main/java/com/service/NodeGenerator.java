package com.service;

import com.Domain.TableDetail;
import com.dao.Dao;
import com.dao.Neo4JDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 2017-10-23.
 */
@Service
public class NodeGenerator {

    @Autowired
    private Dao dao;

    @Autowired
    private Neo4JDao neo4JDao;

    public void generate(List<TableDetail> tableDetailList) throws SQLException {

        for(TableDetail tableDetail: tableDetailList) {
            generateTable(tableDetail);
        }
    }

    public void generateTable(TableDetail tableDetail) throws SQLException {

        //todo junction table

        List<Map<String, Object>> rs = dao.readTableData(tableDetail);
        neo4JDao.createNodes(rs,tableDetail);
//        neo4JDao.createRelationships(rs,tableDetail);

    }
}
