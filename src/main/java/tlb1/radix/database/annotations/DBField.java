package tlb1.radix.database.annotations;

import tlb1.radix.database.FieldType;

import java.lang.annotation.*;

/**
 * The annotation used to mark a field of a record as a database field
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DBField {

    /**
     * @return the type of field in the DB context
     */
    FieldType value();
}
