package database;

import database.records.MagicTestRecord;
import database.records.NotAMagicTestRecord;
import database.records.NotATestRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqliteServiceMagicTest {
    private static final String DB_NAME = "magic-test.db";

    private DBService service;

    @BeforeEach
    void resetService() throws SQLException {
        service = new SQLiteService(DB_NAME, true);
        service.setLogLevel(Level.ALL);
    }

    @AfterEach
    void eradicateService() throws SQLException {
        ((SQLiteService)service).eradicate();
    }

    @Test
    void testRegisterTable() {
        service.registerTable(MagicTestRecord.class);

        assertTrue(service.tableExists(MagicTestRecord.class));
        assertTrue(service.isTableEmpty(MagicTestRecord.class));
    }

    @Test
    void testInsertRecord() {
        service.insert(new MagicTestRecord(10));

        assertTrue(service.hasRecords(MagicTestRecord.class));
    }

    @Test
    void testInsertRecords() {
        service.insert(List.of(
                new MagicTestRecord(10),
                new MagicTestRecord(20),
                new MagicTestRecord(30000)
        ));

        assertTrue(service.hasRecords(MagicTestRecord.class));
        assertEquals(3, service.getRecordCount(MagicTestRecord.class));
    }

    @Test
    void testInsertManyRecords() {
        int count = 500;
        List<MagicTestRecord> records = new ArrayList<>(count);


        for (int i = 0; i < count; i++) {
            records.add(new MagicTestRecord(ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE)));
        }
        service.insert(records);

        assertTrue(service.hasRecords(MagicTestRecord.class));
        assertEquals(count, service.getRecordCount(MagicTestRecord.class));
    }

    @Test
    void testDeleteRecord() {
        MagicTestRecord record = new MagicTestRecord(2000);
        service.insert(record);
        service.insert(List.of(
                new MagicTestRecord(25),
                new MagicTestRecord(500),
                new MagicTestRecord(44000)
        ));
        service.delete(record);

        assertEquals(3, service.getRecordCount(MagicTestRecord.class));
    }

    @Test
    void testUpdateRecord() throws Exception {
        MagicTestRecord record = new MagicTestRecord(2002);
        service.insert(record);
        service.insert(List.of(
                new MagicTestRecord(1),
                new MagicTestRecord(2)
        ));
        new TableReader<>(MagicTestRecord.class, service).call().forEach(System.out::println);
        // Unchanged - (equal)
        assertTrue(new TableReader<>(MagicTestRecord.class, service).call().contains(record));
        record.value = 3;
        // Object Updated - (not equal)
        assertFalse(new TableReader<>(MagicTestRecord.class, service).call().contains(record));
        service.update(record);
        // DB Updated - (equal)
        assertTrue(new TableReader<>(MagicTestRecord.class, service).call().contains(record));
    }

    @Test
    void testInsertRecordWithoutRegisteringTable() {
        service.insert(new MagicTestRecord(10));

        assertTrue(service.hasRecords(MagicTestRecord.class));
    }

    @Test
    void testInsertRecordsWithoutRegisteringTable() {
        service.insert(List.of(
                new MagicTestRecord(10),
                new MagicTestRecord(20),
                new MagicTestRecord(30000)
        ));

        assertTrue(service.hasRecords(MagicTestRecord.class));
        assertEquals(3, service.getRecordCount(MagicTestRecord.class));
    }
    @Test
    void testInsertRecordsThatAreNoRecord() {
        assertThrows(IllegalArgumentException.class, ()->{
            service.insert(List.of(
                    new NotAMagicTestRecord(),
                    new NotAMagicTestRecord(),
                    new NotAMagicTestRecord()
            ));
        });

        assertFalse(service.tableExists(NotAMagicTestRecord.class));
        assertFalse(service.hasRecords(NotAMagicTestRecord.class));
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
}
