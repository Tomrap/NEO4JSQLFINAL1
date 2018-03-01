package com.GraphToSQL.Domain;

/**
 * Created by John on 2018-02-18.
 */
public class MyRelationshipType {

    private String firstNodeLabel;
    private String secondNodeLabel;
    private boolean isFirstNodeForeignKey = false;
    private boolean isSecondNodeForeignKey = false;

    public boolean isSecondNodeForeignKey() {
        return isSecondNodeForeignKey;
    }

    public void setSecondNodeForeignKey(boolean secondNodeForeignKey) {
        isSecondNodeForeignKey = secondNodeForeignKey;
    }

    public boolean isFirstNodeForeignKey() {
        return isFirstNodeForeignKey;
    }

    public void setFirstNodeForeignKey(boolean firstNodeForeignKey) {
        isFirstNodeForeignKey = firstNodeForeignKey;
    }

    public String getFirstNodeLabel() {
        return firstNodeLabel;
    }

    public String getSecondNodeLabel() {
        return secondNodeLabel;
    }


    public MyRelationshipType(String firstNodeLabel, String secondNodeLabel) {
        this.firstNodeLabel = firstNodeLabel;
        this.secondNodeLabel = secondNodeLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyRelationshipType that = (MyRelationshipType) o;

        if (!firstNodeLabel.equals(that.firstNodeLabel)) return false;
        return secondNodeLabel.equals(that.secondNodeLabel);

    }

    @Override
    public int hashCode() {
        int result = firstNodeLabel.hashCode();
        result = 31 * result + secondNodeLabel.hashCode();
        return result;
    }

}