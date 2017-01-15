package com;

import com.config.SpringConfig;
import com.controller.HomeController;
import com.core.Dao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

/**
 * Created by John on 2017-01-08.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=SpringConfig.class)
public class DaoTest {

    @Autowired
    private Dao dao;

    @Test
    public void daoTest()
    {
        List<Map<String ,Object>> map =  dao.get();
        System.out.println(map);

    }

    @Test
    public void testHomePage() throws Exception {
        HomeController controller = new HomeController();
        Assert.assertEquals("home", controller.home());
    }

}
