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

import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.VerificationResult;
import com.rabobank.argos.argos4j.VerifyBuilder;
import com.xebialabs.deployit.community.argos.model.XldClientConfig;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.udm.Version;

public class ArgosVerifier {
    
    private ArgosVerifier() {}

    public static boolean versionIsValid(ExecutionContext context, Version version) {
    	ArgosXldClientSettings settings = ArgosXldClientSettings.builder()
    			.context(context)
    			.version(version).build();
    	
        VerifyBuilder verifyBuilder = settings.getArgos4j().getVerifyBuilder();
        
        XldClientConfig xldConf = ArgosConfiguration.getXldClientConfig(context.getRepository());
        DarCollectorsFactory.getCollectors(xldConf, version).forEach(verifyBuilder::addFileCollector);
        
        VerificationResult verifyResult = null;
        try {
            verifyResult = verifyBuilder.verify();
        } catch (Argos4jError exc) {
            context.logError(String.format("Exception during Argos Notary verify: [%s]", exc.getMessage()));
            return false;
        }
        if (verifyResult != null && verifyResult.isRunIsValid()) {
            context.logOutput(String.format("Application [%s] version [%s] is valid according to Argos Notary", version.getApplication().getName(), version.getName()));
            return true;
        } else {
            context.logError(String.format("Application [%s] version [%s] is invalid according to Argos Notary", version.getApplication().getName(), version.getName()));
            return false;
        }
    }
}
