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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.community.argos.model.ActionOnInvalid;
import com.xebialabs.deployit.community.argos.model.ArgosVerificationStatus;
import com.xebialabs.deployit.community.argos.model.XldClientConfig;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;

public class ArgosConfiguration {
    
    public static final List<Operation> OPERATIONS_WITHOUT_VERIFICATION = Arrays.asList(Operation.NOOP, Operation.DESTROY);
    public static final String PROPERTY_ARGOS_PERSONAL_ACCOUNT = "argosNonPersonalAccount";
    public static final String PROPERTY_VERIFY_WITH_ARGOS = "verifyWithArgos";
    public static final String PROPERTY_ARGOS_SUPPLYCHAIN = "argosSupplyChain";
    
    private static final String EXPORT_URI = "%s/deployit/export/%s";

    private static final Logger logger = LoggerFactory.getLogger(ArgosConfiguration.class);

    protected static final Properties argosProperties = getProperties();
    
    protected static String xldUriTemplate;

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

    public static ArgosVerificationStatus getArgosVerification() {
        return ArgosVerificationStatus.valueOf(argosProperties.getProperty("argos.verification"));
    }
    
    public static String getArgosServerBaseUrl() {
        return argosProperties.getProperty("argos.service.base.url"); 
    }
    
    public static URI getXldUriForExport(ExecutionContext context, String fragment) {
        XldClientConfig xldConf = context.getRepository().read(argosProperties.getProperty("argos.xld.client.conf.id"));
        String xldBaseUrl = argosProperties.getProperty("argos.xld.base.url");
        URI xldUri = null;
        try {
            xldUri = new URI(String.format(EXPORT_URI, xldBaseUrl, fragment));
            xldUri = new URI(
                    xldUri.getScheme(), 
                    xldConf.getUsername() + ":" + xldConf.getPassword(), 
                    xldUri.getHost(),
                    xldUri.getPort(), 
                    xldUri.getPath(), 
                    xldUri.getQuery(), 
                    xldUri.getFragment());
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return xldUri;
    }

}
