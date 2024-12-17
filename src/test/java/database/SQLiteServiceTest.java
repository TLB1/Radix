package database;

import database.records.NotATestRecord;
import database.records.TestRecord;
import org.junit.jupiter.api.*;
import tlb1.radix.database.TableRegistrationPredicate;
import tlb1.radix.database.services.DBService;
import tlb1.radix.database.services.SQLiteService;
import tlb1.radix.database.services.TableReader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteServiceTest {
    private static final String DB_NAME = "test.db";

    private DBService service;

    @BeforeEach
    void resetService() throws SQLException {
        service = new SQLiteService(DB_NAME);
        service.createConnection();
        service.setLogLevel(Level.OFF);
    }

    @AfterEach
    void eradicateService() throws SQLException {
        ((SQLiteService)service).eradicate();
    }

    @Test
    void testRegisterTable() {
        service.registerTable(TestRecord.class);

        assertTrue(service.tableExists(TestRecord.class));
        assertTrue(service.isTableEmpty(TestRecord.class));
    }

    @Test
    void testInsertRecord() {
        service.registerTable(TestRecord.class);
        service.insert(new TestRecord(10));

        assertTrue(service.hasRecords(TestRecord.class));
    }

    @Test
    void testInsertRecords() {
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
    void testInsertManyRecords() {
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
    void testDeleteRecord() {
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
        TestRecord record = new TestRecord(2002);
        service.insert(record);
        service.insert(List.of(
                new TestRecord(1),
                new TestRecord(2)
        ));
        new TableReader<>(TestRecord.class, service).call().forEach(System.out::println);
        // Unchanged - (equal)
        assertTrue(new TableReader<>(TestRecord.class, service).call().contains(record));
        record.value = 3;
        // Object Updated - (not equal)
        assertFalse(new TableReader<>(TestRecord.class, service).call().contains(record));
        service.update(record);
        // DB Updated - (equal)
        assertTrue(new TableReader<>(TestRecord.class, service).call().contains(record));
    }

    @Test
    void testInsertRecordWithoutRegisteringTable() throws SQLException {
        service.insert(new TestRecord(10));

        assertTrue(service.hasRecords(TestRecord.class));
    }

    @Test
    void testInsertRecordsWithoutRegisteringTable() {
        service.insert(List.of(
                new TestRecord(10),
                new TestRecord(20),
                new TestRecord(30000)
        ));

        assertTrue(service.hasRecords(TestRecord.class));
        assertEquals(3, service.getRecordCount(TestRecord.class));
    }
    @Test
    void testInsertRecordsThatAreNoRecord() {
        assertThrows(IllegalArgumentException.class, ()->{
            service.insert(List.of(
                    new NotATestRecord(),
                    new NotATestRecord(),
                    new NotATestRecord()
            ));
        });

        assertFalse(service.tableExists(NotATestRecord.class));
        assertFalse(service.hasRecords(NotATestRecord.class));
    }

    @Test
    void testInsertRecordsWithAltPredicate() {
        service.setTableRegistrationPredicate(TableRegistrationPredicate.RECORD_ONLY);

        assertDoesNotThrow(()->{
            service.insert(List.of(
                    new NotATestRecord(),
                    new NotATestRecord(),
                    new NotATestRecord()
            ));
        });

        assertTrue(service.tableExists(NotATestRecord.class));
        assertTrue(service.hasRecords(NotATestRecord.class));
    }

    @Test
    void testInsertRecordsWithMagic() throws SQLException {
        eradicateService();
        service = new SQLiteService(DB_NAME, true);

        assertDoesNotThrow(()->{
            service.insert(List.of(
                    new NotATestRecord(),
                    new NotATestRecord(),
                    new NotATestRecord()
            ));
        });

        assertTrue(service.tableExists(NotATestRecord.class));
        assertTrue(service.hasRecords(NotATestRecord.class));
    }
}
