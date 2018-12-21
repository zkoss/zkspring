package org.zkoss.spring.config;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.zkoss.spring.web.context.request.*;

/**
 * Register zk specific scopes
 *
 * @author robert
 * @since 4.0
 */
public class ZkScopesConfigurer extends CustomScopeConfigurer {
	public ZkScopesConfigurer() {
		addScope("webapp", new ApplicationScope());
		addScope("desktop", new DesktopScope());
		addScope("page", new PageScope());
		addScope("idspace", new IdSpaceScope());
		addScope("execution", new ExecutionScope());
	}
}
