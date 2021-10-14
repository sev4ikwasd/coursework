package ru.miit.coursework;

public class StringCell extends AbstractCell {
    private String content;

    public StringCell(String content) {
        this.content = content;
    }

    @Override
    public String getValue() {
        return content;
    }

    @Override
    public void setValue(Object value) {
        content = value.toString();
    }
}
