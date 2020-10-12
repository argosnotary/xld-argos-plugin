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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xebialabs.deployit.community.argos.model.XldClientConfig;
import com.xebialabs.deployit.plugin.api.udm.Version;

@ExtendWith(MockitoExtension.class)
class DarCollectorsFactoryTest {
    
    @Mock
    XldClientConfig xldConf;
    
    @Mock
    Version version;

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testGetCollectors() {
    	when(xldConf.getUsername()).thenReturn("foo");
		when(xldConf.getPassword()).thenReturn("bar");
		when(version.getId()).thenReturn("version1");
		Throwable exception = assertThrows(ArgosError.class, () -> {
			DarCollectorsFactory.getCollectors(xldConf, version);
        });
        assertEquals("Connection refused (Connection refused)", exception.getMessage());
	}

}
