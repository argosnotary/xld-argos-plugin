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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.Delegate;
import com.xebialabs.deployit.plugin.api.udm.Parameters;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.argosnotary.argos.argos4j.Argos4jError;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.flow.StepExitCode;

public class ArgosCollectLinkTask {
	
	private ArgosCollectLinkTask() {}
	
	private static final String LAYOUT_SEGMENT_NAME_ARGUMENT = "layoutSegmentName";
	private static final String STEP_NAME_ARGUMENT = "stepName";
	
	@Delegate(name="collectArgosLink")
	public static List<Step> collectArgosLink(ConfigurationItem ci, String method, Map<String, String> arguments, Parameters parameters) {
        Step step = new Step(){

            public int getOrder() {
                return 1;
            }

            public String getDescription() {
                return "Collect Argos Link Object";
            }

            public StepExitCode execute(ExecutionContext ctx) throws NoSuchAlgorithmException, IOException {
            	Version version = (Version) ci;
            	ctx.logOutput(String.format("Collect artifacts for Application [%s] with version [%s]", version.getApplication().getName(), version.getName()));
                try {
	            	ArgosCollectLink.collectLink(ctx, version, parameters.getProperty(LAYOUT_SEGMENT_NAME_ARGUMENT), parameters.getProperty(STEP_NAME_ARGUMENT));
	                ctx.logOutput(String.format("Link object stored in Argos Notary for Application [%s] version [%s]", version.getApplication().getName(), version.getName()));
	                ctx.logOutput(String.format("for segment: [%s] and step: [%s]", parameters.getProperty(LAYOUT_SEGMENT_NAME_ARGUMENT), parameters.getProperty(STEP_NAME_ARGUMENT)));
	                return StepExitCode.SUCCESS;
            	} catch (Argos4jError exc) {
            		ctx.logError(String.format("Exception during Argos Notary add Link: [%s]", exc.getMessage()));
            		return StepExitCode.FAIL;
            }
            }
        };
        List<Step> stepList = new ArrayList<>();
        stepList.add(step);
        return stepList;
    }
}
