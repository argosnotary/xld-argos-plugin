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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.xebialabs.deployit.community.argos.model.ServiceAccount;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.udm.Application;
import com.xebialabs.deployit.plugin.api.udm.Version;

@ExtendWith(MockitoExtension.class)
class ArgosXldClientSettingsTest {
    
    private String SUPPLYCHAIN = "root_label.child_label:argos-test-app";
    private String SA_PASSPHRASE = "bar";
    private String SA_KEY_ID = "key_id";
    
	@Mock
	Version version;
	
	@Mock
	ExecutionContext context;
	
	@Mock
	ServiceAccount saAccount;
	
	@Mock
	Application application;
	
	Argos4j argos4j;
	
	ArgosXldClientSettings expectedSettings;
	

	@BeforeEach
	void setUp() throws Exception {
		Argos4jSettings settings = Argos4jSettings.builder()
                .path(Arrays.asList("root_label", "child_label"))
                .supplyChainName("argos-test-app")
                .keyId(SA_KEY_ID)
                .argosServerBaseUrl(ArgosConfiguration.getArgosServerBaseUrl()).build();
		expectedSettings = new ArgosXldClientSettings(new Argos4j(settings), SA_PASSPHRASE.toCharArray());
		
		
	}

	@Test
	void settingsTest() {
		when(version.getApplication()).thenReturn(application);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN)).thenReturn(SUPPLYCHAIN);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_SERVICE_ACCOUNT)).thenReturn(null);
        Throwable exception = assertThrows(ArgosError.class, () -> {
        	new ArgosXldClientSettings(context, version);
        });
        assertEquals("Argos SA not set on Application", exception.getMessage());
        
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN)).thenReturn(null);
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_SERVICE_ACCOUNT)).thenReturn(saAccount);
        exception = assertThrows(ArgosError.class, () -> {
        	new ArgosXldClientSettings(context, version);
        });
        assertEquals("Argos Supply Chain not set on Application", exception.getMessage());
        
        when(application.getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN)).thenReturn(SUPPLYCHAIN);
        when(saAccount.getPassphrase()).thenReturn(SA_PASSPHRASE);
        when(saAccount.getKeyId()).thenReturn(SA_KEY_ID);
        ArgosXldClientSettings settings = new ArgosXldClientSettings(context, version);
        
        assertEquals(expectedSettings, settings);
        
	}

}
