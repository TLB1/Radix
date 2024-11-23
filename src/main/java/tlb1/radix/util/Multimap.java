package tlb1.radix.util;

import java.util.*;

public class Multimap<K, V> {
    private final Map<K, List<V>> map;

    public Multimap(){
       map = new HashMap<>();
    }

    public void put(K key, V value){
        List<V> values = map.computeIfAbsent(key, k -> new ArrayList<>());
        values.add(value);
    }

    public List<V> get(K key){
        return map.get(key);
    }

    public boolean containsKey(K key){
        return map.containsKey(key);
    }

    public Set<K> keySet(){
        return map.keySet();
    }
}
