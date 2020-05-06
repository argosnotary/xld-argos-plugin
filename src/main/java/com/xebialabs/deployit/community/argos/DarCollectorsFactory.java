package com.xebialabs.deployit.community.argos;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.FileCollector;
import com.rabobank.argos.argos4j.RemoteFileCollector;
import com.rabobank.argos.argos4j.RemoteZipFileCollector;
import com.rabobank.argos.argos4j.RemoteFileCollector.RemoteFileCollectorBuilder;
import com.xebialabs.deployit.community.argos.model.XldClientConfig;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.xebialabs.deployit.plugin.api.udm.artifact.SourceArtifact;
import com.xebialabs.deployit.plugin.credentials.Credentials;
import com.xebialabs.deployit.plugin.credentials.UsernamePasswordCredentials;

import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.auth.BasicAuthRequestInterceptor;

public class DarCollectorsFactory {
	
	private DarCollectorsFactory() {}
	
	public static List<FileCollector> getCollectors(ExecutionContext context, Version version) {
        
        List<FileCollector> collectors = new ArrayList<>();
        
        collectors.add(getDarCollector(context, version));
        collectors.addAll(getRemoteFileCollectors(context, version));
        
        return collectors;
		
	}
	
    private static RemoteZipFileCollector getDarCollector(ExecutionContext context, Version version) {
        XldClientConfig xldConf = ArgosConfiguration.getXldClientConfig(context);
        String downloadKey = getVersionDownloadKey(xldConf, version.getId());
        return RemoteZipFileCollector.builder()
                .url(ArgosConfiguration.getXldUrlForExport(downloadKey))
                .username(xldConf.getUsername()).password(xldConf.getPassword().toCharArray())
                .build();

    }
    
    private static List<FileCollector> getRemoteFileCollectors(ExecutionContext context, Version version) {
        List<FileCollector> fileCollectors = new ArrayList<>();
        version.getDeployables().forEach(deployable -> {
            if (deployable instanceof SourceArtifact) {
                String uri = ((SourceArtifact) deployable).getFileUri();
                if (!uri.startsWith("internal:")) {
                    RemoteFileCollectorBuilder fileCollectorBuilder = null;
                    try {
                        fileCollectorBuilder = RemoteFileCollector.builder().url(new URL(uri));
                    } catch (MalformedURLException e) {
                        context.logError(String.format("Exception during Argos Notary verify: [%s]", e.getMessage()));
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
    
    private static String getVersionDownloadKey(XldClientConfig xldConf, String versionId) {
        String keyUrl = ArgosConfiguration.getXldUrlForDownloadKey(versionId);
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.method(Request.HttpMethod.GET);
        requestTemplate.target(keyUrl);
        addXldAuthorization(xldConf, requestTemplate);
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
    
    private static void addXldAuthorization(XldClientConfig xldConf, RequestTemplate requestTemplate) {
        new BasicAuthRequestInterceptor(xldConf.getUsername(), xldConf.getPassword()).apply(requestTemplate);
    }
	
	

}
