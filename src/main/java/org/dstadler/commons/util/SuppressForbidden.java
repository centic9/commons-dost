package org.dstadler.commons.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to suppress forbidden-apis errors inside a whole class, a method, or a field.
 *
 * See https://github.com/policeman-tools/forbidden-apis for details and file 'forbidden.signatures.txt'
 * for our local rules as well as 'APMjavaProjects.gradle' for the configuration of the API checks.
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
public @interface SuppressForbidden {
    String reason();
}
