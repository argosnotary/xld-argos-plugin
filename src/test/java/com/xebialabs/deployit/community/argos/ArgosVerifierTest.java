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
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
import com.xebialabs.deployit.plugin.api.udm.Deployable;
import com.xebialabs.deployit.plugin.api.udm.DeployableArtifact;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.xebialabs.deployit.plugin.api.udm.artifact.EmbeddedDeployableArtifact;
import com.xebialabs.deployit.plugin.api.udm.artifact.SourceArtifact;
import com.xebialabs.deployit.plugin.api.udm.base.BaseConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.base.BaseDeployable;
import com.xebialabs.deployit.plugin.api.udm.base.BaseDeployableArtifact;
import com.xebialabs.deployit.plugin.credentials.Credentials;
import com.xebialabs.deployit.plugin.credentials.UsernamePasswordCredentials;
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
    private String VERSION_ID2 = "versionId2";
    private String VERSION_ID3 = "versionId3";
    
    @Mock
    Version version;
    
    @Mock
    BaseDeployableArtifact deployable1;
    
    @Mock
    BaseDeployableArtifact deployable2;
    
    @Mock
    BaseDeployableArtifact deployable3;
    
    @Mock
    BaseDeployable deployable4;
    
    Set<Deployable> deployables;
    
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
    
    private WireMockServer httpWireMockServer;
    
    VerificationResult trueResult;
    
    VerificationResult falseResult;
    
    @Mock
    UsernamePasswordCredentials credentials;
    
    @BeforeEach
    void setUp() {
        xldWireMockServer = new WireMockServer(4516);
        xldWireMockServer.start();
        xldWireMockServer.stubFor(get(urlEqualTo("/deployit/export/deploymentpackage/versionId"))
                .willReturn(ok().withBody("theDownloadKey")));
        xldWireMockServer.stubFor(get(urlEqualTo("/deployit/export/deploymentpackage/versionId2"))
                .willReturn(ok().withBody("theDownloadKey2")));
        xldWireMockServer.stubFor(get(urlEqualTo("/deployit/export/deploymentpackage/versionId3"))
                .willReturn(ok().withBody("theDownloadKey3")));
        xldWireMockServer.stubFor(get(urlEqualTo("/deployit/internal/download/theDownloadKey")).willReturn(ok().withBodyFile("argos-test-app-1.0.dar")));
        xldWireMockServer.stubFor(get(urlEqualTo("/deployit/internal/download/theDownloadKey2")).willReturn(ok().withBodyFile("argos-test-app-2.0.dar")));
        xldWireMockServer.stubFor(get(urlEqualTo("/deployit/internal/download/theDownloadKey3")).willReturn(ok().withBodyFile("argos-test-app-1.0.war")));
        
        argosWireMockServer = new WireMockServer(8080);
        argosWireMockServer.start();
        argosWireMockServer.stubFor(get(urlEqualTo("/api/supplychain/verification?artifactHashes=82793bcbd52794aa51a12a8871e5a96a73066f0ae085be3df73dd832d2d2c18f&artifactHashes=a92de6e4706e50e22762b6d0fa125754de8474b4500d80c9f06f3cf546b86460&artifactHashes=f32c596e52896f8004c1fad2c4995cbc511667fc07f3dad1e9cb69924d2fb764&paths=root-label&paths=child-label"))
                .willReturn(ok().withBody("{\"runIsValid\":true}")));
        argosWireMockServer.stubFor(get(urlEqualTo("/api/supplychain/verification?artifactHashes=82793bcbd52794aa51a12a8871e5a96a73066f0ae085be3df73dd832d2d2c18f&artifactHashes=82c614900da1cf893d391215d9cd1a53a041975e329cce2cafed69a859a90cb8&artifactHashes=f2b182501d16376feb4a997a8dafc25cd07e7c235f1c2c041f064f84e2e4bbc0&paths=root-label&paths=child-label"))
                .willReturn(ok().withBody("{\"runIsValid\":false}")));
        
        argosWireMockServer.stubFor(get(urlEqualTo("/api/supplychain/verification?artifactHashes=82793bcbd52794aa51a12a8871e5a96a73066f0ae085be3df73dd832d2d2c18f&artifactHashes=82c614900da1cf893d391215d9cd1a53a041975e329cce2cafed69a859a90cb8&artifactHashes=f2b182501d16376feb4a997a8dafc25cd07e7c235f1c2c041f064f84e2e4bbc0&paths=root-label&paths=child-label"))
                .willReturn(ok().withBody("{\"runIsValid\":false}")));
        argosWireMockServer.stubFor(get(urlEqualTo(
                "/api/supplychain/verification?"
                + "artifactHashes=dcec6d14603774f49c21d3134b3fa1dae3fd4cb2cfb701e7bd608bac771fbf53"
                + "&artifactHashes=27a6b89fb7d125fee36cfc329a4e832ee8f12b2a6f7bcd7ccd7e389e0f4cbcbe"
                + "&artifactHashes=942446f6321c206fcff42ba77b1ae6d964139def4221cceb1e44b62f4708a22e"
                + "&artifactHashes=00277cb119cffb59125253a2d1fee6eb9916aebf7a9465d0d64bbca0a954a25d"
                + "&artifactHashes=63cb060b95edfb48470b7ca72f85ee7e99d1560a6290b8ab5f2415eb2b229291"
                + "&artifactHashes=2091eb9f2f05f09143e5b78726848884914f221f79deeaccd75fcc32b8b38345"
                + "&artifactHashes=1b14a945d788280ecd7c2fcb81397275071d8252bdd5c48ffa7e94f314a6bee9"
                + "&artifactHashes=4d729a644415ae2bdc27cb85aae8c3e6ff55468469d93bb97c00bb9cc3122b6c"
                + "&artifactHashes=caba1936c461b65ae3e36a84f5c0b4b73274fc88af3dda29e64eada39bd86d79"
                + "&artifactHashes=33077d4f469a10c65b57a3592b9c1212bcb6c2b3fcf7a795f827dab704af1a5f"
                + "&artifactHashes=117dd390c39ebf94916a2cca531d15d482e4abd1b427fa45a6144a4caddf53e8"
                + "&artifactHashes=43f3d7767761b6b44b60811fd7b23a86b9f262d7cf53136b4e3dd2f6002fb505"
                + "&artifactHashes=3cb05c7696d264e986eb11906f4e50a50eddbee024f302cec3664a2a9a34283c"
                + "&artifactHashes=bef68ca96838233cb8b6067c7af23d561f2ef42a6d7e4abb5e002cadeb51fc70"
                + "&artifactHashes=7a0177acd7b45c875255e19fbb99a45b8c11482acefae68411e55d42189c43df"
                + "&artifactHashes=66df7122e9f2750c4a02d7fc4b1a0d44b634118ca642669193f057e5eda3aab4"
                + "&artifactHashes=1936a0b0fbb7241af121cac8262ef8c87e3772ae6caa8cf0a2316eede7040062"
                + "&artifactHashes=a14fa3ac3a735b270916f9e59bd06f3443106a83cb157e3f57586b74b58d09fb"
                + "&paths=root-label&paths=child-label"))
                .willReturn(badRequest()));
        
        argosWireMockServer.stubFor(get(urlEqualTo("/api/supplychain/verification?"
                + "artifactHashes=82793bcbd52794aa51a12a8871e5a96a73066f0ae085be3df73dd832d2d2c18f"
                + "&artifactHashes=a92de6e4706e50e22762b6d0fa125754de8474b4500d80c9f06f3cf546b86460"
                + "&artifactHashes=f32c596e52896f8004c1fad2c4995cbc511667fc07f3dad1e9cb69924d2fb764"
                + "&artifactHashes=a92de6e4706e50e22762b6d0fa125754de8474b4500d80c9f06f3cf546b86460"
                + "&artifactHashes=a92de6e4706e50e22762b6d0fa125754de8474b4500d80c9f06f3cf546b86460"
                + "&paths=root-label&paths=child-label"))
                .willReturn(ok().withBody("{\"runIsValid\":true}")));
        
        httpWireMockServer = new WireMockServer(9080);
        httpWireMockServer.start();
        httpWireMockServer.stubFor(get(urlEqualTo("/deployable3")).willReturn(ok().withHeader("Authorization", "Basic Zm9vOmJhcg==").withBodyFile("argos-test-app-1.0.war")));
        httpWireMockServer.stubFor(get(urlEqualTo("/deployable2")).willReturn(ok().withHeader("Authorization", "Basic Zm9vOmJhcg==").withBodyFile("argos-test-app-1.0.war")));
        
        
        trueResult = VerificationResult.builder().runIsValid(true).build();
        falseResult = VerificationResult.builder().runIsValid(false).build();
        
        deployables = new HashSet<>();
        deployables.add(deployable1);
        deployables.add(deployable2);
        deployables.add(deployable3);
        deployables.add(deployable4);
        
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
        assertThat(ArgosVerifier.versionIsValid(context, version), is(true));

        when(version.getId()).thenReturn(VERSION_ID2);
        assertThat(ArgosVerifier.versionIsValid(context, version), is(false));
        
        when(version.getId()).thenReturn(VERSION_ID3);
        assertThat(ArgosVerifier.versionIsValid(context, version), is(false));
        
        when(version.getId()).thenReturn(VERSION_ID);
        when(version.getDeployables()).thenReturn(deployables);
        when(deployable1.getFileUri()).thenReturn("internal:foo");
        when(deployable2.getFileUri()).thenReturn("http://localhost:9080/deployable2");
        when(deployable2.getCredentials()).thenReturn(null);
        when(deployable3.getFileUri()).thenReturn("http://localhost:9080/deployable3");
        when(deployable3.getCredentials()).thenReturn(credentials);
        when(credentials.getUsername()).thenReturn("foo");
        when(credentials.getPassword()).thenReturn("bar");
        
        assertThat(ArgosVerifier.versionIsValid(context, version), is(true));
        
    }

}
