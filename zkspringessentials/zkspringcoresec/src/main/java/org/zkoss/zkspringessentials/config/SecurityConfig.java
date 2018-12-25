package org.zkoss.zkspringessentials.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.AbstractSecurityExpressionHandler;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.zkoss.zkspringessentials.acl.InMemoryAclService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.headers().frameOptions().sameOrigin();
		http.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/secure/extreme/**").hasRole("SUPERVISOR")
				.antMatchers(HttpMethod.GET, "/secure/**").hasRole("USER")
				.and()
				.formLogin()
				.loginProcessingUrl("/login")
				.loginPage("/login.zul")
				.failureUrl("/login.zul?login_error=1")
				.and()
				.logout()
				.logoutSuccessUrl("/index.zul")
				.invalidateHttpSession(true)
				.and();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		//for demo purposes only
		auth.inMemoryAuthentication()
				.withUser("rod").roles("SUPERVISOR", "USER", "TELLER").password("a564de63c2d0da68cf47586ee05984d7").and()
				.withUser("dianne").roles("USER", "TELLER").password("65d15fe9156f9c4bbffd98085992a44e").and()
				.withUser("scott").roles("USER").password("2b58af6dddbd072ed27ffc86725d7d3a").and()
				.withUser("peter").roles("USER").password("22b5c9accc6e1ba628cedc63a72d57f8").and()
				.passwordEncoder(new MessageDigestPasswordEncoder("MD5"));
	}

	@Bean
	public InMemoryAclService aclService() {
		return new InMemoryAclService();
	}
}
