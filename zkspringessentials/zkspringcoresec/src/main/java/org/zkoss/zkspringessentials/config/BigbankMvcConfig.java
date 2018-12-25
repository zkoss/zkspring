package org.zkoss.zkspringessentials.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.http.HttpServletRequest;

@Controller
@EnableWebMvc
@ComponentScan("bigbank")
public class BigbankMvcConfig extends WebMvcConfigurerAdapter {

	@GetMapping("/listAccounts.html")
	public String listAccounts() {
		return "listAccounts";
	}

	public void configureViewResolvers(ViewResolverRegistry viewResolverRegistry) {
		viewResolverRegistry.viewResolver(new InternalResourceViewResolver("/WEB-INF/zul/", ".zul"));
	}
}
