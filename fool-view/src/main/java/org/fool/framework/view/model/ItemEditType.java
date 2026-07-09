package org.fool.framework.view.model;

public enum ItemEditType {
    ReadOnly(0),
    TextBox(1),
    CheckBox(2),
    ComboBox(3),
    SelectLable(4),
    RichTextBox(5),
    DatePicker(6),
    TimePicker(7),
    DateTimePicker(8),
    DropTextBox(9),
    Format(10),
    ChartAxis(11),
    ChartLine(12),
    ChartBar(13),
    ChartScatter(14),
    MapLongitude(16),
    MapLatitude(17),
    MapTitle(18);

    private final int code;

    ItemEditType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
