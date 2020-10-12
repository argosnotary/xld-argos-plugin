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

import com.argosnotary.argos.argos4j.LinkBuilder;
import com.argosnotary.argos.argos4j.LinkBuilderSettings;
import com.xebialabs.deployit.community.argos.model.XldClientConfig;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.udm.Version;

public class ArgosCollectLink {
    
    private ArgosCollectLink() {}

    public static boolean collectLink(ExecutionContext context, Version version, String layoutSegmentName, String stepName) {
    	ArgosXldClientSettings settings = ArgosXldClientSettings.builder()
    			.context(context)
    			.version(version).build();
    	
        LinkBuilderSettings linkBuilderSettings = LinkBuilderSettings.builder()
        		.layoutSegmentName(layoutSegmentName)
        		.stepName(stepName)
        		.runId(version.getName())
        		.build();
        
        LinkBuilder linkBuilder = settings.getArgos4j().getLinkBuilder(linkBuilderSettings);
        

        XldClientConfig xldConf = ArgosConfiguration.getXldClientConfig(context.getRepository());
        
        DarCollectorsFactory.getCollectors(xldConf, version).forEach(linkBuilder::collectProducts);
        
        linkBuilder.store(settings.getPassphrase());
	    return true;        
    }
}
