package tlb1.radix.database.services;

import tlb1.radix.database.annotations.Identifier;
import tlb1.radix.database.records.Record;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RecordCache<T extends Record> {
    private final Map<Object, T> cache;
    private final Field identifier;

    public RecordCache(Collection<T> records) {
        cache = new HashMap<>();
        identifier = Arrays.stream(records.stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("Collection is empty"))

                        .getClass().getFields()).filter((field -> field.isAnnotationPresent(Identifier.class))).findFirst()
                .orElseThrow(() -> new IllegalStateException("Record type does not have a valid identifier"));

        records.forEach(this::tryPut);
    }

    private void tryPut(T record) {
        if (record == null) return;
        try {
            cache.put(identifier.get(record), record);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public T get(Object id) {
        return cache.get(id);
    }

    public int size() {
        return cache.size();
    }

    public void clear() {
        cache.clear();
    }

}
