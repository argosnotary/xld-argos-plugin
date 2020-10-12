/**
 * Copyright (C) 2020 Argos Notary Coöperatie UA
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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import javax.print.attribute.SetOfIntegerSyntax;
import javax.swing.event.ListSelectionEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.LocalFileCollector;
import com.argosnotary.argos.argos4j.VerificationResult;
import com.argosnotary.argos.argos4j.VerifyBuilder;
import com.argosnotary.argos.argos4j.internal.ArtifactCollectorFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
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
    private String SUPPLYCHAIN = "root-label.child-label:argos-test-app";
    private String SA_KEY_ID = "keyId";
    private String SA_PASSPHRASE = "bar";
    private String XLD_CLIENT_CONFIG_ID = "Configuration/config/administration/argos/xldconfig";
    private String XLD_USERNAME = "xldUsername";
    private String XLD_PASSWORD = "xldPassword";
    private String VERSION_ID = "versionId";
    
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
    
    @Mock
    VerifyBuilder verifyBuilder;
    
    @Mock
    ArgosXldClientSettings settings;
    
    MetadataService metadataService;
    
    private WireMockServer xldWireMockServer;
    
    private WireMockServer argosWireMockServer;
    
    VerificationResult trueResult;
    
    @BeforeEach
    void setUp() {
        xldWireMockServer = new WireMockServer(4516);
        xldWireMockServer.start();
        xldWireMockServer.stubFor(get(urlEqualTo("/deployit/export/deploymentpackage/versionId"))
                .willReturn(ok().withBody("theDownloadKey")));
        xldWireMockServer.stubFor(post(urlEqualTo("/deployit/internal/download/theDownloadKey")).willReturn(ok().withBodyFile("src/test/resources/")));
        //xldWireMockServer.stubFor(get(urlEqualTo("/api/serviceaccount/me/activekey")).willReturn(ok().withBody(restKeyPairRest)));
        
        
        argosWireMockServer = new WireMockServer(8080);
        argosWireMockServer.start();
        
        trueResult = VerificationResult.builder().runIsValid(true).build();
        
        //application = new Application();
        //application.setProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN, SUPPLYCHAIN);
        
    }
    
    @Test
    void testVersionIsValid() {
        when(context.getRepository()).thenReturn(repository);
        when(repository.read(XLD_CLIENT_CONFIG_ID)).thenReturn(xldClientConfig);
        when(version.getId()).thenReturn(VERSION_ID);
        when(version.getApplication()).thenReturn(application);
        when(application.getProperty("argosSupplyChain")).thenReturn(SUPPLYCHAIN);
        ArgosError error = assertThrows(ArgosError.class, () -> 
                  ArgosVerifier.versionIsValid(context, version));
        assertThat(error.getMessage(), is("Argos SA not set on Application"));
        when(application.getProperty("argosServiceAccount")).thenReturn(sa);
        when(sa.getPassphrase()).thenReturn(SA_PASSPHRASE);
        when(sa.getKeyId()).thenReturn(SA_KEY_ID);
        when(xldClientConfig.getUsername()).thenReturn(XLD_USERNAME);
        when(xldClientConfig.getPassword()).thenReturn(XLD_PASSWORD);
        when(version.getDeployables()).thenReturn(new HashSet<>());
        //ArgosXldClientSettings settings = ArgosXldClientSettings.builder().context(context).version(version).build();
        //when(ArgosXldClientSettings.builder().context(context).version(version).build()).thenReturn(settings);
        //when(settings.getArgos4j().getSettings().getPath()).thenReturn(Arrays.asList("root-label","child-label"));
        //when(settings.getArgos4j().getVerifyBuilder(Arrays.asList("root-label","child-label"))).thenReturn(verifyBuilder);
        //when(verifyBuilder.verify()).thenReturn(trueResult);
        assertThat(ArgosVerifier.versionIsValid(context, version), is(true));
        
    }

}
