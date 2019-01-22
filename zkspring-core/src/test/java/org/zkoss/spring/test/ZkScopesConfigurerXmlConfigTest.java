package org.zkoss.spring.test;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath:org/zkoss/spring/test/config/zkScopesConfigurerTestContext.xml"})
public class ZkScopesConfigurerXmlConfigTest extends AbstractZkScopesConfigurerTest {
}
