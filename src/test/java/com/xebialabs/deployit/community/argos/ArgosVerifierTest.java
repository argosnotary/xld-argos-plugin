package com.xebialabs.deployit.community.argos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabobank.argos.argos4j.Argos4jError;
import com.xebialabs.deployit.community.argos.model.NonPersonalAccount;
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
    private String NPA_PASSPHRASE = "bar";
    private String XLD_CLIENT_CONFIG_ID = "Configuration/config/administration/argos/xldconfig";
    private String XLD_USERNAME = "xldUsername";
    private String XLD_PASSWORD = "xldPassword";
    
    @Mock
    Version version;
    
    @Mock
    Application application;
    
    @Mock
    NonPersonalAccount npa;
    
    @Mock
    XldClientConfig xldClientConfig;
    
    @Mock
    ExecutionContext context;
    
    @Mock
    Repository repository;
    
    MetadataService metadataService;

    @Test
    void testNpaNotSet() {
        when(version.getApplication()).thenReturn(application);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN)).thenReturn(SUPPLYCHAIN);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_PERSONAL_ACCOUNT)).thenReturn(null);
        when(application.getName()).thenReturn(APPLICATION_NAME);     
        assertFalse(ArgosVerifier.versionIsValid(context, version));
    }
    
    @Test
    void testSupplyChainNotSet() {
        when(version.getApplication()).thenReturn(application);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN)).thenReturn(null);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_PERSONAL_ACCOUNT)).thenReturn(npa);
        when(application.getName()).thenReturn(APPLICATION_NAME);
        
        assertFalse(ArgosVerifier.versionIsValid(context, version));
    }
    
    @Test
    void testVerifyAsFarAsItGoes() {
        when(version.getApplication()).thenReturn(application);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN)).thenReturn(SUPPLYCHAIN);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_PERSONAL_ACCOUNT)).thenReturn(npa);
        when(npa.getPassphrase()).thenReturn(NPA_PASSPHRASE);
        when(context.getRepository()).thenReturn(repository);
        when(repository.read(XLD_CLIENT_CONFIG_ID)).thenReturn(xldClientConfig);
        when(xldClientConfig.getUsername()).thenReturn(XLD_USERNAME);
        when(xldClientConfig.getPassword()).thenReturn(XLD_PASSWORD);
        
        Throwable exception = assertThrows(Argos4jError.class, () -> {
            ArgosVerifier.versionIsValid(context, version);
          });
        assertEquals("Connection refused (Connection refused)", exception.getMessage());
    }
    
    @Test
    void testGetSupplyChainName() {        
        assertEquals("argos-test-app", ArgosVerifier.getSupplyChainName("root_label.child_label:argos-test-app"));
    }
    
    @Test
    void testGetPath() {        
        assertEquals(Arrays.asList("child_label", "root_label"), ArgosVerifier.getPath("root_label.child_label:argos-test-app"));
    }

}
