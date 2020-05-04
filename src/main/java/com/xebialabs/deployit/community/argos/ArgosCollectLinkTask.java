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

public class ArgosCollectLinkTask {
	
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
                ArgosCollectLink.collectLink(ctx, version, parameters.getProperty(LAYOUT_SEGMENT_NAME_ARGUMENT), parameters.getProperty(STEP_NAME_ARGUMENT));
                ctx.logOutput(String.format("Link object stored in Argos Notary for Application [%s] version [%s]", version.getApplication().getName(), version.getName()));
                ctx.logOutput(String.format("for segment: [%s] and step: [%s]", parameters.getProperty(LAYOUT_SEGMENT_NAME_ARGUMENT), parameters.getProperty(STEP_NAME_ARGUMENT)));
                return StepExitCode.SUCCESS;
            }
        };
        List<Step> stepList = new ArrayList<>();
        stepList.add(step);
        return stepList;
    }
}
