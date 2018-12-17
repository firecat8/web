/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ers.v1.connector;

import com.ers.re.service.pub.api.v1_1.messaging.EndpointRegistry;
import com.ers.re.service.pub.api.v1_1.messaging.impl.EndpointRegistryImpl;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author gdimitrova
 */
public class REConnector {

    private ConnectionFactory cf = null;
    private EndpointRegistry registry = null;
    private Properties config = null;
    private final String clientID = "INEA_PUBLIC";

    public final static REConnector INSTANCE = new REConnector();

    private REConnector() {
        start();
    }

    public EndpointRegistry getRegistry() {
        return registry;
    }

    private void start() {
        if (cf == null) {
            Properties props = loadProperties();
            String host = props.getProperty("host");
            String port = props.getProperty("port");
            cf = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);
            registry = new EndpointRegistryImpl(cf, clientID, props.getProperty("prefix"), false);
            registry.start();
        }
    }

    private Properties loadProperties() {
        if (config == null) {
            try {
                config = new Properties();
                InputStreamReader in = new InputStreamReader(getClass().getResourceAsStream("/config.properties"));
                config.load(in);
            } catch (IOException ex) {
                Logger.getLogger(REConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return config;
    }

}
