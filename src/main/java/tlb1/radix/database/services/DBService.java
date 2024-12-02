package tlb1.radix.database.services;

import tlb1.radix.database.TableRegistrationPredicate;
import tlb1.radix.database.records.Record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Is the template for all database service implementations
 */
public interface DBService {

    /**
     * @return the database url including the driver
     */
    String getDatabaseURL();

    /**
     * Attempts to establish a connection to the given database
     * @throws SQLException when there is something wrong with the database
     */
    void createConnection() throws SQLException;

    /**
     * Closes the connection to the database
     * @throws SQLException when there is something wrong with the database
     */
    void closeConnection() throws SQLException;

    /**
     * @param predicate the predicate for automatically creating database tables
     */
    void setTableRegistrationPredicate(TableRegistrationPredicate predicate);

    /**
     * @param tableType class to create a table for
     * @return the data-model of the real table
     */
    Table registerTable(Class<? extends Record> tableType);

    /**
     * @param tableName the table name in a database context
     * @return if the table exists or did exist during this runtime
     */
    boolean tableExists(String tableName);

    /**
     * @param type record type to check
     * @return if the table exists or did exist during this runtime
    **/
    boolean tableExists(Class<?> type);

    /**
     * @param type record type to check
     * @return true if the table is empty or failed to find a matching database table
     */
    boolean isTableEmpty(Class<?> type);

    /**
     * @param type record type to check
     * @return true only if records exist, returns false for initialized types
     */
    boolean hasRecords(Class<?> type);

    /**
     * Queries the database to get the record count of a type
     * @param type record type to check
     * @return The total count of records in the database
     */
    long getRecordCount(Class<?> type);

    /**
     * A raw method to retrieve all records of a type
     * @param type record type to check
     * @return The raw ResultSet of the database query
     */
    ResultSet retrieveAll(Class<?> type);

    /**
     * Experimental method for getting all records of a type
     * @param type record type to retrieve
     * @return the list of records retrieved from the database
     * @throws IllegalStateException if something went wrong
     */
    List<?> getRecords(Class<?> type);

    /**
     * Creates an interaction with the database, useful for single inserts.
     * The use of inserting multiple records with a Collection is highly recommended.
     * @param record to insert into the database
     */
    void insert(Record record);

    /**
     * Is highly optimized due to there only being one interaction with the database for many records
     * @param records to insert into the database
     */
    void insert(Collection<? extends Record> records);

    /**
     * Updates a record in the database using its identifier
     * @param record to update
     */
    void update(Record record);

    /**
     * @param record record to delete
     */
    void delete(Record record);

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
