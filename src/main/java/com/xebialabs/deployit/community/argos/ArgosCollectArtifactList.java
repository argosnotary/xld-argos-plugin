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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.ArtifactListBuilder;
import com.rabobank.argos.domain.link.Artifact;
import com.xebialabs.deployit.community.argos.model.XldClientConfig;

public class ArgosCollectArtifactList {
    
    private ArgosCollectArtifactList() {}

    public static List<Map<String, String>> collectArtifacts(String username, String password, String versionId, List<Map<String, String>> remoteDeployables) {
    	XldClientConfig xldConf =  new XldClientConfig();
    	xldConf.setUsername(username);
    	xldConf.setPassword(password);
    	List<Map<String, String>> artifacts = new ArrayList<>();
    	ArtifactListBuilder artifactListBuilder = Argos4j.getArtifactListBuilder();
        
    	DarCollectorsFactory.getCollectors(xldConf, versionId, remoteDeployables).forEach(artifactListBuilder::addFileCollector);
        
        artifactListBuilder.collect().forEach(artifact -> artifacts.add(toArtifactMap(artifact)));        
        
        return artifacts;
    }
    
    private static Map<String, String> toArtifactMap(Artifact artifact) {
    	Map<String, String> artifactMap = new HashMap<>();
    	artifactMap.put("uri", artifact.getUri());
    	artifactMap.put("hash", artifact.getHash());
    	return artifactMap;
    }
}
