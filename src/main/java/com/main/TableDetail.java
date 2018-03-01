package com.main;

import java.util.List;

/**
 * Created by John on 2018-03-01.
 */
public abstract class TableDetail {

    private String tableName;
    private List<String> pk;

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
