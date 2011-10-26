package com.ettrema.web.security;

import com.ettrema.web.security.PermissionRecipient.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation type to identify properties to be accessible by
 * BeanPropertySource
 *
 * This allows them to have their properties read from and written to
 * by PROPFIND and PROPPATCH.
 *
 * Note that to implement validation rules with feedback to the user you
 * can throw a PropertySetException from within your setters.
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanProperty {

    /**
     * Required role to read this property
     *
     * @return
     */
    Role readRole() default Role.AUTHOR;

    /**
     * Required role to change the property
     *
     * @return
     */
    Role writeRole() default Role.AUTHOR;
}
