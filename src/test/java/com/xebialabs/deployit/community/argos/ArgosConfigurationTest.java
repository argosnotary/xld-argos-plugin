package com.xebialabs.deployit.community.argos;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xebialabs.deployit.community.argos.model.ActionOnInvalid;
import com.xebialabs.deployit.community.argos.model.ArgosVerificationStatus;
import com.xebialabs.deployit.community.argos.model.XldClientConfig;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.services.Repository;

@ExtendWith(MockitoExtension.class)
class ArgosConfigurationTest {
    
    private String APPLICATION_NAME = "Applications/aaa/argos-tes-app";
    private String VERSION_NAME = "1.0";
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
    XldClientConfig xldConf;
    
    @Mock
    Repository repository;
    
    @Mock
    ExecutionContext context;

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void testGetActionOnInvalid() {
        assertThat(ArgosConfiguration.getActionOnInvalid(), is(ActionOnInvalid.NONE));
    }

    @Test
    void testGetArgosVerificationStatus() {
        assertThat(ArgosConfiguration.getArgosVerificationStatus(), is(ArgosVerificationStatus.DISABLED));
    }
    
    @Test
    void testGetArgosServerBaseUrl() {
        assertThat(ArgosConfiguration.getArgosServerBaseUrl(), is("http://localhost:8080/api"));
    }
    
    @Test
    void testGetXldUriForExport() throws URISyntaxException {
        when(context.getRepository()).thenReturn(repository);
        when(repository.read(XLD_CLIENT_CONFIG_ID)).thenReturn(xldConf);

        when(xldConf.getUsername()).thenReturn("admin");
        when(xldConf.getPassword()).thenReturn("admin123"); 
        
        assertThat(ArgosConfiguration.getXldUriForExport(context, "foo"), is(new URI("http://admin:admin123@localhost:4516/deployit/internal/download/foo")));
    }
    
    @Test
    void testGetArgosActionTemplate() {
        assertThat(ArgosConfiguration.getArgosActionTemplate(ActionOnInvalid.ABORT), is("Package: %s has an invalid Argos Notary Verification"));
        assertThat(ArgosConfiguration.getArgosActionTemplate(ActionOnInvalid.WARN), is("Package: %s has an invalid Argos Notary Verification\nThis is a warning"));
    }
    
    @Test
    void testGetArgosValidTemplate() {
        assertThat(ArgosConfiguration.getArgosValidTemplate(), is("Package: %s is valid according Argos Notary Verification"));
    }

}
