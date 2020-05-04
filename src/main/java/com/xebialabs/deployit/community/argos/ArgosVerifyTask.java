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
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.flow.StepExitCode;

public class ArgosVerifyTask {
	
	@Delegate(name="verifyWithArgos")
	public static List<Step> collectArgosLink(ConfigurationItem ci, String method, Map<String, String> arguments, Parameters parameters) {
        Step step = new Step(){

            public int getOrder() {
                return 1;
            }

            public String getDescription() {
                return "Argos Verify";
            }

            public StepExitCode execute(ExecutionContext ctx) throws NoSuchAlgorithmException, IOException {
            	Version version = (Version) ci;
            	if (ArgosVerifier.versionIsValid(ctx, version)) {
            		ctx.logOutput("This package has a valid Argos Notary Verification");            		
            	} else {
            		ctx.logError("This package has an invalid Argos Notary Verification");
            	}
            	
            	return StepExitCode.SUCCESS;
                
            }
        };
        List<Step> stepList = new ArrayList<>();
        stepList.add(step);
        return stepList;
    }
}
