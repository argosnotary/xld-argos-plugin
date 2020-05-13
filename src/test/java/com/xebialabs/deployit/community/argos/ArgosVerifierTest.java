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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabobank.argos.argos4j.Argos4jError;
import com.xebialabs.deployit.community.argos.model.ServiceAccount;
import com.xebialabs.deployit.community.argos.model.XldClientConfig;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.services.Repository;
import com.xebialabs.deployit.plugin.api.udm.Application;
import com.xebialabs.deployit.plugin.api.udm.Version;

import com.xebialabs.deployit.engine.api.MetadataService;

@ExtendWith(MockitoExtension.class)
class ArgosVerifierTest {
    
    private String APPLICATION_NAME = "Applications/aaa/argos-tes-app";
    private String SUPPLYCHAIN = "root_label.child_label:argos-test-app";
    private String SA_PASSPHRASE = "bar";
    private String XLD_CLIENT_CONFIG_ID = "Configuration/config/administration/argos/xldconfig";
    private String XLD_USERNAME = "xldUsername";
    private String XLD_PASSWORD = "xldPassword";
    
    @Mock
    Version version;
    
    @Mock
    Application application;
    
    @Mock
    ServiceAccount sa;
    
    @Mock
    XldClientConfig xldClientConfig;
    
    @Mock
    ExecutionContext context;
    
    @Mock
    Repository repository;
    
    MetadataService metadataService;

}
