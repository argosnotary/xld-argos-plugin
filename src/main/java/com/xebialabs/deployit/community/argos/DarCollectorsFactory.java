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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.argosnotary.argos.argos4j.FileCollector;
import com.argosnotary.argos.argos4j.RemoteFileCollector;
import com.argosnotary.argos.argos4j.RemoteZipFileCollector;
import com.argosnotary.argos.argos4j.RemoteFileCollector.RemoteFileCollectorBuilder;
import com.xebialabs.deployit.community.argos.model.XldClientConfig;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.xebialabs.deployit.plugin.api.udm.artifact.SourceArtifact;
import com.xebialabs.deployit.plugin.credentials.Credentials;
import com.xebialabs.deployit.plugin.credentials.UsernamePasswordCredentials;

import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.auth.BasicAuthRequestInterceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DarCollectorsFactory {
	
	private DarCollectorsFactory() {}
	
	public static List<FileCollector> getCollectors(XldClientConfig xldConf, Version version) {
        
        List<FileCollector> collectors = new ArrayList<>();
        
        collectors.add(getDarCollector(xldConf, version.getId()));
        collectors.addAll(getRemoteFileCollectors(version));
        
        return collectors;
		
	}
	
	public static List<FileCollector> getCollectors(XldClientConfig xldConf, String versionId, List<Map<String, String>> remoteDeployables) {        
        List<FileCollector> collectors = new ArrayList<>();
        
        collectors.add(getDarCollector(xldConf, versionId));
        remoteDeployables.forEach(deployable -> collectors.add(
        		getRemoteFileCollector(deployable.get("uri"), deployable.get("username"), deployable.get("password"))));
        
        return collectors;
		
	}
	
    private static RemoteZipFileCollector getDarCollector(XldClientConfig xldConf, String versionId) {
        String downloadKey = getVersionDownloadKey(xldConf, versionId);
        return RemoteZipFileCollector.builder()
                .url(ArgosConfiguration.getXldUrlForExport(downloadKey))
                .username(xldConf.getUsername()).password(xldConf.getPassword().toCharArray())
                .build();
    }
    
    private static List<FileCollector> getRemoteFileCollectors(Version version) {
        List<FileCollector> fileCollectors = new ArrayList<>();
        version.getDeployables().forEach(deployable -> {
            if (deployable instanceof SourceArtifact) {
                String uri = ((SourceArtifact) deployable).getFileUri();
                if (!uri.startsWith("internal:")) {
                    RemoteFileCollectorBuilder fileCollectorBuilder = null;
                    try {
                        fileCollectorBuilder = RemoteFileCollector.builder().url(new URL(uri));
                    } catch (MalformedURLException e) {
                        throw new ArgosError(String.format("Exception during Argos Notary verify: [%s]", e.getMessage()));
                    }
                    Optional<Credentials> credentials = Optional
                            .ofNullable(((SourceArtifact) deployable).getCredentials());
                    if (credentials.isPresent()) {
                        fileCollectorBuilder
                            .username(((UsernamePasswordCredentials) credentials.get()).getUsername())
                            .password(((UsernamePasswordCredentials) credentials.get()).getPassword().toCharArray());
                    }
                    fileCollectors.add(fileCollectorBuilder.build());    
                }
            }
        });
        return fileCollectors;
    }
    
	private static FileCollector getRemoteFileCollector(String uri, String username, String password) {
		log.info("Create remote file collector"+ uri + " "+username+" "+password);
		RemoteFileCollectorBuilder fileCollectorBuilder = null;
		try {
			fileCollectorBuilder = RemoteFileCollector.builder().url(new URL(uri));
		} catch (MalformedURLException e) {
			throw new ArgosError(String.format("Exception during Argos Notary verify: [%s]", e.getMessage()));
		}
		if (username != null && password != null) {
			fileCollectorBuilder.username(username).password(password.toCharArray());
		}
		return fileCollectorBuilder.build();
	}
    
    private static String getVersionDownloadKey(XldClientConfig xldConf, String versionId) {
        String keyUrl = ArgosConfiguration.getXldUrlForDownloadKey(versionId);
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.method(Request.HttpMethod.GET);
        requestTemplate.target(keyUrl);
        new BasicAuthRequestInterceptor(xldConf.getUsername(), xldConf.getPassword()).apply(requestTemplate);
        Client client = new Client.Default(null, null);
        Request request = requestTemplate.resolve(new HashMap<>()).request();
        try (Response response = client.execute(request, new Request.Options())) {
            if (response.status() == 200) {
                return response.body().toString();
            } else {
                throw new ArgosError("status code : " + response.status() + " returned");
            }
        } catch (IOException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

}
