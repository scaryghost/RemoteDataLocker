package com.github.scaryghost.remotedatalocker;

import org.apache.commons.cli.*;

/**
 * Created by etsai on 3/6/2016.
 */
public class ApplicationSettings {
    private final static String DEFAULT_PORT= "8000", DEFAULT_HOSTNAME= "0.0.0.0",
            DEFAULT_DB_URL= "jdbc:sqlite:datalocker.sqlite", DEFAULT_DB_DRIVER= "org.sqlite.JDBC";

    private final Short portSetting;
    private final String hostnameSetting, dbDriverSetting, dbUrlSetting;

    public ApplicationSettings(String[] args) throws ParseException {
        Options options= new Options();
        options.addOption("p", "port", true, "TCP port listening for connections");
        options.addOption("n", "hostname", true, "Hostname of the Jetty route");
        options.addOption(Option.builder().longOpt("db-url").argName("url").hasArg(true).desc("JDBC database url").required(false).build());
        options.addOption(Option.builder().longOpt("db-driver").argName("classname").hasArg(true).desc("JDBC driver classname").required(false).build());

        CommandLine cmdLine = new DefaultParser().parse( options, args);

        portSetting= Short.parseShort(cmdLine.getOptionValue("p", DEFAULT_PORT));
        hostnameSetting= cmdLine.getOptionValue("hostname", DEFAULT_HOSTNAME);
        dbUrlSetting= cmdLine.getOptionValue("db-url", DEFAULT_DB_URL);
        dbDriverSetting= cmdLine.getOptionValue("db-driver", DEFAULT_DB_DRIVER);
    }

    public short port() {
        return portSetting;
    }

    public String hostname() {
        return hostnameSetting;
    }

    public String dbDriver() {
        return dbDriverSetting;
    }

    public String dbUrl() {
        return dbUrlSetting;
    }
}
