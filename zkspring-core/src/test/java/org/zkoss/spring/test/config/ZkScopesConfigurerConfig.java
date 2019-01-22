package org.zkoss.spring.test.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.zkoss.spring.config.ZkScopesConfigurer;

@Configuration
@Import(ZkScopesConfigurer.class)
public class ZkScopesConfigurerConfig {
}
