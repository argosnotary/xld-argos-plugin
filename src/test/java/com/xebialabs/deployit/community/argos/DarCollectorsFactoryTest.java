package com.xebialabs.deployit.community.argos;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xebialabs.deployit.community.argos.model.XldClientConfig;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.services.Repository;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.api.udm.Version;

import feign.RequestTemplate;

@ExtendWith(MockitoExtension.class)
class DarCollectorsFactoryTest {
    
    @Mock
    XldClientConfig xldConf;
    
    @Mock
    Repository repository;
    
    @Mock
    ExecutionContext context;
    
    @Mock
    Version version;

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testGetCollectors() {
    	when(context.getRepository()).thenReturn(repository);
		when(repository.read("Configuration/config/administration/argos/xldconfig")).thenReturn(xldConf);
		when(xldConf.getUsername()).thenReturn("foo");
		when(xldConf.getPassword()).thenReturn("bar");
		when(version.getId()).thenReturn("version1");
		Throwable exception = assertThrows(ArgosError.class, () -> {
			DarCollectorsFactory.getCollectors(context, version);
        });
        assertEquals("Connection refused (Connection refused)", exception.getMessage());
	}

}
