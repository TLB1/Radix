package tlb1.radix.database;

import org.sqlite.JDBC;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DBService {
    private Connection con;

    private final Set<String> existingTables = new HashSet<>();

    private final List<Table> tables = new ArrayList<>();

    public final String dbPath;

    public static final String TABLE_QUERY = "SELECT name FROM sqlite_master WHERE type='table' AND name='%s';";

    public DBService(String database) {
        dbPath = database;
    }

    public String getDatabaseURL() {
        return JDBC.PREFIX + dbPath;
    }

    @SuppressWarnings("unused")
    public void createConnection() throws SQLException {
        con = DriverManager.getConnection(getDatabaseURL());
    }

    public void closeConnection() throws SQLException {
        con.close();
    }

    /**
     * Used to completely remove the database from the system
     * @throws SQLException if an error occurs while accessing the database
     */
    public void eradicate() throws SQLException {
        if(!con.isClosed()) closeConnection();
        if (!new File(dbPath).delete()) throw new IllegalStateException("DB File could not be deleted");
    }

    /**
     * @param tableType class to create a table for
     * @throws SQLException when there is something wrong with the database
     */
    @SuppressWarnings("unused")
    public Table registerTable(Class<? extends Record> tableType) throws SQLException {
        Table table = new Table(tableType);
        if (!tableExists(table.getName())) {
            exec(table.createTableQuery());
        }
        tables.add(table);
        return table;
    }

    /**
     * @return if the table exists, or did exist during this runtime
     * @throws SQLException when there is something wrong with the database
     */
    public boolean tableExists(String tableName) throws SQLException {
        if (existingTables.contains(tableName)) return true;

        boolean exists = con.createStatement().executeQuery(String.format(TABLE_QUERY, tableName)).next();
        if (exists) existingTables.add(tableName);
        return exists;
    }


    @SuppressWarnings("invert")
    public boolean tableExists(Class<?> type) {
        for (Table table : tables) {
            if (table.type == type) return true;
        }
        return false;
    }


    /**
     * @param type record type to check
     * @return true if the table is empty or failed to find a matching database table
     */
    @SuppressWarnings("unused")
    public boolean isTableEmpty(Class<?> type) {
        for (Table table : tables) {
            if (table.type != type) continue;
            try {
                return !execQuery(table.selectTableQuery() + " LIMIT 1").next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean hasRecords(Class<?> type) {
        return tableExists(type) && !isTableEmpty(type);
    }

    public int getRecordCount(Class<?> type) {
        for (Table table : tables) {
            if (table.type != type) continue;
            try {
                ResultSet result = execQuery("SELECT COUNT(*) FROM %s".formatted(table.getName()));
                result.next();
                return result.getInt(1);
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    public List<?> getRecords(Class<?> type) {
       try{
           TableReader<?> tableReader = new TableReader<>(type, this);
           return tableReader.call();
       }catch (Exception e){
           throw new RuntimeException("Could not get records");
       }
    }

    /**
     * @param type record type to retrieve
     * @return null if something went wrong
     */
    @SuppressWarnings("unused")
    protected ResultSet retrieveAll(Class<?> type) {
        for (Table table : tables) {
            if (table.type != type) continue;
            try {
                return execQuery(table.selectTableQuery());
            } catch (SQLException e) {
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public void insert(Record record) {
        if (!tableExists(record.getClass()))
            throw new IllegalArgumentException("There exists no table for this record type: " + record.getClass());
        for (Table table : tables) {
            if (table.type != record.getClass()) continue;
            insert(table, table.prepareInsert(record));
            break;
        }
    }

    @SuppressWarnings("unused")
    public void insert(Collection<? extends Record> records) {
        if (records.isEmpty()) return;
        Record record = records.iterator().next();
        if (record == null) return;
        Class<?> type = record.getClass();
        if (!tableExists(type))
            throw new IllegalArgumentException("There exists no table for this record type: " + type);
        for (Table table : tables) {
            if (table.type != type) continue;
            insert(table, table.prepareInsert(records));
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
            e.printStackTrace();
        }
    }

    /**
     * A raw method to execute sql statements,
     * be careful, issues may arise when used incorrectly
     *
     * @param query sql that you want to execute
     * @throws SQLException when your sql statement is invalid
     */
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
    public ResultSet execQuery(String query) throws SQLException {
        return con.createStatement().executeQuery(query);
    }
}
