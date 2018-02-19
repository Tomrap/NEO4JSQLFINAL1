package com.service;

import com.Domain.GraphDetail;
import com.dao.NEO4JReaderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Created by John on 2018-02-18.
 */
@Service
public class GraphReader {

    @Autowired @Lazy
    private NEO4JReaderDao neo4JReaderDao;

    public GraphDetail read() {

        return neo4JReaderDao.read();
    }

}
