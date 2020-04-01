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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.plugin.api.deployment.planning.Contributor;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Deltas;
import com.xebialabs.deployit.plugin.api.udm.Environment;

public class ArgosVerificationStepGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ArgosVerificationStepGenerator.class);
    
    private ArgosVerificationStepGenerator() {}
    
    @Contributor
    public static void contribute(Deltas deltas, DeploymentPlanningContext context) {
        final Environment environment = context.getDeployedApplication().getEnvironment();
        boolean noVerifyOperations = deltas.getDeltas().stream().map(Delta::getOperation).allMatch(ArgosConfiguration.OPERATIONS_WITHOUT_VERIFICATION::contains);
        if (noVerifyOperations) {
            logger.info("no verification");
        } else {
            if (ArgosConfiguration.isEnabled(environment)) {
                context.addStep(new ArgosVerificationStep(context.getDeployedApplication().getVersion(),
                        ArgosConfiguration.getActionOnInvalid(environment)));
            }
        }
    }
}
