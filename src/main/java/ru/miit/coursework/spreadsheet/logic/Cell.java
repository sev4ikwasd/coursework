package ru.miit.coursework.spreadsheet.logic;

public class Cell {
    private int x, y;
    private String backgroundColor, textColor;
    private Object value;
    private boolean isEvaluable;

    public Cell(int x, int y, String backgroundColor, String textColor, Object value, boolean isEvaluable) {
        this.x = x;
        this.y = y;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.value = value;
        this.isEvaluable = isEvaluable;
    }

    public Cell() {
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isEvaluable() {
        return isEvaluable;
    }

    public void setEvaluable(boolean evaluable) {
        isEvaluable = evaluable;
    }

    public enum Type {
        STRING,
        INTEGER,
        DOUBLE,
    }
}
