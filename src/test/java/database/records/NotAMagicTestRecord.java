package database.records;

import tlb1.radix.database.annotations.RecordExclude;
import tlb1.radix.database.records.Record;

import java.util.UUID;

public class NotAMagicTestRecord implements Record {

    @RecordExclude
    public UUID id;

    public NotAMagicTestRecord() {
        id = UUID.randomUUID();
    }
}
