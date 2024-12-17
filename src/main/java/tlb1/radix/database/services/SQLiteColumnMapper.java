package tlb1.radix.database.services;

import tlb1.radix.database.Column;
import tlb1.radix.database.FieldType;
import tlb1.radix.database.FieldUsePredicate;
import tlb1.radix.database.annotations.DBField;
import tlb1.radix.database.annotations.Identifier;
import tlb1.radix.database.annotations.Reference;

import java.lang.reflect.Field;
import java.util.*;

public class SQLiteColumnMapper {
    private final Map<Class<?>, FieldType> conversion;

    public SQLiteColumnMapper(){
        conversion = new HashMap<>();
        
        conversion.put(Long.class, FieldType.LONG);
        conversion.put(Integer.class, FieldType.INTEGER);
        conversion.put(Short.class, FieldType.SHORT);

        conversion.put(Float.class, FieldType.FLOAT);
        conversion.put(Double.class, FieldType.DOUBLE);

        conversion.put(Boolean.class, FieldType.BOOLEAN);
        conversion.put(Byte.class, FieldType.BYTE);

        conversion.put(String.class, FieldType.TEXT);
        conversion.put(UUID.class, FieldType.UUID);
    }

    public Column get(Field field, FieldUsePredicate predicate) {
        if (!predicate.shouldUse(field)) return null;
        FieldType type = getType(field);
        String name = getName(field);
        return new Column(type, name, field);
    }

    public FieldType getType(Field field) {
        if (field.isAnnotationPresent(DBField.class) && !field.isAnnotationPresent(Reference.class)) return field.getAnnotation(DBField.class).value();
        if (field.isAnnotationPresent(Reference.class) && !field.isAnnotationPresent(Identifier.class)) {
            Field reference = getIdentifier(field.getType()).orElseThrow(() -> new IllegalStateException("Could not find identifier for record reference"));
            return getType(reference);
        }
        return getDefaultType(field);
    }

    public FieldType getDefaultType(Field field) {
        if (field.isAnnotationPresent(DBField.class)) return field.getAnnotation(DBField.class).value();
        return conversion.get(field.getType());
    }

    public Optional<Field> getIdentifier(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields()).filter(foreignField ->
                foreignField.isAnnotationPresent(Identifier.class)).findFirst();
    }

    /**
     * @return A generated field name used for database generation, works with relational datastructures
     */
    public String getName(Field field){
        if(!field.isAnnotationPresent(Reference.class)) return field.getName();

        Optional<Field> otherField = getIdentifier(field.getType());
        if(otherField.isEmpty()) return field.getName();
        String addedName = otherField.get().getName();
        addedName = addedName.substring(0, 1).toUpperCase() + addedName.substring(1);
        return field.getName() + addedName;
    }
}
