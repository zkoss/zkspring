package org.zkoss.zkspringessentials.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.util.matcher.*;
import org.zkoss.zkspringessentials.app.acl.InMemoryAclService;

import java.util.Arrays;

/**
 * see https://www.spring-doc.cn/spring-security/6.1.9/servlet_authorization_authorize-http-requests.en.html
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) //doesn't work with ZK, because ZK uses its own CSRF protection
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            ).authorizeHttpRequests(authorize -> authorize
                .requestMatchers(new AntPathRequestMatcher("/zkres/**")).permitAll() //permit ZK getting resources from DHtmlResourceServlet
                .requestMatchers(new ZkDesktopRemoveRequestMatcher()).permitAll() //permit ZK desktop removal request
                //permit application specific public pages
                .requestMatchers(new AntPathRequestMatcher("/login.zul"),
                        new AntPathRequestMatcher("/index.zul"),
                        new AntPathRequestMatcher("/")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/**/*.html")).permitAll()
                //secure specific paths with specific roles
                .requestMatchers(new AntPathRequestMatcher("/secure/extreme/**")).hasRole("SUPERVISOR")
                .requestMatchers(new AntPathRequestMatcher( "/secure/**")).hasRole("USER")
                .requestMatchers(new AntPathRequestMatcher( "/zkau/**")).hasRole("USER")
                //all other unspecified paths are secured, even if they are not explicitly listed, when using authorizeHttpRequests()
            )
            .formLogin(form -> form
                .loginProcessingUrl("/login")
                .loginPage("/login.zul")
                .failureUrl("/login.zul?login_error=1")
                .defaultSuccessUrl("/index.zul", true) //specify true to always redirect to index.zul after login, avoid redirect to the last requested URL, because it might be a zkau
                .successHandler(redirectLoginSuccessHandler())
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/index.zul")
                .invalidateHttpSession(true)
            )
            /* when an end-user sends an AJAX without authentication, turn 302 to 403, and redirect to login page in zk.xml
            * because zk client engine cannot handle the HTML result of 302, redirect to login.zul page */
            .exceptionHandling(exception -> exception
                .defaultAuthenticationEntryPointFor(
                        new Http403ForbiddenEntryPoint(),
                        new AntPathRequestMatcher("/zkau", "POST"))
            );
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
        redirectLoginHandler.setRequestCache(new NullRequestCache()); //avoid saving /zkau, so avoid being redirected to /zkau after login
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
