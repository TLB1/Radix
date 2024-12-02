package tlb1.radix.database;

import tlb1.radix.database.annotations.TableName;
import tlb1.radix.database.records.Record;

import java.util.function.Function;

public class TableRegistrationPredicate {
    private final Function<Class<?>, Boolean> predicate;

    public TableRegistrationPredicate(Function<Class<?>, Boolean> predicate) {
        this.predicate = predicate;
    }
    public boolean shouldRegister(Class<?> type){
        return predicate.apply(type);
    }

    public static final TableRegistrationPredicate IMPLEMENTED_RECORD_ONLY = new TableRegistrationPredicate(
            (type)-> type.isAssignableFrom(Record.class) && type.isAnnotationPresent(TableName.class));

    public static final TableRegistrationPredicate RECORD_ONLY = new TableRegistrationPredicate(
            (type)-> type.isAssignableFrom(Record.class));
}
