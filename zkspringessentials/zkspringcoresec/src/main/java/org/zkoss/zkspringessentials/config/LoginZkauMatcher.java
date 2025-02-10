package org.zkoss.zkspringessentials.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * This class is used to match the ZK AU requests from a login page.
 * If you plan to allow ajax on the login page, you need to apply this matcher.
 */
class LoginZkauMatcher implements RequestMatcher {

    public static final String LOGIN_ZUL = "login.zul";

    @Override
    public boolean matches(HttpServletRequest request) {
        String referer = request.getHeader("referer");
        String requestURI = request.getRequestURI();
        return referer != null
                && referer.endsWith(LOGIN_ZUL)
                && requestURI.endsWith("/zkau") // Verify it's actually a ZK AU request
                && request.getMethod().equals("POST"); // ZK AU requests should be POST
    }
}
