package tlb1.radix.database.services;

import tlb1.radix.database.records.Record;
import tlb1.radix.database.records.RecordValueSet;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;

public interface Table {
    String getName();
    Optional<Field> getPrimaryKey(Class<?> type);
    String getDBFieldName(Field field);

    RecordValueSet prepareInsert(Record record);
    RecordValueSet prepareInsert(Collection<? extends Record> records);

    String createTableQuery();
    String selectTableQuery();
    String selectTableQuery(long limit);
}
