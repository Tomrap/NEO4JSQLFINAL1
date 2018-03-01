package com.SQLToGraph.Service;

import com.SQLToGraph.Domain.SQLtoGraphTableDetail;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 2018-02-18.
 */
@Service
public class SQLService {

    private static final Logger logger = Logger.getLogger(SQLService.class);

    @Autowired @Lazy
    private com.SQLToGraph.Dao.SQLImportDao SQLImportDao;

    public List<List<Map<String, Object>>> readAllTables(List<SQLtoGraphTableDetail> SQLtoGraphTableDetailList) throws SQLException {

        return SQLImportDao.readAllTables(SQLtoGraphTableDetailList);
    }

}
