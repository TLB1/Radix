package tlb1.radix.database.annotations;

import java.lang.annotation.*;

/**
 * Defines that a field of a record cannot be overwritten in the database context
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ReadOnly {
}
