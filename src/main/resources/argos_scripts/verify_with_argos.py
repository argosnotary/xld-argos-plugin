'''
Created on Oct 19, 2018

@author: borstg
'''
import com.xebialabs.deployit.community.argos.ArgosConfiguration as ArgosConfiguration
from scripts.restconfiguration.util import RestApiServer

global params, thisCi, context, permissionService, metadataService, repositoryService

currentUser = context.getTask().getUsername()

# check if user has read permission on deployment package
if not permissionService.isGrantedToMe("read", thisCi.id):
    context.logOutput("User [%s] has not the permission to set a release check\n" % (ArgosConfiguration.getArgosVerification()))
else:
    context.logOutput("Argos verification is set at [%s]\n" % (ArgosConfiguration.getArgosVerification()))
    
