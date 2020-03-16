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

import com.xebialabs.deployit.community.argos.model.ActionOnInvalid;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.flow.StepExitCode;
import com.xebialabs.deployit.plugin.api.udm.Version;

public class ArgosVerificationStep implements Step {
    
    private Version version;

    public ArgosVerificationStep(Version version) {
        this.version = version;
    }
    
    @Override
    public StepExitCode execute(ExecutionContext context) throws Exception {
        if (Argos4jVerifier.versionIsValid(context, version)) {
            context.logOutput(String.format(ArgosConfiguration.getArgosValidTemplate(), version.getName()));
            return StepExitCode.SUCCESS;
        } else {
            return handleFail(context);
        }
    }
    
    private StepExitCode handleFail(ExecutionContext context) {
        if (ActionOnInvalid.ABORT.equals(ArgosConfiguration.getActionOnInvalid())) {
            context.logError(String.format(ArgosConfiguration.getArgosActionTemplate(ActionOnInvalid.ABORT), version.getName()));
            return StepExitCode.FAIL;
        } else {
            context.logError(String.format(ArgosConfiguration.getArgosActionTemplate(ActionOnInvalid.WARN), version.getName()));
            return StepExitCode.SUCCESS;
        }
    }

    @Override
    public String getDescription() {
        return "Argos Notary Verification Step";
    }

    @Override
    public int getOrder() {
        return 0;
    }
    
    @Override
    public boolean skippable() {
        return false;
    }

}
