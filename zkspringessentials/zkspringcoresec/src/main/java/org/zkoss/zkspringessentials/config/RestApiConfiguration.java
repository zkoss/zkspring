package org.zkoss.zkspringessentials.config;

import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan({"org.zkoss.zkspringessentials.api"})
public class RestApiConfiguration {
}
