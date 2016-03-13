package com.github.scaryghost.remotedatalocker;

import fi.iki.elonen.NanoHTTPD;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;

/**
 * Created by etsai on 3/12/2016.
 */
public class Application extends NanoHTTPD {
    private static final String HEADER_ACTION= "action", ACTION_GET= "get", ACTION_SAVE= "save";

    public static void main(String[] args) throws ParseException, ClassNotFoundException, SQLException, IOException {
        final ApplicationSettings settings= new ApplicationSettings(args);

        Class.forName(settings.dbDriver());
        final Connection dbConn= DriverManager.getConnection(settings.dbUrl());
        initDataBase(dbConn);

        new Application(settings.hostname(), settings.port(), dbConn);

    }

    private static void initDataBase(final Connection conn) throws SQLException {
        conn.setAutoCommit(false);

        Statement stmt = conn.createStatement();
        stmt.setQueryTimeout(30);  // set timeout to 30 sec.
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS data (key text NOT NULL, value text NOT NULL, PRIMARY KEY (key));");

        conn.commit();
    }

    private final Connection dbConn;

    public Application(String hostname, int port, Connection dbConn) throws IOException {
        super(hostname, port);
        this.dbConn= dbConn;

        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            session.parseBody(null);
        } catch (IOException | ResponseException e) {
            StringWriter sw= new StringWriter();
            PrintWriter pw= new PrintWriter(sw);
            e.printStackTrace(pw);

            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, sw.toString());
        }
        String action= session.getParms().get(HEADER_ACTION);

        switch(action) {
            case ACTION_GET:
                try {
                    final PreparedStatement getData = dbConn.prepareStatement("select value from data where key=?");
                    getData.setString(1, session.getParms().get("key"));

                    ResultSet result = getData.executeQuery();
                    if (result.next()) {
                        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, result.getString("value"));
                    } else {
                        return newFixedLengthResponse(Response.Status.NO_CONTENT, NanoHTTPD.MIME_PLAINTEXT, "");
                    }
                } catch (SQLException e) {
                    StringWriter sw= new StringWriter();
                    PrintWriter pw= new PrintWriter(sw);
                    e.printStackTrace(pw);

                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, sw.toString());
                }
            case ACTION_SAVE:
                try {
                    final PreparedStatement insertOrIgnore = dbConn.prepareStatement("insert or ignore into data (key, value) values (?, ?)");
                    insertOrIgnore.setString(1, session.getParms().get("key"));
                    insertOrIgnore.setString(2, "0");
                    insertOrIgnore.executeUpdate();

                    final PreparedStatement updateData = dbConn.prepareStatement("update data set value=? where key=?");
                    updateData.setString(1, session.getParms().get("value"));
                    updateData.setString(2, session.getParms().get("key"));
                    updateData.executeUpdate();

                    dbConn.commit();
                    return newFixedLengthResponse(Response.Status.NO_CONTENT, NanoHTTPD.MIME_PLAINTEXT, "");
                } catch (SQLException e) {
                    StringWriter sw= new StringWriter();
                    PrintWriter pw= new PrintWriter(sw);
                    e.printStackTrace(pw);

                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, sw.toString());
                }
            default:
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Unrecognized action: " + action);
        }
    }
}
