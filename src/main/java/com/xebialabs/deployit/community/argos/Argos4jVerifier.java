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
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.FileCollector;
import com.rabobank.argos.argos4j.VerificationResult;
import com.rabobank.argos.argos4j.FileCollector.FileCollectorType;
import com.rabobank.argos.argos4j.FileCollectorSettings;
import com.xebialabs.deployit.community.argos.model.NonPersonalAccount;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.udm.Version;

import feign.Client;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.auth.BasicAuthRequestInterceptor;

public class Argos4jVerifier {

    public static boolean versionIsValid(ExecutionContext context, String versionId) {
        Version version = context.getRepository().read(versionId);
        String supplyChain = version.getApplication().getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN);
        NonPersonalAccount npaAccount = version.getApplication().getProperty(ArgosConfiguration.PROPERTY_ARGOS_PERSONAL_ACCOUNT);
        if (npaAccount == null) {
            context.logError(String.format("Argos NPA not set on Application %s", version.getApplication().getName()));
            return false;
        }
        if (supplyChain == null) {
            context.logError(String.format("Argos Supply Chain not set on Application %s", version.getApplication().getName()));
            return false;
        }
        char[] passphrase = npaAccount.getPassphrase().toCharArray();
        
        List<String> path = getPath(supplyChain);
        String supplyChainName = getSupplyChainName(supplyChain);
        

        context.logOutput(String.format("Supply Chain Name: [%s]", supplyChainName));
        context.logOutput(String.format("Supply Chain Path: [%s]", path));
        
        String downloadKey = null;
        try {
            downloadKey = getVersionDownloadKey(context, version.getId());
        } catch (MalformedURLException e) {
            context.logError(String.format("Exception during Argos Notary verify: %s", e.getMessage()));
            return false;
        }
        
        Argos4jSettings settings = Argos4jSettings.builder()
                .pathToLabelRoot(path)
                .supplyChainName(supplyChainName)
                .signingKeyId(npaAccount.getKeyId())
                .argosServerBaseUrl(ArgosConfiguration.getArgosServerBaseUrl()).build();
        Argos4j argos4j = new Argos4j(settings);
        FileCollector darCollect = FileCollector.builder()
                .uri(ArgosConfiguration.getXldUriForExport(context, downloadKey))
                .settings(FileCollectorSettings.builder().build())
                .type(FileCollectorType.REMOTE_ZIP)
                .build();
        
        VerificationResult verifyResult = null;
        try {
            verifyResult = argos4j.getVerifyBuilder().addFileCollector(darCollect).verify(passphrase);
        } catch (Argos4jError exc) {
            context.logError(String.format("Exception during Argos Notary verify: %s", exc.getMessage()));
            return false;
        }
        // TODO process remote artifacts
        if (verifyResult != null && verifyResult.isRunIsValid()) {
            context.logOutput(String.format("Application %s version %s is valid according to Argos Notary", version.getApplication().getName(), version.getName()));
            return true;
        } else {
            context.logError(String.format("Application %s version %s is invalid according to Argos Notary", version.getApplication().getName(), version.getName()));
            return false;
        }
    }
    
    private static String getVersionDownloadKey(ExecutionContext context, String versionId) throws MalformedURLException {
        URI keyUri = ArgosConfiguration.getXldUriForDownloadKey(context, versionId);
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.method(Request.HttpMethod.GET);
        requestTemplate.target(keyUri.toURL().toString());
        addAuthorization(keyUri, requestTemplate);
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
    
    private static void addAuthorization(URI uri, RequestTemplate requestTemplate) {
        Optional.ofNullable(uri.getUserInfo())
                .map(userInfo -> userInfo.split(":"))
                .filter(userInfo -> userInfo.length == 2)
                .ifPresent(userInfo -> new BasicAuthRequestInterceptor(userInfo[0], userInfo[1]).apply(requestTemplate));
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
