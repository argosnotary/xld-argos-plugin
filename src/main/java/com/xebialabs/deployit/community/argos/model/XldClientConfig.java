/**
 * Copyright (C) 2020 Argos Notary Coöperatie UA
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
import lombok.Setter;

@SuppressWarnings("serial")
@Metadata(root = Metadata.ConfigurationItemRoot.CONFIGURATION, description = "Server Configuration for REST api")
@TypeIcon(value="icons/types/xld.svg")
@EqualsAndHashCode(callSuper=false)
@Setter
public class XldClientConfig extends BaseConfigurationItem {

    @Property(required = true)
    private String username;

    @Property(password = true)
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
}
