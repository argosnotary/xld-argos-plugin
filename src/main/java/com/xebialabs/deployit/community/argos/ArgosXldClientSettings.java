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

import java.util.List;

import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.domain.SupplyChainHelper;
import com.xebialabs.deployit.community.argos.model.ServiceAccount;
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
        ServiceAccount saAccount = version.getApplication().getProperty(ArgosConfiguration.PROPERTY_ARGOS_SERVICE_ACCOUNT);
        if (saAccount == null) {
            context.logError(String.format("Argos SA not set on Application [%s]", version.getApplication().getName()));
            throw new ArgosError("Argos SA not set on Application");
        }
        if (supplyChain == null) {
            context.logError(String.format("Argos Supply Chain not set on Application [%s]", version.getApplication().getName()));
            throw new ArgosError("Argos Supply Chain not set on Application");
        }
        passphrase = saAccount.getPassphrase().toCharArray();
        
        List<String> path = SupplyChainHelper.getSupplyChainPath(supplyChain);
        String supplyChainName = SupplyChainHelper.getSupplyChainName(supplyChain);
        
        Argos4jSettings settings = Argos4jSettings.builder()
                .path(path)
                .supplyChainName(supplyChainName)
                .keyId(saAccount.getKeyId())
                .argosServerBaseUrl(ArgosConfiguration.getArgosServerBaseUrl()).build();
        argos4j = new Argos4j(settings);
    }

}
