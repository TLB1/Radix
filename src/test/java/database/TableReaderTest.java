package database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tlb1.radix.database.services.DBService;
import tlb1.radix.database.services.SQLiteService;
import tlb1.radix.database.services.TableReader;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class TableReaderTest {
    private static final String DB_NAME = "test.db";
    private static DBService service;

    private static final List<TestRecord> testRecords = List.of(
            new TestRecord(256),
            new TestRecord(111),
            new TestRecord(999)
    );

    @BeforeAll
    static void initService() throws SQLException {
        service = new SQLiteService(DB_NAME);
        service.createConnection();

        service.registerTable(TestRecord.class);
        service.insert(testRecords);
    }

    @AfterAll
    static void deleteService() throws SQLException {
        service.eradicate();
    }

    @Test
    void getRecordsTest() throws Exception {
        List<TestRecord> records = new TableReader<>(TestRecord.class, service).call();

        assertFalse(records.isEmpty());
        assertEquals(testRecords.size(), records.size());
        assertTrue(records.contains(testRecords.get(1)));
    }

    @Test
    void getRecordsUsingThreadingTest() throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newFixedThreadPool(2);
        Future<List<TestRecord>> future = es.submit(new TableReader<>(TestRecord.class, service));

        List<TestRecord> records = future.get();

        assertFalse(records.isEmpty());
        assertEquals(testRecords.size(), records.size());
        assertTrue(records.contains(testRecords.get(1)));
    }
}
