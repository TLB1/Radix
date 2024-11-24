package tlb1.radix.database.services;

import tlb1.radix.database.records.Record;
import tlb1.radix.database.annotations.DBField;
import tlb1.radix.database.annotations.Identifier;
import tlb1.radix.database.annotations.Reference;
import tlb1.radix.database.annotations.TableName;
import tlb1.radix.database.records.RecordValueSet;
import tlb1.radix.util.Multimap;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

/**
 *  The table interface that connects the DB to the data-model
 */
public class SQLiteTable implements Table {

    /**
     * DBTable name for use in the DB
     */
    private final String name;

    @Override
    public String getName() {
        return name;
    }

    /**
     * Class type requires a class with a <b>@DBTableName</b> and one or more <b>@DBField</b>(s)
     */
    public final Class<? extends Record> type;

    private Map<Field, String> fields;

    /**
     * @param name of the database table in-context
     * @param type record model for the database table
     */
    public SQLiteTable(String name, Class<? extends Record> type) {
        this.name = name;
        this.type = type;
        this.fields = getTypeFields();
    }

    /**
     * @param type class to create a DB table for
     * @throws NullPointerException if class does not include a table name
     */
    public SQLiteTable(Class<? extends Record> type) throws NullPointerException {
        this(type.getAnnotation(TableName.class).value(), type);
    }

    /**
     * @return the fields of the type that have the <b>@DBField</b> annotation
     * @throws NullPointerException if the class type does not include any DB fields
     */
    private Map<Field, String> getTypeFields() throws NullPointerException {
        fields = new HashMap<>();
        Stream.of(this.type.getFields())
                .filter(field -> field.isAnnotationPresent(DBField.class)).forEach(
                        field -> fields.put(field, field.getAnnotation(DBField.class).value().toString())
                );
        if (fields.isEmpty())
            throw new NullPointerException("Class type does not include any DB fields (@DBField field)");
        addForeignKeys(fields);
        return fields;
    }

    /**
     * @param tableFields all fields that contain references
     * @throws IllegalStateException if the reference class does not have an identifier
     */
    private void addForeignKeys(Map<Field, String> tableFields) {
        Stream.of(this.type.getFields())
                .filter(field -> field.isAnnotationPresent(Reference.class)).forEach((field -> {
                    Optional<Field> foreignField = getPrimaryKey(field.getType());

                    if (foreignField.isEmpty())
                        throw new IllegalStateException("Reference reference does not have a Identifier");
                    Field foreignKey = foreignField.get();
                    if (!foreignKey.isAnnotationPresent(DBField.class))
                        throw new IllegalStateException("Identifier of reference does not have a field type");
                    tableFields.put(field, foreignKey.getAnnotation(DBField.class).value().toString());
                }));
    }

    /**
     * @param field the field you want to read
     * @param record the in memory record
     * @return null if the reference could not find a valid identifier
     * @throws IllegalAccessException if the field does not have a public accessor
     */
    private String getValue(Field field, Record record) throws IllegalAccessException {
        if(!field.isAnnotationPresent(Reference.class)) return field.get(record).toString();

        Optional<Field> key = getPrimaryKey(field.getType());

        if(key.isPresent()) return key.get().get(field.get(record)).toString();
        return null;
    }

    /**
     * @param type class to check
     * @return the first identifier field if present
     */
    @Override
    public Optional<Field> getPrimaryKey(Class<?> type){
        return Arrays.stream(type.getDeclaredFields()).filter(foreignField ->
                foreignField.isAnnotationPresent(Identifier.class)).findFirst();
    }

    /**
     * Organises the Record data for use in a DB context
     */
    @Override
    public RecordValueSet prepareInsert(Record r) {
        if (r.getClass() != type) throw new IllegalArgumentException("Object should be of type " + type.getName());
        Multimap<String, String> record = new Multimap<>();
        fields.keySet().forEach(field -> {
            try {
                record.put(field.getName(), getValue(field, r));
            } catch (Exception ignored) {

            }
        });
        return new RecordValueSet(record);
    }
    /**
     * Organises the Record collection data for use in a DB context
     */
    @Override
    public RecordValueSet prepareInsert(Collection<? extends Record> collection) {
        if (collection.isEmpty()) return null;
        for (Record record : collection) {
            if (record.getClass() != type)
                throw new IllegalArgumentException("Object should be of type " + type.getName());
            break;
        }
        Multimap<String, String> recordValues = new Multimap<>();
        collection.forEach(r ->
            fields.keySet().forEach(field -> {
                try {
                    recordValues.put(getDBFieldName(field), getValue(field, r));
                } catch (IllegalAccessException ignored) {
                   recordValues.put(getDBFieldName(field), "null");
                }
            })
        );

        return new RecordValueSet(recordValues);
    }

    /**
     * @return The sql query as an unprepared String
     */
    @Override
    public String createTableQuery() {
        StringBuilder query = new StringBuilder("CREATE TABLE ");
        query.append(name).append(" (\n ");
        fields.forEach(((field, value) -> query.append(getDBFieldName(field)).append(" ").append(value).append(",\n ")));
        query.replace(query.length() - 3, query.length() - 1, "\n);");
        return query.toString();
    }

    /**
     * @return The sql query that contains a selector for all current fields in the java class
     */
    @Override
    public String selectTableQuery() {
        StringBuilder query = new StringBuilder("SELECT ");
        fields.forEach(((field, value) -> query.append(getDBFieldName(field)).append(", ")));
        query.replace(query.length() - 2, query.length() - 1, " FROM");
        query.append(name);
        return query.toString();
    }

    @Override
    public String selectTableQuery(long limit) {
        return "%s LIMIT %s".formatted(selectTableQuery(), limit);
    }

    /**
     * @return A generated field name used for database generation, works with relational datastructures
     */
    @Override
    public String getDBFieldName(Field field){
        if(!field.isAnnotationPresent(Reference.class)) return field.getName();

        Optional<Field> otherField = getPrimaryKey(field.getType());
        if(otherField.isEmpty()) return field.getName();
        String addedName = otherField.get().getName();
        addedName = addedName.substring(0, 1).toUpperCase() + addedName.substring(1);
        return field.getName() + addedName;
    }
}
