package com.dao;

import com.Domain.MyRelationshipType;
import com.config.SpringConfig;
import com.config.WebConfig;
import com.controller.HomeController;
import com.service.MainService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 2017-01-08.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpringConfig.class, WebConfig.class})
@WebAppConfiguration
public class SQLImportDaoTest {


    @Autowired
    private MainService mainService;



    @Test
    public void daoTest() throws IOException {

//        neo4JDao.createDb();
    }

    @Test
    public void convertSQLtoNEO4JTest() throws SQLException, SchemaCrawlerException, IOException {

        mainService.convertSQLtoNEO4J();

    }

    @Test
    public void convertNEO4JtoSQLTest() throws SQLException {
        mainService.convertNEO4JtoSQL();
    }

    @Test
    public void testt() {

        Map<MyRelationshipType,List<String>> allMyRelationships = new HashMap<>();
        MyRelationshipType myRelationshipType1 = new MyRelationshipType("store","staff");
        MyRelationshipType myRelationshipType2 = new MyRelationshipType("staff","store");

        int hashcode1 = myRelationshipType1.hashCode();
        int hashcode2 = myRelationshipType2.hashCode();

        System.out.println(hashcode1);
        System.out.println(hashcode2);

        allMyRelationships.computeIfAbsent(myRelationshipType1, k-> new ArrayList<>()).add("Cos1");
        allMyRelationships.computeIfAbsent(myRelationshipType2, k-> new ArrayList<>()).add("Cos2");

        System.out.println("gdg");
    }


    @Test
    public void testHomePage() throws Exception {
        HomeController controller = new HomeController();
        Assert.assertEquals("home", controller.home());
    }

}
