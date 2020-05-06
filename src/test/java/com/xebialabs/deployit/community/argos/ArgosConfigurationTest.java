package com.xebialabs.deployit.community.argos;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

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
import com.xebialabs.deployit.plugin.api.udm.Environment;

@ExtendWith(MockitoExtension.class)
class ArgosConfigurationTest {
    
    @Mock
    XldClientConfig xldConf;
    
    @Mock
    Repository repository;
    
    @Mock
    ExecutionContext context;
    
    @Mock
    Environment environment;

    @BeforeEach
    void setUp() throws Exception {
    }
    
    @Test
    void testGetActionOnInvalid() {
        when(environment.hasProperty("actionOnInvalid")).thenReturn(false);
        assertThat(ArgosConfiguration.getActionOnInvalid(environment), is(ActionOnInvalid.NONE));
        when(environment.hasProperty("actionOnInvalid")).thenReturn(true);
        when(environment.getProperty(ArgosConfiguration.ENV_PROPERTY_ACTION_ON_INVALID)).thenReturn(ActionOnInvalid.NONE);
        assertThat(ArgosConfiguration.getActionOnInvalid(environment), is(ActionOnInvalid.NONE));
        when(environment.getProperty(ArgosConfiguration.ENV_PROPERTY_ACTION_ON_INVALID)).thenReturn(ActionOnInvalid.WARN);
        assertThat(ArgosConfiguration.getActionOnInvalid(environment), is(ActionOnInvalid.WARN));
        when(environment.getProperty(ArgosConfiguration.ENV_PROPERTY_ACTION_ON_INVALID)).thenReturn(ActionOnInvalid.ABORT);
        assertThat(ArgosConfiguration.getActionOnInvalid(environment), is(ActionOnInvalid.ABORT));
    }

    @Test
    void testIsEnabled() {
        when(environment.hasProperty("verifyWithArgos")).thenReturn(false);
        assertThat(ArgosConfiguration.isEnabled(environment), is(false));
        when(environment.hasProperty("verifyWithArgos")).thenReturn(true);
        when(environment.getProperty(ArgosConfiguration.PROPERTY_VERIFY_WITH_ARGOS)).thenReturn(ArgosVerificationStatus.ENABLED);
        assertThat(ArgosConfiguration.isEnabled(environment), is(true));
    }
    
    @Test
    void testGetXldClientConfig() {
    	when(context.getRepository()).thenReturn(repository);
        when(repository.read("Configuration/config/administration/argos/xldconfig")).thenReturn(xldConf);
        assertThat(ArgosConfiguration.getXldClientConfig(context), is(xldConf));
    }
    
    @Test
    void testGetArgosServerBaseUrl() {
        assertThat(ArgosConfiguration.getArgosServerBaseUrl(), is("http://foo-service:8080/api"));
    }
    
    @Test
    void testGetXldUrlForExport() throws URISyntaxException, MalformedURLException {
    	URL url = ArgosConfiguration.getXldUrlForExport("foo");
        assertThat(url, is(new URL("http://localhost:4516/deployit/internal/download/foo")));
    }
    
    @Test
    void testGetXldUrlForDownloadKey() throws URISyntaxException, MalformedURLException {
    	String url = ArgosConfiguration.getXldUrlForDownloadKey("foo");
        assertThat(url, is("http://localhost:4516/deployit/export/deploymentpackage/foo"));
    }
    
    @Test
    void testGetArgosActionTemplate() {
    	assertThat(ArgosConfiguration.getArgosActionTemplate(ActionOnInvalid.NONE), is("Package: %s has an invalid Argos Notary Verification but an action is not defined"));
        assertThat(ArgosConfiguration.getArgosActionTemplate(ActionOnInvalid.ABORT), is("Package: %s has an invalid Argos Notary Verification, abort"));
        assertThat(ArgosConfiguration.getArgosActionTemplate(ActionOnInvalid.WARN), is("Package: %s has an invalid Argos Notary Verification, this is a warning"));
    }
    
    @Test
    void testGetArgosValidTemplate() {
        assertThat(ArgosConfiguration.getArgosValidTemplate(), is("Package: %s is valid according Argos Notary Verification"));
    }

}
