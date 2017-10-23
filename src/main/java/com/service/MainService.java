package com.service;

import com.Domain.TableDetail;
import com.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by John on 2017-10-20.
 */
@Service
public class MainService {

    @Autowired
    NodeGenerator nodeGenerator;

    @Autowired
    private RDBReader rdbReader;

    public void main() throws SQLException, SchemaCrawlerException {

        List<TableDetail> tables = rdbReader.extractTables();

        nodeGenerator.generate(tables);

    }

}
