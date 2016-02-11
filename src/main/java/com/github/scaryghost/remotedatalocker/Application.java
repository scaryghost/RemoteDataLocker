package com.github.scaryghost.remotedatalocker;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

public class Application {
    public static void main(String[] args) throws Exception {
        final Main camelMain= new Main();

        camelMain.enableHangupSupport();
        camelMain.addRouteBuilder(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jetty:http://0.0.0.0:8000").process((exchange) -> {
                    exchange.getOut().setBody("Remote Data Locker on port 8000");
                }).end();
            }
        });
        camelMain.run();
    }
}
