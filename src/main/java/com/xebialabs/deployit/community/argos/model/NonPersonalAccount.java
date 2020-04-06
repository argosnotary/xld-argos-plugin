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
package com.xebialabs.deployit.community.argos.model;

import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;
import com.xebialabs.deployit.plugin.api.udm.TypeIcon;
import com.xebialabs.deployit.plugin.api.udm.base.BaseConfigurationItem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Metadata(root = Metadata.ConfigurationItemRoot.ENVIRONMENTS, label = "Non Personal Account", description = "An Non Personal Account used for the Argos Notary Service")
@TypeIcon(value="icons/types/argos.NonPersonalAccount.svg")
@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
public class NonPersonalAccount extends BaseConfigurationItem {

    @Property(required = true, label = "Key identifier of the Non Personal Account on the Argos Service")
    private String keyId;

    @Property(required = true, password = true, label = "Passphrase used to authenticate to the Argos Service")
    private String passphrase;

}
