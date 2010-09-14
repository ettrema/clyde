package com.bradmcevoy.web.security;

import com.bradmcevoy.web.security.PermissionRecipient.Role;
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
