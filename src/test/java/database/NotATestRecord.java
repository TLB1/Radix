package database;

import tlb1.radix.database.FieldType;
import tlb1.radix.database.annotations.DBField;
import tlb1.radix.database.annotations.Identifier;
import tlb1.radix.database.records.Record;

import java.util.UUID;

public class NotATestRecord implements Record {
    @Identifier
    @DBField(FieldType.UUID)
    public UUID id;

    public NotATestRecord(){
        id = UUID.randomUUID();
    }
}
