package org.zkoss.zkspringessentials.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * This class is used to match the request to remove a ZK desktop.
 * Since ZK 8.6.2 (ZK-4204), zk sends a remove-desktop request by navigator.sendBeacon(), so we have to check the request object.
 * In previous version, ZK sends a remove-desktop GET request with parameters appended in the request URL. So yu can match the request by checking the URL.
 */
class ZkDesktopRemoveRequestMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {
        return "rmDesktop".equals(request.getParameter("cmd_0"))
                && "POST".equals(request.getMethod())
                && "/zkau".equals(request.getRequestURI());
    }
}
