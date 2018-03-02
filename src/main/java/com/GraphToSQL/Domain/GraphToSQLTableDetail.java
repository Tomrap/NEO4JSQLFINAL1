package com.GraphToSQL.Domain;

import java.util.List;
import java.util.Map;

/**
 * Created by John on 2018-03-01.
 */
public class GraphToSQLTableDetail{

    public static final String schemaName = "default";

    private String tableName;
    private List<String> pk;
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

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public String getTableName() {
        return tableName;
    }

    public List<String> getPk() {
        return pk;
    }
    public void setPk(List<String> pk) {
        this.pk = pk;
    }

}
