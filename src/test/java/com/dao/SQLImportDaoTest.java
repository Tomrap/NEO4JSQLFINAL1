package com.dao;

import com.config.SpringConfig;
import com.MainService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by John on 2017-01-08.
 */

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringConfig.class})
@WebAppConfiguration
public class SQLImportDaoTest {


    @Autowired
    private MainService mainService;



    @Test
    public void convertSQLtoNEO4JTest() throws SQLException, SchemaCrawlerException, IOException, ClassNotFoundException {

        mainService.convertSQLtoNEO4J();

    }

    @Test
    public void convertNEO4JtoSQLTest() throws SQLException, IOException {
        mainService.convertNEO4JtoSQL();
    }

}
