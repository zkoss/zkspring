package org.zkoss.zkspringessentials.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.http.WebManager;
import org.zkoss.zk.ui.sys.DesktopCache;
import org.zkoss.zk.ui.sys.WebAppCtrl;

/**
 * Permits ZK AU requests that originate from public /core/** pages.
 * Uses the ZK Desktop API to look up the page URL from the dtid request parameter,
 * since ZATS does not send Referer headers.
 */
class CorePageZkauMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {
        if (!"POST".equals(request.getMethod()) || !request.getRequestURI().endsWith("/zkau")) {
            return false;
        }
        String dtid = request.getParameter("dtid");
        if (dtid == null) {
            return false;
        }
        try {
            Session zkSession = WebManager.getSession(request.getServletContext(), request, false);
            if (zkSession == null) {
                return false;
            }
            DesktopCache cache = ((WebAppCtrl) zkSession.getWebApp()).getDesktopCache(zkSession);
            Desktop desktop = cache.getDesktopIfAny(dtid);
            if (desktop == null) {
                return false;
            }
            String requestPath = desktop.getRequestPath();
            return requestPath != null && requestPath.startsWith("/core/");
        } catch (Exception e) {
            return false;
        }
    }
}
