package tlb1.radix.database;

import java.lang.annotation.*;

/**
 * Are used to declare the ID field of a record type,
 * these are used in references and are required vor relational datastructures
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)

public @interface Identifier {
}
