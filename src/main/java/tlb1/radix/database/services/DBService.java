package tlb1.radix.database.services;

import tlb1.radix.database.records.Record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

public interface DBService {

    String getDatabaseURL();

    void createConnection() throws SQLException;
    void closeConnection() throws SQLException;

    default void eradicate() throws SQLException{
        throw new UnsupportedOperationException();
    }

    Table registerTable(Class<? extends Record> tableType) throws SQLException;
    boolean tableExists(String tableName)  throws SQLException;
    boolean tableExists(Class<?> type);
    boolean isTableEmpty(Class<?> type);
    boolean hasRecords(Class<?> type);
    long getRecordCount(Class<?> type);

    ResultSet retrieveAll(Class<?> type);

    void insert(Record record);
    void insert(Collection<? extends Record> records);
    /**
     * A raw method to execute sql statements
     *
     * @param query sql that you want to execute
     * @throws SQLException when your sql statement is invalid
     */
    void exec(String query) throws SQLException;

    /**
     * A raw method to execute sql statements with results,
     * @param query sql that you want to execute
     * @return ResultSet of your query
     * @throws SQLException when your sql statement is invalid
     */
    ResultSet execQuery(String query) throws SQLException;
}
