package tlb1.radix.database;

import java.lang.reflect.Field;

public class Column {
    private final FieldType columnType;
    private final String name;

    private final Field field;

    public Column(FieldType columnType, String name, Field field) {
        this.columnType = columnType;
        this.name = name;
        this.field = field;
    }

    public FieldType getType() {
        return columnType;
    }

    public String getName() {
        return name;
    }

    public Field getField() {
        return field;
    }
}
