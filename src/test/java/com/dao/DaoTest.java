package com.dao;

import com.config.SpringConfig;
import com.config.WebConfig;
import com.controller.HomeController;
import com.service.RDBReader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 2017-01-08.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringConfig.class, WebConfig.class})
@WebAppConfiguration
public class DaoTest {

    @Autowired
    private Dao dao;

    @Autowired
    private RDBReader rdbReader;

    @Autowired
    private Neo4JDao neo4JDao;

    @Test
    public void daoTest() throws IOException {

//        neo4JDao.createDb();


        List<Map<String ,Object>> map =  dao.get();
        System.out.println(map);

    }

    @Test
    public void rdbreaderTest() throws SQLException, SchemaCrawlerException {

        rdbReader.extractTables();
    }



    @Test
    public void testHomePage() throws Exception {
        HomeController controller = new HomeController();
        Assert.assertEquals("home", controller.home());
    }

}
