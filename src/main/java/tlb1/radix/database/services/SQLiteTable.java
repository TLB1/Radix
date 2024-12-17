package tlb1.radix.database.services;

import tlb1.radix.database.Column;
import tlb1.radix.database.FieldUsePredicate;
import tlb1.radix.database.records.Record;
import tlb1.radix.database.annotations.Identifier;
import tlb1.radix.database.annotations.Reference;
import tlb1.radix.database.annotations.TableName;
import tlb1.radix.database.records.RecordValueSet;
import tlb1.radix.util.Multimap;

import java.lang.reflect.Field;
import java.util.*;

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

    private final SQLiteColumnMapper columnMapper = new SQLiteColumnMapper();

    /**
     * Class type requires a class with a <b>@DBTableName</b> and one or more <b>@DBField</b>(s)
     */
    public final Class<? extends Record> type;

    private final List<Column> columns;

    /**
     * @param name of the database table in-context
     * @param type record model for the database table
     */
    public SQLiteTable(String name, Class<? extends Record> type) {
        columns = computeColumns(FieldUsePredicate.DEFAULT_NONE);
        this.name = name;
        this.type = type;
    }

    /**
     * @param type class to create a DB table for
     */
    public SQLiteTable(Class<? extends Record> type, FieldUsePredicate predicate) {
        if(type.isAnnotationPresent(TableName.class)){
            name = type.getAnnotation(TableName.class).value();
        } else name = type.getSimpleName() + "s";
        this.type = type;
        columns = computeColumns(predicate);
        if(columns.isEmpty()){
            throw new IllegalArgumentException("Record type could not be assigned to a table");
        }
    }

    private List<Column> computeColumns(FieldUsePredicate predicate){
        List<Column> columns = new ArrayList<>();
        Arrays.stream(type.getFields()).forEach((field -> {
            Optional<Column> optional = columnMapper.get(field, predicate);
            optional.ifPresent(columns::add);

        }));
        return columns;
    }

    /**
     * @param field the field you want to read
     * @param record the in memory record
     * @return null if the reference could not find a valid identifier
     * @throws IllegalAccessException if the field does not have a public accessor
     */
    private String getValue(Field field, Record record) throws IllegalAccessException {
        if(!field.isAnnotationPresent(Reference.class)){
            if(field.get(record) == null) return null;
            return field.get(record).toString();
        }

        Optional<Field> key = columnMapper.getIdentifier(field.getType());

        if(key.isPresent()) return key.get().get(field.get(record)).toString();
        return null;
    }

    /**
     * @return the identifier field of this table
     * @throws IllegalStateException if it couldn't retrieve an Identifier
     */
    @Override
    public Column getIdentifier(){
        return columns.stream().filter(column ->
                        column.getField().isAnnotationPresent(Identifier.class)).findFirst()
                .orElseThrow(()-> new IllegalStateException("Table should have an Identifier"));
    }


    /**
     * Organises the Record data for use in a DB context
     */
    @Override
    public RecordValueSet prepareInsert(Record r) {
        if (r.getClass() != type) throw new IllegalArgumentException("Object should be of type " + type.getName());
        Multimap<String, String> record = new Multimap<>();
        columns.forEach(column -> {
            try {
                record.put(column.getName(), getValue(column.getField(), r));
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
            columns.forEach(column -> {
                try {
                    recordValues.put(column.getName(), getValue(column.getField(), r));
                } catch (IllegalAccessException ignored) {
                   recordValues.put(column.getName(), "null");
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
        columns.forEach(((column) -> query.append(column.getName()).append(" ").append(column.getType()).append(",\n ")));
        query.replace(query.length() - 3, query.length() - 1, "\n);");
        return query.toString();
    }

    /**
     * @return The sql query that contains a selector for all current fields in the java class
     */
    @Override
    public String selectTableQuery() {
        StringBuilder query = new StringBuilder("SELECT ");
        columns.forEach(((column) -> query.append(column.getName()).append(", ")));
        query.replace(query.length() - 2, query.length() - 1, " FROM");
        query.append(name);
        return query.toString();
    }

    @Override
    public String selectTableQuery(long limit) {
        return "%s LIMIT %s".formatted(selectTableQuery(), limit);
    }

    @Override
    public String deleteRecordQuery(){
        return "DELETE FROM %s WHERE %s = ?;".formatted(getName(), columnMapper.getName(getIdentifier().getField()));
    }

}
