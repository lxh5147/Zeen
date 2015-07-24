package com.zeen.plagiarismchecker.application.impl.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class RESTServer {

    static boolean started = false;

    public static void main(String[] args) throws Exception {
        PlagiarismCheckeService.setupContext(args);

        ServletContextHandler context = new ServletContextHandler(
                ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(8080);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        
        jerseyServlet.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        
        jerseyServlet.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        // Tells the Jersey Servlet which REST service/class to load.
        
        jerseyServlet.setInitParameter(
                "jersey.config.server.provider.classnames",
                PlagiarismCheckeService.class.getCanonicalName());

        try {
            jettyServer.start();
            started = true;
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }

}
