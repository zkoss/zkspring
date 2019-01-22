package org.zkoss.spring.test;

import org.springframework.test.context.ContextConfiguration;
import org.zkoss.spring.test.config.ZkScopesConfigurerConfig;

//Run the same test with 3 different context configurations
@ContextConfiguration(classes = {ZkScopesConfigurerConfig.class})
public class ZkScopesConfigurerJavaConfigTest extends AbstractZkScopesConfigurerTest {
}
