package org.zkoss.zkspringessentials.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.zkoss.zkspringessentials.app.acl.InMemoryAclService;

import java.util.Arrays;

/**
 * see https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter/
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.headers().frameOptions().sameOrigin();

        http.authorizeRequests()
                .requestMatchers(HttpMethod.GET, "/secure/extreme/**").hasRole("SUPERVISOR")
                .requestMatchers(HttpMethod.GET, "/secure/**").hasRole("USER")
                .and()
                .formLogin()
                .loginProcessingUrl("/login")
                .loginPage("/login.zul")
                .failureUrl("/login.zul?login_error=1")
                .successHandler(redirectLoginSuccessHandler())
                .and()
                .logout()
                .logoutSuccessUrl("/index.zul")
                .invalidateHttpSession(true);
        return http.build();
    }

    /**
     * allows login urls with a specific redirect parameter after successful login
     * e.g. /login.zul?redirect-after-login=/listAccounts.html
     *
     * @return
     */
    private AuthenticationSuccessHandler redirectLoginSuccessHandler() {
        final SavedRequestAwareAuthenticationSuccessHandler redirectLoginHandler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        redirectLoginHandler.setTargetUrlParameter("redirect-after-login");
        return redirectLoginHandler;
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        //for demo purposes only
        UserDetails user1 = User.withDefaultPasswordEncoder().username("rod").password("koala").roles("SUPERVISOR", "USER", "TELLER").build();
        UserDetails user2 = User.withDefaultPasswordEncoder().username("dianne").password("emu").roles("USER", "TELLER").build();
        UserDetails user3 = User.withDefaultPasswordEncoder().username("scott").password("wombat").roles("USER").build();
        UserDetails user4 = User.withDefaultPasswordEncoder().username("peter").password("opal").roles("USER").build();
        return new InMemoryUserDetailsManager(Arrays.asList(user1, user2, user3, user4));
    }

    @Bean
    public InMemoryAclService aclService() {
        return new InMemoryAclService();
    }
}
