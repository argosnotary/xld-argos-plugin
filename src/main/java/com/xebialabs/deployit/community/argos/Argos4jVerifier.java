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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.FileCollector;
import com.rabobank.argos.argos4j.VerificationResult;
import com.xebialabs.deployit.community.argos.model.NonPersonalAccount;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.udm.Version;

public class Argos4jVerifier {

    public static boolean versionIsValid(ExecutionContext context, String versionId) {
        Version version = context.getRepository().read(versionId);
        String supplyChain = version.getApplication().getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN);
        NonPersonalAccount npaAccount = version.getApplication().getProperty(ArgosConfiguration.PROPERTY_ARGOS_PERSONAL_ACCOUNT);
        if (npaAccount == null) {
            context.logError(String.format("Argos NPA not set on deployment package %s", version.getName()));
            return false;
        }
        if (supplyChain == null) {
            context.logError(String.format("Argos Supply Chain not set on Application %s", version.getApplication().getName()));
            return false;
        }
        char[] passphrase = npaAccount.getPassphrase().toCharArray();
        
        Argos4jSettings settings = Argos4jSettings.builder()
                .pathToLabelRoot(getPath(supplyChain))
                .supplyChainName(getSupplyChainName(supplyChain))
                .signingKeyId(npaAccount.getKeyId())
                .argosServerBaseUrl(ArgosConfiguration.getArgosServerBaseUrl()).build();
        Argos4j argos4j = new Argos4j(settings);
        FileCollector darCollect = FileCollector.builder().uri(ArgosConfiguration.getXldUriForExport(context, version.getId())).build();
                
        VerificationResult verifyResult = argos4j.getVerifyBuilder().addFileCollector(darCollect).verify(passphrase);
        // TODO process remote artifacts
        return verifyResult.isRunIsValid();
    }
        
    private static String getSupplyChainName(String supplyChain) {
        return supplyChain.split(":")[1];
    }

    private static List<String> getPath(String supplyChain) {
        List<String> labelList = Arrays.asList(supplyChain.split(":")[0].split("."));
        Collections.reverse(labelList);
        return labelList;
    }
}
