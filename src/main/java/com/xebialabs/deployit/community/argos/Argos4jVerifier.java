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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.FileCollector;
import com.rabobank.argos.argos4j.RemoteFileCollector;
import com.rabobank.argos.argos4j.RemoteFileCollector.RemoteFileCollectorBuilder;
import com.rabobank.argos.argos4j.RemoteZipFileCollector;
import com.rabobank.argos.argos4j.VerificationResult;
import com.rabobank.argos.argos4j.VerifyBuilder;
import com.xebialabs.deployit.community.argos.model.NonPersonalAccount;
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

public class Argos4jVerifier {

    public static boolean versionIsValid(ExecutionContext context, Version version) {
        String supplyChain = version.getApplication().getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN);
        NonPersonalAccount npaAccount = version.getApplication().getProperty(ArgosConfiguration.PROPERTY_ARGOS_PERSONAL_ACCOUNT);
        if (npaAccount == null) {
            context.logError(String.format("Argos NPA not set on Application [%s]", version.getApplication().getName()));
            return false;
        }
        if (supplyChain == null) {
            context.logError(String.format("Argos Supply Chain not set on Application [%s]", version.getApplication().getName()));
            return false;
        }
        char[] passphrase = npaAccount.getPassphrase().toCharArray();        

        XldClientConfig xldConf = ArgosConfiguration.getXldClientConfig(context);
        
        List<String> path = getPath(supplyChain);
        String supplyChainName = getSupplyChainName(supplyChain);
        
        String downloadKey = getVersionDownloadKey(context, xldConf, version.getId());
        
        Argos4jSettings settings = Argos4jSettings.builder()
                .pathToLabelRoot(path)
                .supplyChainName(supplyChainName)
                .signingKeyId(npaAccount.getKeyId())
                .argosServerBaseUrl(ArgosConfiguration.getArgosServerBaseUrl()).build();
        Argos4j argos4j = new Argos4j(settings);
        
        VerifyBuilder verifyBuilder = argos4j.getVerifyBuilder();
        RemoteZipFileCollector darCollect = RemoteZipFileCollector.builder()
                .url(ArgosConfiguration.getXldUrlForExport(context, downloadKey))
                .username(xldConf.getUsername())
                .password(xldConf.getPassword().toCharArray())
                .build();
        
        verifyBuilder.addFileCollector(darCollect);

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
                    verifyBuilder.addFileCollector(fileCollectorBuilder.build());

                }
            }
        });
        
        VerificationResult verifyResult = null;
        try {
            verifyResult = verifyBuilder.verify(passphrase);
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
    
    private static String getVersionDownloadKey(ExecutionContext context, XldClientConfig xldConf, String versionId) {
        String keyUrl = ArgosConfiguration.getXldUrlForDownloadKey(context, versionId);
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
                throw new Argos4jError("status code : " + response.status() + " returned");
            }
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }
    
    private static void addXldAuthorization(XldClientConfig xldConf, RequestTemplate requestTemplate) {
        new BasicAuthRequestInterceptor(xldConf.getUsername(), xldConf.getPassword()).apply(requestTemplate);
    }
        
    public static String getSupplyChainName(String supplyChain) {
        return supplyChain.split(":")[1];
    }

    public static List<String> getPath(String supplyChain) {
        List<String> labelList = Arrays.asList(supplyChain.split(":")[0].split("\\."));
        Collections.reverse(labelList);
        return labelList;
    }
}
