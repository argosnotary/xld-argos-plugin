#
# Copyright (C) 2019 - 2020 Rabobank Nederland
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import com.xebialabs.deployit.community.argos.ArgosCollectArtifactList as ArgosCollectArtifactList
import com.xebialabs.deployit.community.argos.ArgosError as ArgosError
import com.xebialabs.deployit.community.argos.ArgosConfiguration as ArgosConfiguration
import com.xebialabs.deployit.community.argos.model.XldClientConfig as XldClientConfig

import com.xebialabs.deployit.plugin.api.udm.artifact.SourceArtifact as SourceArtifact
import com.xebialabs.deployit.exception.NotFoundException as NotFoundException
import com.xebialabs.deployit.util.PasswordEncrypter as PasswordEncrypter

import com.rabobank.argos.argos4j.internal.mapper.RestMapper;

from com.xebialabs.deployit.plugin.api.reflect import Type
import sys
import logging
from __builtin__ import None

global repositoryService, context

logging.basicConfig(stream=sys.stdout, level="INFO")

def collect_artifacts(applicationName, versionName):
    cis = repositoryService.query(Type.valueOf("udm.Application"), None, None, applicationName, None, None, 0, -1)
    if cis:
        versionId = cis[0].getId()+'/'+versionName
        try:
            version = repositoryService.read(versionId)
        except NotFoundException as exc:
            response.setStatusCode(404)
            logging.error("On Application [%s] version [%s] not found" % (applicationName, versionName))
            return
            
        xldConf = None
        try:
            xldConf = repositoryService.read(ArgosConfiguration.getXldClientConfigId());
        except NotFoundException as exc:
            response.setStatusCode(500)
            logging.error("XLD Client Config [%s] not found" % ArgosConfiguration.getXldClientConfigId())
            return
        
        
        tempDeployables = version.getDeployables()
        remoteDeployables = []
        for deployable in tempDeployables:
            deployable = repositoryService.read(str(deployable))
            if deployable.hasProperty('fileUri') \
                and not deployable.getFileUri() is None \
                and not deployable.getFileUri().startswith('internal:'):
                dep = {}
                dep['uri'] = deployable.getFileUri()
                if not deployable.getCredentials() is None:
                    creds = repositoryService.read(str(deployable.getCredentials()))
                    dep['username'] = creds.getUsername()
                    dep['password'] = creds.getPassword()
                remoteDeployables.append(dep)
        # for backwards compatibility
        password = None
        if PasswordEncrypter.getInstance().isEncrypted(xldConf.getPassword()):
            password = PasswordEncrypter.getInstance().decrypt(xldConf.getPassword())
        else:
            password = xldConf.getPassword()
        try:
            
            response.setEntity(ArgosCollectArtifactList.collectArtifacts(xldConf.getUsername(), password, versionId, remoteDeployables))
        except Exception as exc:
            logging.error("During artifact collect %s", exc.message)
            response.setStatusCode(400)
    else:
        response.setStatusCode(404)
        logging.error("Application [%s] unknown" % applicationName)
     

applicationName = None
versionName = None

if not 'application' in request.query:
    logging.error("Query parameter application not set")
    response.setStatusCode(400)
else:
    applicationName = request.query['application']
    
if not 'version' in request.query:
    logging.error("Query parameter version not set")
    response.setStatusCode(400)
else:
    versionName = request.query['version']

if not applicationName is None and not versionName is None:
    collect_artifacts(applicationName, versionName)
    

    