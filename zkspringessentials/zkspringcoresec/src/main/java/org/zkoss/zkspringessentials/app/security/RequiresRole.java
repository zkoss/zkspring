package org.zkoss.zkspringessentials.app.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that the annotated method requires the caller to hold at least one
 * of the specified Spring Security role authorities.
 *
 * <p>Enforcement is handled by {@link ZkAuthorizationAspect}, which intercepts
 * the method call and throws {@link org.springframework.security.access.AccessDeniedException}
 * if the current user does not hold any of the declared roles.
 *
 * <p><b>Note:</b> Do NOT place this annotation on ZK MVVM ViewModel methods directly if
 * you observe broken @Command or @BindingParam behaviour — that indicates Spring AOP has
 * CGLIB-proxied the ViewModel and stripped ZK annotations.  In that case, move
 * @RequiresRole to a dedicated @Service method and delegate from the ViewModel.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    /** One or more Spring Security granted-authority strings (e.g. "ROLE_SUPERVISOR"). */
    String[] value();
}
