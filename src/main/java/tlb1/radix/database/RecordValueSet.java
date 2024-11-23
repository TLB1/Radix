package tlb1.radix.database;

import tlb1.radix.util.Multimap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RecordValueSet {
    Multimap<String, String> recordValues;
    List<Iterator<String>> valueIterators;

    public RecordValueSet(Multimap<String, String> recordValues){
        this.recordValues = recordValues;
    }

    public void prepare(){
        Set<String> keys = recordValues.keySet();
        valueIterators = new ArrayList<>();
        for(String key : keys){
            valueIterators.add(recordValues.get(key).iterator());
        }
    }

    public Set<String> getColumns(){
        return recordValues.keySet();
    }

    public List<String> nextRecord(){
        List<String> values = new ArrayList<>();
        valueIterators.forEach((stringIterator -> {
            values.add(stringIterator.next());
        }));
        return values;
    }

    public boolean hasNext(){
        return valueIterators.get(0).hasNext();
    }

}
