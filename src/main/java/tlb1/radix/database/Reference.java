package tlb1.radix.database;

import java.lang.annotation.*;

/**
 * Are used to refer to the ID field of another record type,
 * these are used for seamless translation between java objects and database records
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Reference {
}
