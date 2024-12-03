package tlb1.radix.database.annotations;

import java.lang.annotation.*;

/**
 * Fields to exclude using the default record field predicate
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RecordExclude {
}
