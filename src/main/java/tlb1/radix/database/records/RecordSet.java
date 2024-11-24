package tlb1.radix.database.records;


import tlb1.radix.database.services.SQLiteService;
import tlb1.radix.database.services.TableReader;

import java.util.List;

public class RecordSet<T extends Record> {
    private final SQLiteService service;
    private final Class<T> type;

    public RecordSet(SQLiteService service, Class<T> type) {
        this.service = service;
        this.type = type;
    }

    public List<T> collect(){
        try{
            return getRecords();
        }catch (Exception e){
            throw new RuntimeException("Could not collect records");
        }
    }

    private List<T> getRecords() throws Exception {
        TableReader<T> tableReader = new TableReader<>(type, service);
        return tableReader.call();
    }
}
