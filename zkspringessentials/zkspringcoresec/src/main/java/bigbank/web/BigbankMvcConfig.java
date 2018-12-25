package bigbank.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Controller
@EnableWebMvc
public class BigbankMvcConfig extends WebMvcConfigurerAdapter {

	@GetMapping("/listAccounts.html")
	public String listAccounts() {
		return "listAccounts";
	}

	public void configureViewResolvers(ViewResolverRegistry viewResolverRegistry) {
		viewResolverRegistry.viewResolver(new InternalResourceViewResolver("/WEB-INF/zul/", ".zul"));
	}
}
