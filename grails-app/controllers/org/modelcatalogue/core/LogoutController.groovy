package org.modelcatalogue.core

import grails.plugin.springsecurity.SpringSecurityUtils

class LogoutController {

    /**
     * Index action. Redirects to the Spring security logout uri.
     */
    def index = {
        flash.message = flash.message
        flash.error   = flash.error
        // TODO put any pre-logout code here
        redirect absolute: true, uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/logoff'
    }
}
