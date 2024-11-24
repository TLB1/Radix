package tlb1.radix.util;

import java.util.*;

/**
 * An object that maps keys to multiple values.
 * @param <K> The key type
 * @param <V> The type of value
 */
public class Multimap<K, V> {
    private final Map<K, List<V>> map;

    /**
     * The only and default constructor.
     * uses a HashMap for mapping
     */
    public Multimap(){
       map = new HashMap<>();
    }

    /**
     * If the specified key is not already associated with a value (or is mapped to null),
     * creates a map entry. Otherwise, adds to this list
     * @param key the key whose associated value is being put
     * @param value the value to put
     */
    public void put(K key, V value){
        List<V> values = map.computeIfAbsent(key, k -> new ArrayList<>());
        values.add(value);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or null if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped,
     * or null if this map contains no mapping for the key
     */
    public List<V> get(K key){
        return map.get(key);
    }

    /**
     * Returns true if this map contains a mapping for the specified key
     * @param key  key whose presence in this map is to be tested
     * @return true if this map contains a mapping for the specified key
     */
    public boolean containsKey(K key){
        return map.containsKey(key);
    }

    /**
     * Returns a Set view of the keys contained in this map
     * @return a Set view of the keys contained in this map
     */
    public Set<K> keySet(){
        return map.keySet();
    }
}
