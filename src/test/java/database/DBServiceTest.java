package database;

import org.junit.jupiter.api.*;
import tlb1.radix.database.services.DBService;
import tlb1.radix.database.services.SQLiteService;
import tlb1.radix.database.services.TableReader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class DBServiceTest {
    private static final String DB_NAME = "test.db";

    private DBService service;

    @BeforeEach
    void resetService() throws SQLException {
        service = new SQLiteService(DB_NAME);
        service.createConnection();
    }

    @AfterEach
    void eradicateService() throws SQLException {
        ((SQLiteService)service).eradicate();
    }

    @Test
    void testRegisterTable() throws SQLException {
        service.registerTable(TestRecord.class);

        assertTrue(service.tableExists(TestRecord.class));
        assertTrue(service.isTableEmpty(TestRecord.class));
    }

    @Test
    void testInsertRecord() throws SQLException {
        service.registerTable(TestRecord.class);
        service.insert(new TestRecord(10));

        assertTrue(service.hasRecords(TestRecord.class));
    }

    @Test
    void testInsertRecords() throws SQLException {
        service.registerTable(TestRecord.class);
        service.insert(List.of(
                new TestRecord(10),
                new TestRecord(20),
                new TestRecord(30000)
        ));

        assertTrue(service.hasRecords(TestRecord.class));
        assertEquals(3, service.getRecordCount(TestRecord.class));
    }

    @Test
    void testInsertManyRecords() throws SQLException {
        int count = 500;
        service.registerTable(TestRecord.class);
        List<TestRecord> records = new ArrayList<>(count);


        for (int i = 0; i < count; i++) {
            records.add(new TestRecord(ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE)));
        }
        service.insert(records);

        assertTrue(service.hasRecords(TestRecord.class));
        assertEquals(count, service.getRecordCount(TestRecord.class));
    }

    @Test
    void testDeleteRecord() throws SQLException {
        service.registerTable(TestRecord.class);
        TestRecord record = new TestRecord(2000);
        service.insert(record);
        service.insert(List.of(
                new TestRecord(25),
                new TestRecord(500),
                new TestRecord(44000)
        ));
        service.delete(record);

        assertEquals(3, service.getRecordCount(TestRecord.class));
    }

    @Test
    void testUpdateRecord() throws Exception {
        service.registerTable(TestRecord.class);
        TestRecord record = new TestRecord(2000);
        service.insert(record);
        service.insert(List.of(
                new TestRecord(1),
                new TestRecord(2)
        ));

        // Unchanged - (equal)
        assertTrue(new TableReader<>(TestRecord.class, service).call().contains(record));
        record.value = 3;
        // Object Updated - (not equal)
        assertFalse(new TableReader<>(TestRecord.class, service).call().contains(record));
        service.update(record);
        // DB Updated - (equal)
        assertTrue(new TableReader<>(TestRecord.class, service).call().contains(record));
    }
}
