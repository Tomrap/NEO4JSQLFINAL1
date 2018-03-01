package com.GraphToSQL.Domain;

import com.main.TableDetail;

import java.util.List;
import java.util.Map;

/**
 * Created by John on 2018-03-01.
 */
public class GraphToSQLTableDetail extends TableDetail {

    public static final String schemaName = "sakila1";

    private List<String> graphFks;
    private Map<String, Object> columnsAndTypes;

    public List<String> getGraphFks() {
        return graphFks;
    }
    public void setGraphFks(List<String> graphFks) {
        this.graphFks = graphFks;
    }

    public Map<String, Object> getColumnsAndTypes() {
        return columnsAndTypes;
    }
    public void setColumnsAndTypes(Map<String, Object> columnsAndTypes) {
        this.columnsAndTypes = columnsAndTypes;
    }

}
