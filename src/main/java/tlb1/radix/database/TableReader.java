package tlb1.radix.database;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import java.util.concurrent.Callable;

public class TableReader<T> implements Callable<List<T>> {

    private final Class<T> type;
    private final DBService connectedService;

    static protected Map<Class<?>, Method> typeConversions;

    static {
        try {
            typeConversions = new HashMap<>(Map.of(
                    String.class,   ResultSet.class.getMethod("getString", String.class),
                    Short.class,    ResultSet.class.getMethod("getShort", String.class),
                    Integer.class,  ResultSet.class.getMethod("getInt", String.class),
                    int.class,      ResultSet.class.getMethod("getInt", String.class),
                    Long.class,     ResultSet.class.getMethod("getLong", String.class),
                    Float.class,    ResultSet.class.getMethod("getFloat", String.class),
                    Double.class,   ResultSet.class.getMethod("getDouble", String.class),
                    Date.class,     ResultSet.class.getMethod("getDate", String.class),
                    Boolean.class,  ResultSet.class.getMethod("getBoolean", String.class),
                    Byte.class,     ResultSet.class.getMethod("getByte", String.class)
            ));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public TableReader(Class<T> type, DBService service) {
        this.connectedService = service;
        this.type = type;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public List<T> call() throws Exception {
        ResultSet dataset = connectedService.retrieveAll(type);
        List<T> records = new ArrayList<>();

        while(dataset.next()){
            T record = type.getConstructor().newInstance();
            Arrays.stream(type.getFields()).filter((field -> field.isAnnotationPresent(DBField.class))).forEach((field -> {
                try {
                    setField(record, field, dataset);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                   throw new IllegalStateException("Target method cannot be invoked");
                } catch (IllegalAccessException | SQLException e) {
                    throw new IllegalStateException("Cannot access all fields of target class");
                }
            }));
            records.add(record);
        }

        return records;
    }

    /**
     * @throws IllegalAccessException If the accessor isn't public
     * @throws InvocationTargetException If the underlying method throws an SQLException
     */
    public void setField(T record, Field field, ResultSet dataSet) throws InvocationTargetException, IllegalAccessException, SQLException {
        if(field.getType().equals(UUID.class)) field.set(record, UUID.fromString(dataSet.getString(field.getName())));
        else field.set(record, typeConversions.get(field.getType()).invoke(dataSet, field.getName()));
    }
}
