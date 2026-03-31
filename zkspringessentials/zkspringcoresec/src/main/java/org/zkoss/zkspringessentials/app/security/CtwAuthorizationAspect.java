package org.zkoss.zkspringessentials.app.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * AspectJ Compile-Time Weaving (CTW) aspect for {@link RequiresRole} on ZK ViewModels.
 *
 * <p>This class is intentionally NOT annotated with {@code @Component}.
 * The aspectj-maven-plugin weaves this advice directly into target class bytecode at
 * compile time.  No CGLIB subclass is generated at runtime, so ZK's
 * {@code @BindingParam} parameter annotations are preserved exactly as written.
 *
 * <p>Contrast with {@link ZkAuthorizationAspect}: that aspect is a Spring-managed
 * {@code @Component} and uses Spring AOP (runtime proxies). Placing it on a ViewModel
 * causes CGLIB to strip parameter annotations.  This CTW aspect avoids that problem
 * entirely because the advice is already in the bytecode before Spring ever sees the class.
 *
 * <p>The pointcut is scoped to {@code BigbankViewModel2} so that the existing Spring AOP
 * aspect and this CTW aspect do not double-execute on the same join points.
 */
@Aspect
public class CtwAuthorizationAspect {

    @Before("@annotation(requiresRole) && within(org.zkoss.zkspringessentials.bigbank.web.BigbankViewModel2)")
    public void checkRole(JoinPoint jp, RequiresRole requiresRole) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user");
        }

        for (String requiredRole : requiresRole.value()) {
            boolean granted = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(requiredRole));
            if (granted) {
                return;
            }
        }

        throw new AccessDeniedException(
                "Access denied: requires one of " + String.join(", ", requiresRole.value())
                + " but user only has " + auth.getAuthorities());
    }
}
