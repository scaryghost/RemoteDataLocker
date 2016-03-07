package com.github.scaryghost.remotedatalocker;

import org.apache.commons.cli.*;

/**
 * Created by etsai on 3/6/2016.
 */
public class ApplicationSettings {
    private final static Short DEFAULT_PORT= 8000;
    private final static String DEFAULT_HOSTNAME= "0.0.0.0", DEFAULT_DB_URL= "jdbc:sqlite:datalocker.sqlite", DEFAULT_DB_DRIVER= "org.sqlite.JDBC";

    private final Short portSetting;
    private final String hostnameSetting, dbDriverSetting, dbUrlSetting;

    public ApplicationSettings(String[] args) throws ParseException {
        Options options= new Options();
        options.addOption("p", "port", true, "TCP port listening for connections");
        options.addOption("n", "hostname", true, "Hostname of the Jetty route");
        options.addOption(Option.builder().longOpt("db-url").argName("url").hasArg(true).desc("JDBC database url").required(false).build());
        options.addOption(Option.builder().longOpt("db-driver").argName("classname").hasArg(true).desc("JDBC driver classname").required(false).build());

        CommandLine cmdLine = new DefaultParser().parse( options, args);

        portSetting= Short.parseShort(cmdLine.getOptionValue("p"));
        hostnameSetting= cmdLine.getOptionValue("hostname");
        dbUrlSetting= cmdLine.getOptionValue("db-url");
        dbDriverSetting= cmdLine.getOptionValue("db-driver");
    }

    public short port() {
        return (portSetting == null) ? DEFAULT_PORT : portSetting;
    }

    public String hostname() {
        return (hostnameSetting == null) ? DEFAULT_HOSTNAME : hostnameSetting;
    }

    public String dbDriver() {
        return (dbDriverSetting == null) ? DEFAULT_DB_DRIVER : dbDriverSetting;
    }

    public String dbUrl() {
        return (dbUrlSetting == null) ? DEFAULT_DB_URL : dbUrlSetting;
    }
}
