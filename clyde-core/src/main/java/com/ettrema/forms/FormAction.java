package com.ettrema.forms;

import com.ettrema.web.security.PermissionRecipient.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to go on methods which can be executed from POST requests
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormAction {
	Role requiredRole() default Role.AUTHENTICATED;
}
