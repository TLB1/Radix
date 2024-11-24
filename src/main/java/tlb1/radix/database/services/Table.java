package tlb1.radix.database.services;

import tlb1.radix.database.records.Record;
import tlb1.radix.database.records.RecordValueSet;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;

/**
 * The table interface that is used to interact with the real database table
 */
public interface Table {

    /**
     * @return the table name as defined in the database
     */
    String getName();

    /**
     * @param type class to check
     * @return the first identifier field (if present)
     */
    Optional<Field> getPrimaryKey(Class<?> type);

    /**
     * @param field one of the records data-model fields
     * @return the name of the field in the DB context
     * @throws IllegalArgumentException if the field is not related to the database context
     */
    String getDBFieldName(Field field);

    /**
     * Organises the Record data for use in a DB context
     * @param record the record to insert
     * @return the set of keys with their values
     */
    RecordValueSet prepareInsert(Record record);

    /**
     * Organises the Record collection data for use in a DB context
     * @param records the collection of records to insert
     * @return the set of keys with their values
     */
    RecordValueSet prepareInsert(Collection<? extends Record> records);

    /**
     * Is used to generate a sql query to generate a table
     * @return The sql query as a String
     */
    String createTableQuery();

    /**
     * Is used to generate a sql query to select all rows
     * @return The sql query as a String
     */
    String selectTableQuery();

    /**
     * Is used to generate a sql query to select the first n rows
     * @param limit sets the amount of rows
     * @return The sql query as a String
     */
    String selectTableQuery(long limit);
}
