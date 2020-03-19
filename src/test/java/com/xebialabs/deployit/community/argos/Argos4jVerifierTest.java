package com.xebialabs.deployit.community.argos;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;

import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.VerificationResult;
import com.rabobank.argos.argos4j.VerifyBuilder;
import com.rabobank.argos.argos4j.internal.VerifyBuilderImpl;
import com.xebialabs.deployit.community.argos.model.NonPersonalAccount;
import com.xebialabs.deployit.community.argos.model.XldClientConfig;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.services.Repository;
import com.xebialabs.deployit.plugin.api.udm.Application;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.xebialabs.deployit.plugin.api.udm.base.BaseConfigurationItem;

import com.xebialabs.deployit.engine.api.MetadataService;

@ExtendWith(MockitoExtension.class)
class Argos4jVerifierTest {
    
    private String APPLICATION_NAME = "Applications/aaa/argos-tes-app";
    private String VERSION_NAME = "1.0";
    private String VERSION_ID = APPLICATION_NAME+"/"+VERSION_NAME;
    private String SUPPLYCHAIN_KEY = "argosSupplyChain";
    private String SUPPLYCHAIN = "root_label.child_label:argos-test-app";
    private String NPA_KEY = "argosNonPersonalAccount";
    private String NPA_ID = "BLA";
    private String NPA_KEY_ID = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
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
        assertFalse(Argos4jVerifier.versionIsValid(context, version));
    }
    
    @Test
    void testSupplyChainNotSet() {
        when(version.getApplication()).thenReturn(application);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN)).thenReturn(null);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_PERSONAL_ACCOUNT)).thenReturn(npa);
        when(application.getName()).thenReturn(APPLICATION_NAME);
        
        assertFalse(Argos4jVerifier.versionIsValid(context, version));
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
            Argos4jVerifier.versionIsValid(context, version);
          });
        assertEquals("Connection refused (Connection refused)", exception.getMessage());
    }
    
    @Test
    void testGetSupplyChainName() {        
        assertEquals("argos-test-app", Argos4jVerifier.getSupplyChainName("root_label.child_label:argos-test-app"));
    }
    
    @Test
    void testGetPath() {        
        assertEquals(Arrays.asList("child_label", "root_label"), Argos4jVerifier.getPath("root_label.child_label:argos-test-app"));
    }

}
