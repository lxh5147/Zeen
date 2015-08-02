package com.zeen.plagiarismchecker.application.impl.service;

import java.util.logging.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.common.base.Strings;

public class RESTServer {

    private static final Logger LOGGER = Logger.getLogger(RESTServer.class
            .getName());

    static boolean started = false;
    static final int DEFAULT_PORT_NUMBER = 8080;

    public static final String PORT_NUMBER_ENV_VARIABLE_NAME = "PlagiarismCheckerServicePortNumber";

    static int getPortNumberSetting() {
        String property = System.getProperty(PORT_NUMBER_ENV_VARIABLE_NAME);
        if (Strings.isNullOrEmpty(property)) {
            property = System.getenv(PORT_NUMBER_ENV_VARIABLE_NAME);
        }
        if (Strings.isNullOrEmpty(property)) {
            return DEFAULT_PORT_NUMBER;
        }
        return Integer.valueOf(property);
    }

    public static void main(String[] args) throws Exception {
        PlagiarismCheckerService.setupContext(args);

        ServletContextHandler context = new ServletContextHandler(
                ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(getPortNumberSetting());
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.

        jerseyServlet.setInitParameter(
                "jersey.config.server.provider.classnames",
                PlagiarismCheckerService.class.getCanonicalName());

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
