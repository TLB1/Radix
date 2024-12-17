package tlb1.radix.database;

import tlb1.radix.database.annotations.DBField;
import tlb1.radix.database.annotations.RecordExclude;
import tlb1.radix.database.annotations.RecordInclude;

import java.lang.reflect.Field;
import java.util.function.Function;

public class FieldUsePredicate {
    private final Function<Field, Boolean> predicate;

    public FieldUsePredicate(Function<Field, Boolean> predicate) {
        this.predicate = predicate;
    }

    public boolean shouldUse(Field field) {
        return predicate.apply(field);
    }

    public static FieldUsePredicate DEFAULT_ALL = new FieldUsePredicate(
            (field) -> !field.isAnnotationPresent(RecordExclude.class));
    public static FieldUsePredicate DEFAULT_NONE = new FieldUsePredicate(
            (field) -> field.isAnnotationPresent(RecordInclude.class) || field.isAnnotationPresent(DBField.class));
}
