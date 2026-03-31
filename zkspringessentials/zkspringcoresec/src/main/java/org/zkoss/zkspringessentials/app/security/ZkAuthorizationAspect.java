package org.zkoss.zkspringessentials.app.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring AOP aspect that enforces {@link RequiresRole} on any Spring-managed bean method.
 *
 * <p>When a method annotated with {@code @RequiresRole} is invoked, this aspect checks
 * whether the current authenticated user holds at least one of the declared roles.
 * If not, it throws {@link AccessDeniedException} before the method body executes.
 *
 * <p><b>CGLIB proxy warning:</b> Applying this aspect to a ZK MVVM ViewModel causes
 * Spring to CGLIB-proxy the ViewModel class.  ZK's AnnotationBinder calls
 * {@code vm.getClass().getDeclaredMethods()} and may miss ZK annotations
 * ({@code @Command}, {@code @BindingParam}) on the proxy's generated overrides.
 * Run the application and observe whether commands still fire correctly.
 */
@Aspect
@Component
public class ZkAuthorizationAspect {

    // BigbankViewModel2 is excluded: CTW (CtwAuthorizationAspect) handles it at compile time.
    // Including it here would cause Spring AOP to CGLIB-proxy it, stripping @BindingParam.
    @Before("@annotation(requiresRole) && !within(org.zkoss.zkspringessentials.bigbank.web.BigbankViewModel2)")
    public void checkRole(JoinPoint jp, RequiresRole requiresRole) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user");
        }

        for (String requiredRole : requiresRole.value()) {
            boolean granted = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(requiredRole));
            if (granted) {
                return; // at least one role matched — allow
            }
        }

        throw new AccessDeniedException(
                "Access denied: requires one of " + String.join(", ", requiresRole.value())
                + " but user only has " + auth.getAuthorities());
    }
}
