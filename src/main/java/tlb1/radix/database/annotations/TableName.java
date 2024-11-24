package tlb1.radix.database.annotations;

import java.lang.annotation.*;

/**
 * Defines the name of the database table associated with its target class
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableName {

    /**
     * @return the name of the database table
     */
    String value();
}
