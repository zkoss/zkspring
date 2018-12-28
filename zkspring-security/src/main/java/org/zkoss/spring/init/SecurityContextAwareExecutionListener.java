package org.zkoss.spring.init;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.util.ExecutionCleanup;
import org.zkoss.zk.ui.util.ExecutionInit;

import java.util.List;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

/**
 * ZK Listener to fill/cleanup Springs {@link SecurityContextHolder} for executions outside Spring's security filter chain.
 * e.g. Websocket requests or executions activated from background threads (server push / eventqueues / manual activation).<br/>
 * <br/>
 * This implementation will retrieve the current {@link SecurityContext} stored in the Http Session using Spring's
 * default attribute name {@link org.springframework.security.web.context.HttpSessionSecurityContextRepository#SPRING_SECURITY_CONTEXT_KEY}<br/>
 * The listener can be enabled/disabled via the library property 'org.zkoss.spring.init.SecurityContextAwareExecutionListener.enabled'.<br/>
 * After disabling a custom implementation can be provided e.g. by overriding the {@link #loadSecurityContext(Execution)} method.
 * @author Robert
 * @see org.zkoss.zk.ui.util.ExecutionInit
 * @see org.zkoss.zk.ui.util.ExecutionCleanup
 * @see SecurityWebAppInit#SECURITY_CONTEXT_AWARE_EXECUTION_LISTENER_ENABLED
 * @since 4.0.0
 */
public class SecurityContextAwareExecutionListener implements ExecutionInit, ExecutionCleanup {
	private static final String ZK_SPRING_ORIGINAL_SECURITY_CONTEXT = "ZK_SPRING_ORIGINAL_SECURITY_CONTEXT";

	public void init(Execution exec, Execution parent) throws Exception {
		final SecurityContext originalSecurityContext = SecurityContextHolder.getContext();
		if (parent == null && originalSecurityContext.getAuthentication() == null) {
			final SecurityContext ctx = loadSecurityContext(exec);
			if (ctx != null) {
				SecurityContextHolder.setContext(ctx);
				exec.setAttribute(ZK_SPRING_ORIGINAL_SECURITY_CONTEXT, originalSecurityContext);
			}
		}
	}

	public void cleanup(Execution exec, Execution parent, List<Throwable> errs) throws Exception {
		if (parent == null) {
			final SecurityContext originalSecurityContext = (SecurityContext) exec.removeAttribute(ZK_SPRING_ORIGINAL_SECURITY_CONTEXT);
			if (originalSecurityContext != null) {
				if (SecurityContextHolder.createEmptyContext().equals(originalSecurityContext)) {
					SecurityContextHolder.clearContext();
				} else {
					SecurityContextHolder.setContext(originalSecurityContext);
				}
			}
		}
	}

	protected SecurityContext loadSecurityContext(Execution execution) {
		return (SecurityContext) execution.getSession().getAttribute(SPRING_SECURITY_CONTEXT_KEY);
	}
}
