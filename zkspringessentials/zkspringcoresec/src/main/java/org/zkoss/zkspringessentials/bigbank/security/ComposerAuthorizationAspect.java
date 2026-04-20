package org.zkoss.zkspringessentials.bigbank.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.zkoss.zkspringessentials.app.SecurityUtil;
import org.zkoss.zkspringessentials.bigbank.web.BigbankComposer2;

/**
 * Centralized authorization aspect for MVC Composers — the ZK equivalent of
 * Spring Security's {@code requestMatchers("/admin/**")}.
 *
 * <p>Rules are declared as AspectJ pointcut expressions keyed on class or package,
 * with no {@code @PreAuthorize} required on any Composer method. Adding a new rule
 * means adding one {@code @Before} method here; the Composer itself stays clean.
 *
 * <p>This aspect is woven at compile time by {@code aspectj-maven-plugin}. No Spring
 * bean wiring is needed because {@link SecurityUtil} reads directly from
 * {@code SecurityContextHolder}.
 *
 * <p>Example of scaling to a package:
 * <pre>
 *   // All @Listen methods in the admin package require ROLE_ADMIN
 *   &#64;Before("execution(&#64;Listen * com.example.admin..*.*(..))")
 *   public void requireAdmin(JoinPoint jp) { ... }
 * </pre>
 */
@Aspect
public class ComposerAuthorizationAspect {

    /**
     * Any {@code @Listen}-annotated method on {@link BigbankComposer2} requires
     * ROLE_SUPERVISOR or ROLE_TELLER.
     */
    @Before("execution(@org.zkoss.zk.ui.select.annotation.Listen * " +
            "org.zkoss.zkspringessentials.bigbank.web.BigbankComposer2.*(..))")
    public void requireTellerOrSupervisor(JoinPoint jp) {
        if (!SecurityUtil.isAnyGranted("ROLE_SUPERVISOR,ROLE_TELLER")) {
            throw new AccessDeniedException(
                    "Access denied for: " + jp.getSignature().toShortString());
        }
    }

    // -------------------------------------------------------------------------
    // MVVM ViewModel example (reference — not active)
    //
    // The same pattern works for MVVM ViewModels: swap @Listen for @Command.
    // A single aspect class can enforce rules for both MVC and MVVM in one place.
    //
    // @Before("execution(@org.zkoss.bind.annotation.Command * " +
    //         "org.zkoss.zkspringessentials.bigbank.web.BigbankViewModel3.*(..))")
    // public void requireTellerOrSupervisorForVM(JoinPoint jp) {
    //     if (!SecurityUtil.isAnyGranted("ROLE_SUPERVISOR,ROLE_TELLER")) {
    //         throw new AccessDeniedException(
    //                 "Access denied for: " + jp.getSignature().toShortString());
    //     }
    // }
    //
    // Package-level rule (covers all ViewModels under an admin package):
    // @Before("execution(@org.zkoss.bind.annotation.Command * com.example.admin..*.*(..))")
    // public void requireAdminForAdminPackageVMs(JoinPoint jp) { ... }
    // -------------------------------------------------------------------------
}
