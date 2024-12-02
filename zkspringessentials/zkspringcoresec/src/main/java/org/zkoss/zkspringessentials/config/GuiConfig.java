package org.zkoss.zkspringessentials.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Controller
@EnableWebMvc
@ComponentScan("org.zkoss.zkspringessentials.bigbank")
public class GuiConfig implements WebMvcConfigurer {

	@GetMapping("/listAccounts.html")
	public String listAccounts() {
		return "listAccounts";
	}

	public void configureViewResolvers(ViewResolverRegistry viewResolverRegistry) {
		viewResolverRegistry.viewResolver(new InternalResourceViewResolver("/WEB-INF/zul/", ".zul"));
	}
}
