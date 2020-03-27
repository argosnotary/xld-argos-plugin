/**
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xebialabs.deployit.community.argos;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabobank.argos.argos4j.Argos4jError;
import com.xebialabs.deployit.community.argos.model.ActionOnInvalid;
import com.xebialabs.deployit.community.argos.model.ArgosVerificationStatus;
import com.xebialabs.deployit.community.argos.model.XldClientConfig;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;

public class ArgosConfiguration {
    
    public static final List<Operation> OPERATIONS_WITHOUT_VERIFICATION = 
            Arrays.asList(Operation.NOOP, Operation.DESTROY);
    
    public static final String PROPERTY_ARGOS_PERSONAL_ACCOUNT = "argosNonPersonalAccount";
    public static final String PROPERTY_VERIFY_WITH_ARGOS = "verifyWithArgos";
    public static final String PROPERTY_ARGOS_SUPPLYCHAIN = "argosSupplyChain";
    public static final String PROPERTY_ACTION_ON_INVALID = "argos.action.on.invalid";
    public static final String PROPERTY_VERIFICATION_STATUS = "argos.verification.status";
    public static final String PROPERTY_ARGOS_SERVICE_BASE_URL = "argos.service.base.url";
    public static final String PROPERTY_XLD_BASE_URL = "xld.base.url";
    public static final String PROPERTY_XLD_CLIENT_CONF_ID = "argos.xld.client.conf.id";
    public static final String PROPERTY_ARGOS_ABORT_TEMPLATE = "argos.abort.template";
    public static final String PROPERTY_ARGOS_WARN_TEMPLATE = "argos.warn.template";
    public static final String PROPERTY_ARGOS_VALID_TEMPLATE = "argos.valid.template";
    public static final String PROPERTY_ARGOS_RESULT_PREFIX = "argos.result.prefix";
    
    private static final String KEY_URI = "%s/export/deploymentpackage/%s";    
    private static final String EXPORT_URI = "%s/internal/download/%s";

    private static final Logger logger = LoggerFactory.getLogger(ArgosConfiguration.class);

    protected static final Properties argosProperties = getProperties();
    
    protected static String xldUriTemplate;

    private ArgosConfiguration() {
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try {
            logger.warn("loading default properties.");
            properties.load(ArgosConfiguration.class.getResourceAsStream("/default.properties"));
        } catch (IOException e) {
            logger.info("{}", e.getMessage());
        }
        
        try (InputStream input = ArgosConfiguration.class.getClassLoader().getResourceAsStream("argos.properties")) {
            if (input == null) {
                logger.warn("argos.properties file not in config directory, defaults are used.");
                return properties;
            }
            
          //load a properties file from class path, inside static method
            properties.load(input);
        } catch (IOException e) {
            logger.info("{}", e.getMessage());
        }
        return properties;
    }

    public static ActionOnInvalid getActionOnInvalid() {
        return ActionOnInvalid.valueOf(argosProperties.getProperty(PROPERTY_ACTION_ON_INVALID));
    }

    public static ArgosVerificationStatus getArgosVerificationStatus() {
        return ArgosVerificationStatus.valueOf(argosProperties.getProperty(PROPERTY_VERIFICATION_STATUS));
    }
    
    public static String getArgosServerBaseUrl() {
        return argosProperties.getProperty(PROPERTY_ARGOS_SERVICE_BASE_URL); 
    }
    
    public static String getXldUrlForDownloadKey(String fragment) {
        return String.format(KEY_URI, argosProperties.getProperty(PROPERTY_XLD_BASE_URL), fragment);
    }
    
    public static URL getXldUrlForExport(String key) {
        String url = String.format(EXPORT_URI, argosProperties.getProperty(PROPERTY_XLD_BASE_URL), key);
        try {
            return new URL(url);
        } catch (MalformedURLException e) {        
            throw new Argos4jError(url+": " + e.getMessage());
        }
    }
    
    public static XldClientConfig getXldClientConfig(ExecutionContext context) {
        return context.getRepository().read(argosProperties.getProperty(PROPERTY_XLD_CLIENT_CONF_ID));
    }
    
    public static String getArgosActionTemplate(ActionOnInvalid action) {
        switch(action) {
        case ABORT: 
            return argosProperties.getProperty(PROPERTY_ARGOS_RESULT_PREFIX)+argosProperties.getProperty(PROPERTY_ARGOS_ABORT_TEMPLATE);
        case WARN:
            return argosProperties.getProperty(PROPERTY_ARGOS_RESULT_PREFIX)+argosProperties.getProperty(PROPERTY_ARGOS_WARN_TEMPLATE);
        default:
            return argosProperties.getProperty(PROPERTY_ARGOS_RESULT_PREFIX);
        }
    }
    
    public static String getArgosValidTemplate() {
        return argosProperties.getProperty(PROPERTY_ARGOS_RESULT_PREFIX)+argosProperties.getProperty(PROPERTY_ARGOS_VALID_TEMPLATE);
    }

}
