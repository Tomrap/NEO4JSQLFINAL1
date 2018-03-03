package com.GraphToSQL.Domain;

/**
 * Created by John on 2018-02-18.
 */
public class MyRelationshipType {

    private String label;
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

    public String getLabel() {
        return label;
    }

    public MyRelationshipType(String label, String firstNodeLabel, String secondNodeLabel) {
        this.firstNodeLabel = firstNodeLabel;
        this.secondNodeLabel = secondNodeLabel;
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyRelationshipType that = (MyRelationshipType) o;

        return label.equals(that.label);

    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }
}
