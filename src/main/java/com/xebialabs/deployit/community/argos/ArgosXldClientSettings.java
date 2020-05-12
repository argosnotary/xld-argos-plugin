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
import com.rabobank.argos.domain.PathHelper;
import com.xebialabs.deployit.community.argos.model.NonPersonalAccount;
import com.xebialabs.deployit.plugin.api.flow.ExecutionContext;
import com.xebialabs.deployit.plugin.api.udm.Version;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class ArgosXldClientSettings {
	
	private final Argos4j argos4j;
    private final char[] passphrase;
    
    @Builder
    public ArgosXldClientSettings(ExecutionContext context, Version version) {
    	String supplyChain = version.getApplication().getProperty(ArgosConfiguration.PROPERTY_ARGOS_SUPPLYCHAIN);
        NonPersonalAccount npaAccount = version.getApplication().getProperty(ArgosConfiguration.PROPERTY_ARGOS_PERSONAL_ACCOUNT);
        if (npaAccount == null) {
            context.logError(String.format("Argos NPA not set on Application [%s]", version.getApplication().getName()));
            throw new ArgosError("Argos NPA not set on Application");
        }
        if (supplyChain == null) {
            context.logError(String.format("Argos Supply Chain not set on Application [%s]", version.getApplication().getName()));
            throw new ArgosError("Argos Supply Chain not set on Application");
        }
        passphrase = npaAccount.getPassphrase().toCharArray();
        
        List<String> path = PathHelper.getSupplyChainPath(supplyChain);
        String supplyChainName = PathHelper.getSupplyChainName(supplyChain);
        
        Argos4jSettings settings = Argos4jSettings.builder()
                .path(path)
                .supplyChainName(supplyChainName)
                .signingKeyId(npaAccount.getKeyId())
                .argosServerBaseUrl(ArgosConfiguration.getArgosServerBaseUrl()).build();
        argos4j = new Argos4j(settings);
    }

}
