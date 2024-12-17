package database.records;

import tlb1.radix.database.annotations.Identifier;
import tlb1.radix.database.records.Record;

import java.util.Objects;
import java.util.UUID;

public class MagicTestRecord implements Record {

    @Identifier
    public UUID id;

    public int value;

    public MagicTestRecord(int value) {
        this.id = UUID.randomUUID();
        this.value = value;
    }

    public MagicTestRecord() {

    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(value, id);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MagicTestRecord that)) return false;
        return value == that.value && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }
}
