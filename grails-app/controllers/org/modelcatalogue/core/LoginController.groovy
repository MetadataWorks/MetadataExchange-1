package org.modelcatalogue.core

import grails.config.Config
import grails.converters.JSON
import grails.core.support.GrailsConfigurationAware
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import org.modelcatalogue.core.security.MetadataOauthService
import org.modelcatalogue.core.util.marshalling.CatalogueElementMarshaller
import org.springframework.context.MessageSource
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.WebAttributes
import javax.servlet.http.HttpServletResponse

class LoginController implements GrailsConfigurationAware {

    String serverUrl

    MessageSource messageSource

    MetadataOauthService metadataOauthService

    @Override
    void setConfiguration(Config co) {
        serverUrl = co.getProperty('grails.serverURL', String)
    }

    /**
     * Dependency injection for the authenticationTrustResolver.
     */
    AuthenticationTrustResolver authenticationTrustResolver

    /**
     * Dependency injection for the springSecurityService.
     */
    SpringSecurityService springSecurityService

    /**
     * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
     */
    def index() {
        if (springSecurityService.isLoggedIn()) {
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        } else {
            redirect action: 'auth', params: params
        }
    }

    /**
     * Show the login page.
     */
    def auth() {
        // This solves the problem with infinite redirect loop when accessing main url witout trailing slash.
        // This is super dirty hack which actually redirects this request to trailing slash - ajax login.
        String redirectUrl = SpringSecurityUtils.getSavedRequest(session)?.redirectUrl
        if ( redirectUrl && !serverUrl?.endsWith('/') && redirectUrl == serverUrl ) {
            redirect uri: "${SpringSecurityUtils.getSavedRequest(session).redirectUrl}/"
            return
        }

        def config = SpringSecurityUtils.securityConfig

        if (springSecurityService.isLoggedIn()) {
            redirect uri: config.successHandler.defaultTargetUrl
            return
        }

        Map oauthServices = metadataOauthService.findAllOauthServices()

        String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
        response.setHeader('X-S2Auth-PostUrl', postUrl)
        [postUrl: postUrl, rememberMeParameter: config.rememberMe.parameter, oauthServices: oauthServices]
    }

    /**
     * The redirect action for Ajax requests.
     */
    def authAjax() {
        response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
        response.sendError HttpServletResponse.SC_UNAUTHORIZED
    }

    /**
     * Show denied page.
     */
    def denied() {
        response.sendError HttpServletResponse.SC_UNAUTHORIZED
    }

    /**
     * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
     */
    def full() {
        def config = SpringSecurityUtils.securityConfig
        render view: 'auth', params: params,
                model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
                        postUrl  : "${request.contextPath}${config.apf.filterProcessesUrl}"]
    }

    /**
     * Callback after a failed login. Redirects to the auth page with a warning message.
     */
    def authfail() {
        String msg = ''
        def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
        if (exception) {
            String messageCode = AuthenticationUtils.i18nCodePerException(exception)
            msg = messageSource.getMessage(messageCode, [] as Object[], 'Authentication failed', request.locale)
        }

        if (springSecurityService.isAjax(request)) {
            render([error: msg] as JSON)
        } else {
            flash.message = msg
            redirect action: 'auth', params: params
        }
    }

    /**
     * The Ajax success redirect url.
     *
     * You need to return roles array in this result to make security.hasRole angular service working
     *
     *
     */
    def ajaxSuccess() {
        if (!springSecurityService.currentUser) {
            render([success: false] as JSON)
            return
        }
        render([
            success: true,
            username: springSecurityService.authentication.name,
            roles: springSecurityService.authentication.authorities*.authority,
            id: springSecurityService.authentication.hasProperty('id') ? springSecurityService.authentication.id : null,
            dataModels: springSecurityService.authentication.hasProperty('id') ? [springSecurityService.authentication.dataModel?.collect{ CatalogueElementMarshaller.minimalCatalogueElementJSON(it) }] : []
        ] as JSON)
    }

    /**
     * The Ajax denied redirect url.
     */
    def ajaxDenied() {
        response.sendError HttpServletResponse.SC_UNAUTHORIZED
        render([error: 'access denied'] as JSON)
    }
}
