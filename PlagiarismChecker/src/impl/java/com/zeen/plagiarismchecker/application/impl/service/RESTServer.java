package com.zeen.plagiarismchecker.application.impl.service;

import java.util.logging.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class RESTServer {

    private static final Logger LOGGER = Logger.getLogger(RESTServer.class
            .getName());

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
        
       // Tells the Jersey Servlet which REST service/class to load.
        
        jerseyServlet.setInitParameter(
                "jersey.config.server.provider.classnames",
                PlagiarismCheckeService.class.getCanonicalName());

        try {
            LOGGER.info("Starting server in port 8080");
            jettyServer.start();
            LOGGER.info("Started");
            started = true;
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }

}
