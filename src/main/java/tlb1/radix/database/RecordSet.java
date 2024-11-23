package tlb1.radix.database;


import java.util.List;

public class RecordSet<T extends Record> {
    private final DBService service;
    private final Class<T> type;

    public RecordSet(DBService service, Class<T> type) {
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
