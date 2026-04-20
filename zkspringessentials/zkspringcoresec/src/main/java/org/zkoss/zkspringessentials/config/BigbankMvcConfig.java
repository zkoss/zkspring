package org.zkoss.zkspringessentials.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Controller
@EnableWebMvc
@ComponentScan("org.zkoss.zkspringessentials.bigbank")
public class BigbankMvcConfig implements WebMvcConfigurer {

	@GetMapping("/listAccounts.html")
	public String listAccounts() {
		return "listAccounts";
	}

	@GetMapping("/listAccounts2.html")
	public String listAccounts2() {
		return "listAccounts2";
	}

	@GetMapping("/listAccounts3.html")
	public String listAccounts3() {
		return "listAccounts3";
	}

	@GetMapping("/listAccounts4.html")
	public String listAccounts4() {
		return "listAccounts4";
	}

	@GetMapping("/listAccounts5.html")
	public String listAccounts5() {
		return "listAccounts5";
	}

	public void configureViewResolvers(ViewResolverRegistry viewResolverRegistry) {
		viewResolverRegistry.viewResolver(new InternalResourceViewResolver("/WEB-INF/zul/", ".zul"));
	}
}
