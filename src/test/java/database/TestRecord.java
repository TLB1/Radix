package database;

import tlb1.radix.database.*;
import tlb1.radix.database.Record;

import java.util.Objects;
import java.util.UUID;

@TableName("tests")
public class TestRecord extends Record {

    @Identifier
    @DBField(FieldType.UUID)
    public UUID id;

    @DBField(FieldType.INTEGER)
    public int value;

    public TestRecord(int value) {
        this.id = UUID.randomUUID();
        this.value = value;
    }

    public TestRecord(){

    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(value, id);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TestRecord that)) return false;
        return value == that.value && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }
}
