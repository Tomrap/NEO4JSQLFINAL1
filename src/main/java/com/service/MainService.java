package com.service;

import com.Domain.TableDetail;
import com.dao.RelationalDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 2017-10-20.
 */
@Service
public class MainService {

    private static final Logger logger = Logger.getLogger(MainService.class);

    @Autowired
    private GraphGenerator graphGenerator;

    @Autowired
    private RelationalDao relationalDao;

    @Autowired
    private RDBReader rdbReader;

    public void main() throws SQLException, SchemaCrawlerException, IOException {


        List<TableDetail> tables = rdbReader.extractTables();
        List<List<Map<String, Object>>> allData = relationalDao.readAllTables(tables);
        graphGenerator.generate(allData,tables);
    }

    public long hash(String code) {
        return code.hashCode();
    }

}
