package com.xebialabs.deployit.community.argos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgosConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ArgosConfiguration.class);

    protected static final Properties argosProperties = getProperties();

    private ArgosConfiguration() {
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try {
            properties.load(ArgosConfiguration.class.getResourceAsStream("/default.properties"));
            InputStream input = ArgosConfiguration.class.getResourceAsStream("/argos.properties");
            if (input != null) {
                properties.load(ArgosConfiguration.class.getResourceAsStream("/argos.properties"));
            } else {
                logger.warn("argos.properties file not in config directory, defaults are used.");
            }
        } catch (IOException e) {
            logger.info("{}", e.getMessage());
        }        
        return properties;
    }

    public static ActionOnInvalid getActionOnInvalid() {
        return ActionOnInvalid.valueOf(argosProperties.getProperty("argos.action.on.invalid"));
    }

    public static ArgosVerification getArgosVerification() {
        return ArgosVerification.valueOf(argosProperties.getProperty("argos.verification"));
    }

}
