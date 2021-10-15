package ru.miit.coursework;

import javafx.scene.paint.Color;

public class StringCell extends AbstractCell {
    private String content;

    public StringCell(String content) {
        super(Color.BLACK, Color.WHITE);
        this.content = content;
    }

    public StringCell(Color textColor, Color backgroundColor, String content) {
        super(textColor, backgroundColor);
        this.content = content;
    }

    @Override
    public String getValue() {
        return content;
    }

    @Override
    public void setValue(Object value) {
        content = value == null ? "" : value.toString();
    }
}
