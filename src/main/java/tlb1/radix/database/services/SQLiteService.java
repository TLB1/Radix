package tlb1.radix.database.services;

import org.sqlite.JDBC;
import tlb1.radix.database.FieldUsePredicate;
import tlb1.radix.database.TableRegistrationPredicate;
import tlb1.radix.database.records.Record;
import tlb1.radix.database.records.RecordValueSet;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The database service implementation for SQLite
 * Automatically creates the SQLite file when needed
 */
public class SQLiteService implements DBService {
    private static final Logger logger = Logger.getLogger(SQLiteService.class.getName());

    private final Set<String> existingTables = new HashSet<>();
    private final List<SQLiteTable> tables = new ArrayList<>();

    private TableRegistrationPredicate registrationPredicate;
    private FieldUsePredicate fieldUsePredicate;

    private Connection con;

    /**
     * The path to SQLite file itself
     */
    public final String dbPath;

    /**
     * The SQLite query for asserting if a table exists
     */
    public static final String TABLE_QUERY = "SELECT name FROM sqlite_master WHERE type='table' AND name='%s';";

    /**
     * The default constructor
     *
     * @param database the path of the sqlite file
     */
    public SQLiteService(String database) {
        registrationPredicate = TableRegistrationPredicate.IMPLEMENTED_RECORD_ONLY;
        fieldUsePredicate = FieldUsePredicate.DEFAULT_NONE;
        dbPath = database;
    }
    /**
     * The default constructor
     *
     * @param database the path of the sqlite file
     */
    public SQLiteService(String database, boolean magic) {
        this(database);
        if(magic){
            registrationPredicate = TableRegistrationPredicate.RECORD_ONLY;
            fieldUsePredicate = FieldUsePredicate.DEFAULT_ALL;
        }
        try{
            createConnection();
        }catch (SQLException e){
            logger.log(Level.SEVERE, "Could not create database connection using magic.");
            throw new IllegalStateException("Could not create database connection using magic.");
        }
    }


    @Override
    public void setTableRegistrationPredicate(TableRegistrationPredicate predicate) {
        registrationPredicate = predicate;
    }

    @Override
    public String getDatabaseURL() {
        return JDBC.PREFIX + dbPath;
    }

    @Override
    public void createConnection() throws SQLException {
        con = DriverManager.getConnection(getDatabaseURL());
    }

    @Override
    public void closeConnection() throws SQLException {
        con.close();
    }

    @Override
    public void setLogLevel(Level level) {
        logger.setLevel(level);
    }

    /**
     * Used to completely remove the database from the system
     *
     * @throws SQLException if an error occurs while accessing the database
     */
    public void eradicate() throws SQLException {
        if (!con.isClosed()) closeConnection();
        if (!new File(dbPath).delete()) throw new IllegalStateException("DB File could not be deleted");
    }

    /**
     * @param tableType class to create a table for
     */
    @Override
    public SQLiteTable registerTable(Class<? extends Record> tableType) {
        SQLiteTable table = new SQLiteTable(tableType, fieldUsePredicate);
        try {
            if (!tableExists(table.getName())) {
                exec(table.createTableQuery());
            }
            tables.add(table);
            return table;
        }catch (SQLException e){
            logger.log(Level.SEVERE, "Could not create table for %s.".formatted(tableType.getName()));
            throw new IllegalStateException("Could not create table for %s.".formatted(tableType.getName()));
        }
    }

    /**
     * @return if the table exists or did exist during this runtime
     */
    @Override
    public boolean tableExists(String tableName) {
        if (existingTables.contains(tableName)) return true;

        try{
            boolean exists = con.createStatement().executeQuery(String.format(TABLE_QUERY, tableName)).next();
            if (exists) existingTables.add(tableName);
            return exists;
        }catch (SQLException e){
            logger.log(Level.WARNING, "Could not check if table: %s exists.".formatted(tableName));
            return false;
        }
    }


    @Override
    public boolean tableExists(Class<?> type) {
        for (SQLiteTable table : tables) {
            if (table.type == type) return true;
        }
        return false;
    }

    private Table getTable(Class<?> type) {
        for (SQLiteTable table : tables) {
            if (table.type.equals(type)) return table;
        }
        return null;
    }

    private Table getOrCreateTable(Class<? extends Record> type) {
        Table table = getTable(type);
        if (table != null) return table;
        if (registrationPredicate.shouldRegister(type)) {
            registerTable(type);
        }
        return getTable(type);
    }

    /**
     * @param type record type to check
     * @return true if the table is empty or failed to find a matching database table
     */
    @Override
    public boolean isTableEmpty(Class<?> type) {
        try {
            Table table = getTable(type);
            if (table == null) return true;
            return !execQuery(table.selectTableQuery() + " LIMIT 1").next();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not check if table for %s is empty.".formatted(type.getName()));
            return false;
        }
    }

    @Override
    public boolean hasRecords(Class<?> type) {
        return tableExists(type) && !isTableEmpty(type);
    }

    @Override
    public long getRecordCount(Class<?> type) {
        try {
            Table table = getTable(type);
            if (table == null) return 0;
            ResultSet result = execQuery("SELECT COUNT(*) FROM %s".formatted(table.getName()));
            result.next();
            return result.getLong(1);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not check record count of table for %s.".formatted(type.getName()));
            return -1;
        }
    }

    /**
     * Experimental method for getting all records of a type
     *
     * @param type record type to retrieve
     * @return the list of records retrieved from the database
     */
    public List<?> getRecords(Class<?> type) {
        try {
            TableReader<?> tableReader = new TableReader<>(type, this);
            return tableReader.call();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not get records for type %s".formatted(type.getName()));
            throw new IllegalStateException("Could not get records for type %s".formatted(type.getName()));
        }
    }

    /**
     * @param type record type to retrieve
     * @return null if something went wrong
     */
    @Override
    public ResultSet retrieveAll(Class<?> type) {
        try {
            Table table = getTable(type);
            if (table == null) return null;
            return execQuery(table.selectTableQuery());
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public void insert(Record record) {
        Table table = getOrCreateTable(record.getClass());
        if (table == null){
            logger.log(Level.SEVERE, "No table could be created for this record type: %s.".formatted(record.getClass()));
            throw new IllegalArgumentException("No table could be created for this record type: %s.".formatted(record.getClass()));
        }

        insert(table, table.prepareInsert(record));
    }

    @Override
    public void insert(Collection<? extends Record> records) {
        if (records.isEmpty()) return;
        Record record = records.iterator().next();
        if (record == null) return;
        Table table = getOrCreateTable(record.getClass());
        if (table == null){
            logger.log(Level.SEVERE, "No table could be created for this record type: %s.".formatted(record.getClass()));
            throw new IllegalArgumentException("No table could be created for this record type: %s.".formatted(record.getClass()));
        }

        insert(table, table.prepareInsert(records));
    }

    @Override
    public void update(Record record) {
        for (SQLiteTable table : tables) {
            if (table.type != record.getClass()) continue;
            delete(record);
            insert(record);
            break;
        }
    }

    public void delete(Record record) {
        for (SQLiteTable table : tables) {
            if (table.type != record.getClass()) continue;
            try (PreparedStatement statement = con.prepareStatement(table.deleteRecordQuery())) {
                statement.setString(1, table.getIdentifier().getField().get(record).toString());
                statement.execute();
            } catch (SQLException | IllegalAccessException e) {
                logger.log(Level.SEVERE, "Record of type: %s could not be deleted.".formatted(record.getClass()));
                throw new IllegalArgumentException("Record of type: %s could not be deleted.".formatted(record.getClass()));
            }
            break;
        }
    }

    private void insert(Table table, RecordValueSet recordValueSet) {
        StringBuilder statement = new StringBuilder("INSERT INTO ");
        statement.append(table.getName()).append(" (");
        recordValueSet.getColumns().forEach(column -> statement.append(column).append(", "));
        statement.delete(statement.length() - 2, statement.length()).append(") VALUES (");

        recordValueSet.prepare();
        while (recordValueSet.hasNext()) {
            recordValueSet.nextRecord().forEach(
                    value -> statement.append("'").append(value.replace("'", "''")).append("', "));

            statement.replace(statement.length() - 2, statement.length() - 1, "),").append("(");
        }
        statement.replace(statement.length() - 4, statement.length(), "); ");
        try {
            exec(statement.toString());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Records from table: %s could not be inserted.".formatted(table.getName()));
            throw new IllegalArgumentException("Records from table: %s could not be inserted.".formatted(table.getName()));
        }
    }

    /**
     * A raw method to execute sql statements,
     * be careful, issues may arise when used incorrectly
     *
     * @param query sql that you want to execute
     * @throws SQLException when your sql statement is invalid
     */
    @Override
    public void exec(String query) throws SQLException {
        con.createStatement().execute(query);
    }

    /**
     * A raw method to execute sql statements with results,
     * be careful, issues may arise when used incorrectly
     *
     * @param query sql that you want to execute
     * @return ResultSet of your query
     * @throws SQLException when your sql statement is invalid
     */
    @Override
    public ResultSet execQuery(String query) throws SQLException {
        return con.createStatement().executeQuery(query);
    }

    @Override
    public void close() throws Exception {
        closeConnection();
    }
}
