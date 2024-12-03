package tlb1.radix.database.annotations;

import java.lang.annotation.*;

/**
 * Fields to include using the default record field predicate
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RecordInclude {
}
