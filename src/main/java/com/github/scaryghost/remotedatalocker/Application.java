package com.github.scaryghost.remotedatalocker;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;

import java.sql.*;

public class Application {
    private static final String HEADER_ACTION= "action", ACTION_GET= "get", ACTION_SAVE= "save";

    public static void main(String[] args) throws Exception {
        final Connection dbConn= initDataBase();
        final Main camelMain= new Main();

        camelMain.enableHangupSupport();
        camelMain.addMainListener(new MainListenerSupport() {
            @Override
            public void beforeStop(MainSupport main) {
                try {
                    dbConn.close();
                } catch (SQLException ignored) {
                }
            }
        });
        camelMain.addRouteBuilder(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jetty:http://0.0.0.0:8000")
                    .choice()
                        .when(header(HEADER_ACTION).isEqualTo(ACTION_GET))
                        .doTry()
                        .process((exchange) -> {
                            Message msg = exchange.getIn();

                            PreparedStatement getState = dbConn.prepareStatement("select value from data where key=?");
                            getState.setString(1, msg.getHeader("key", String.class));

                            ResultSet result = getState.executeQuery();
                            if (result.next()) {
                                exchange.getOut().setBody(result.getString("value"));
                            } else {
                                exchange.getOut().setBody("");
                                exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 204);
                            }
                        })
                        .doCatch(SQLException.class).setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500)).endChoice()
                        .when(header(HEADER_ACTION).isEqualTo(ACTION_SAVE))
                        .doTry()
                        .process((exchange) -> {
                            Message msg = exchange.getIn();

                            PreparedStatement insertOrIgnore = dbConn.prepareStatement("insert or ignore into data (key, value) values (?, ?)");
                            insertOrIgnore.setString(1, msg.getHeader("key", String.class));
                            insertOrIgnore.setString(2, "0");
                            insertOrIgnore.executeUpdate();

                            PreparedStatement updateData = dbConn.prepareStatement("update data set value=? where key=?");
                            updateData.setString(1, msg.getHeader("value", String.class));
                            updateData.setString(2, msg.getHeader("key", String.class));
                            updateData.executeUpdate();

                            dbConn.commit();
                        })
                        .doCatch(SQLException.class).setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500)).endChoice()
                        .otherwise().to("mock:other")
                        .end();
            }
        });
        camelMain.run();
    }

    private static Connection initDataBase() throws SQLException {
        Connection conn= DriverManager.getConnection("jdbc:sqlite:datalocker.sqlite");
        conn.setAutoCommit(false);

        Statement stmt = conn.createStatement();
        stmt.setQueryTimeout(30);  // set timeout to 30 sec.
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS data (key text NOT NULL, value text NOT NULL, PRIMARY KEY (key));");

        conn.commit();

        return conn;
    }
}
