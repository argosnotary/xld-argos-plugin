package com.xebialabs.deployit.community.argos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.xebialabs.deployit.community.argos.model.NonPersonalAccount;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.udm.Application;
import com.xebialabs.deployit.plugin.api.udm.Version;

@ExtendWith(MockitoExtension.class)
class ArgosXldClientSettingsTest {
    
    private String APPLICATION_NAME = "Applications/aaa/argos-tes-app";
    private String SUPPLYCHAIN = "root_label.child_label:argos-test-app";
    private String NPA_PASSPHRASE = "bar";
    private String NPA_KEY_ID = "key_id";
    private String XLD_CLIENT_CONFIG_ID = "Configuration/config/administration/argos/xldconfig";
    private String XLD_USERNAME = "xldUsername";
    private String XLD_PASSWORD = "xldPassword";
	
	@Mock
	Version version;
	
	@Mock
	ExecutionContext context;
	
	@Mock
	NonPersonalAccount npaAccount;
	
	@Mock
	Application application;
	
	Argos4j argos4j;
	
	ArgosXldClientSettings expectedSettings;
	

	@BeforeEach
	void setUp() throws Exception {
		Argos4jSettings settings = Argos4jSettings.builder()
                .pathToLabelRoot(Arrays.asList("child_label", "root_label"))
                .supplyChainName("argos-test-app")
                .signingKeyId(NPA_KEY_ID)
                .argosServerBaseUrl(ArgosConfiguration.getArgosServerBaseUrl()).build();
		expectedSettings = new ArgosXldClientSettings(new Argos4j(settings), NPA_PASSPHRASE.toCharArray());
	}

	@Test
	void settingsTest() {
		when(version.getApplication()).thenReturn(application);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN)).thenReturn(SUPPLYCHAIN);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_PERSONAL_ACCOUNT)).thenReturn(null);
        Throwable exception = assertThrows(ArgosError.class, () -> {
        	new ArgosXldClientSettings(context, version);
        });
        assertEquals("Argos NPA not set on Application", exception.getMessage());
        
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN)).thenReturn(null);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_PERSONAL_ACCOUNT)).thenReturn(npaAccount);
        exception = assertThrows(ArgosError.class, () -> {
        	new ArgosXldClientSettings(context, version);
        });
        assertEquals("Argos Supply Chain not set on Application", exception.getMessage());
        
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN)).thenReturn(SUPPLYCHAIN);
        when(npaAccount.getPassphrase()).thenReturn(NPA_PASSPHRASE);
        when(npaAccount.getKeyId()).thenReturn(NPA_KEY_ID);
        ArgosXldClientSettings settings = new ArgosXldClientSettings(context, version);
        boolean eq = expectedSettings.equals(settings);
        
        assertEquals(expectedSettings, settings);
        
	}

}
