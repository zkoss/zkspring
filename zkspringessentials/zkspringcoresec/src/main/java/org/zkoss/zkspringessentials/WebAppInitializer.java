package org.zkoss.zkspringessentials;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.zkoss.zk.ui.http.HttpSessionListener;
import org.zkoss.zkspringessentials.config.*;

import javax.servlet.*;


public class WebAppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(final ServletContext servletContext) {
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.setServletContext(servletContext);
        servletContext.addListener(new ContextLoaderListener(rootContext));
        rootContext.register(ApplicationConfig.class);

        registerApiContext(servletContext, rootContext);

        registerGuiContext(servletContext, rootContext);

        servletContext.addListener(new HttpSessionListener());
        //Manage the lifecycle of the root application context
    }

    private static void registerGuiContext(ServletContext servletContext, AnnotationConfigWebApplicationContext rootContext) {
        // GUI Context for ZKoss
        AnnotationConfigWebApplicationContext guiContext = new AnnotationConfigWebApplicationContext();
        guiContext.setParent(rootContext);
        guiContext.register(GuiConfig.class);
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("org.zkoss.zkspringessentials.bigbank", new DispatcherServlet(guiContext));
        dispatcher.setLoadOnStartup(1); // Equivalent to <load-on-startup>1</load-on-startup>
        dispatcher.addMapping("*.html");
    }

    private static void registerApiContext(ServletContext servletContext, AnnotationConfigWebApplicationContext rootContext) {
        // REST API Context
        AnnotationConfigWebApplicationContext apiContext = new AnnotationConfigWebApplicationContext();
        apiContext.setParent(rootContext);
        apiContext.register(RestApiConfiguration.class);
        ServletRegistration.Dynamic apiServlet = servletContext.addServlet("apiDispatcher", new DispatcherServlet(apiContext));
        apiServlet.addMapping("/api/");
        apiServlet.setLoadOnStartup(1);
    }
}
