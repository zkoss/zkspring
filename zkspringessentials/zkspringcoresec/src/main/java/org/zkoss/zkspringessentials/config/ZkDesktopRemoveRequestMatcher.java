package org.zkoss.zkspringessentials.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * This class is used to match the request to remove a ZK desktop.
 */
class ZkDesktopRemoveRequestMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {
        return "rmDesktop".equals(request.getParameter("cmd_0"))
                && "POST".equals(request.getMethod())
                && "/zkau".equals(request.getRequestURI());
    }
}
