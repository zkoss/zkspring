package org.zkoss.zkspringessentials.bigbank.security;

import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Phase;
import org.zkoss.bind.PhaseListener;
import org.zkoss.spring.SpringUtil;
import org.zkoss.zkspringessentials.app.SecurityUtil;

import java.util.Optional;

/**
 * Global MVVM command interceptor that enforces centralized authorization rules.
 *
 * <p>Registered in {@code zk.xml} via {@code org.zkoss.bind.PhaseListener.class}.  ZK invokes
 * {@link #prePhase} before every {@code @Command} / {@code @GlobalCommand} executes.  Rules are
 * fetched from the Spring-managed {@link CommandAuthorizationRules} bean — no {@code @PreAuthorize}
 * annotations are needed on the ViewModel.
 *
 * <p>ViewModels that have no registered rule are allowed through unchanged, so existing examples
 * are unaffected.
 */
public class AuthPhaseListener implements PhaseListener {

    @Override
    public void prePhase(Phase phase, BindContext ctx) {
        if (phase != Phase.COMMAND && phase != Phase.GLOBAL_COMMAND) return;

        Object vm = ctx.getBinder().getViewModel();
        if (vm == null) return;

        String commandName = ctx.getCommandName();

        ApplicationContext appCtx = SpringUtil.getApplicationContext();
        if (appCtx == null) return;

        CommandAuthorizationRules rules = appCtx.getBean(CommandAuthorizationRules.class);
        Optional<String> requiredRoles = rules.getRequiredRoles(vm.getClass(), commandName);

        if (requiredRoles.isEmpty()) return;

        if (!SecurityUtil.isAnyGranted(requiredRoles.get())) {
            throw new AccessDeniedException(
                    "Access denied for command '" + commandName + "' on " + vm.getClass().getSimpleName());
        }
    }

    @Override
    public void postPhase(Phase phase, BindContext ctx) {
    }
}
