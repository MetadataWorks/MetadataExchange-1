
#= require jquery/dist/jquery
#= require bootstrap/dist/js/bootstrap
#= require ng-file-upload-shim/angular-file-upload-shim
#= require angular/angular
#= require angular-i18n/angular-locale_en-gb
#= require angular-animate/angular-animate
#= require ng-file-upload/angular-file-upload
#= require angular-http-auth/src/http-auth-interceptor
#= require angular-loading-bar/build/loading-bar
#= require angular-ui-router/release/angular-ui-router
#= require modelcatalogue/util/index
#= require modelcatalogue/util/ui/index
#= require modelcatalogue/core/index
#= require modelcatalogue/core/ui/index
#= require modelcatalogue/core/ui/states/index
#= require modelcatalogue/core/ui/bs/index

@grailsAppName = 'model_catalogue'

metadataCurator = angular.module('metadataCurator', [
  'demo.config'
  'mc.core.ui.bs'
  'mc.core.ui.states'
  'ui.bootstrap'
  'angular-loading-bar'
  'ngAnimate'
])

metadataCurator.config ['$stateProvider', '$urlRouterProvider', ($stateProvider, $urlRouterProvider)->
  $urlRouterProvider.otherwise("/dashboard")
]

metadataCurator.run ['$templateCache', ($templateCache) ->
  $templateCache.put 'modelcatalogue/core/ui/omnisearchItem.html', '''
  <a>
      <span class="omnisearch-icon" ng-class="match.model.icon"></span>
      <span ng-if="!match.model.highlight" bind-html-unsafe="match.label"></span>
      <span ng-if=" match.model.highlight" bind-html-unsafe="match.label | typeaheadHighlight:query"></span>
  </a>
'''
]

metadataCurator.controller('metadataCurator.searchCtrl',
  ['catalogueElementResource', 'modelCatalogueSearch', '$scope', '$rootScope', '$log', '$q', '$state', 'names', 'messages'
    (catalogueElementResource, modelCatalogueSearch, $scope, $rootScope, $log, $q, $state, names, messages)->
      actions = []

      $scope.search = (item, model, label) ->
        if angular.isString(item)
          $state.go('mc.search', {q: model })
        else
          item?.action item, model, label

      $scope.clearSelection = ->
        $state.searchSelect = undefined
        $rootScope.$stateParams.q = undefined
        $state.go('.', {q: undefined })

      initActions = ->
        actions = []
        actions.push {
          condition: (term) -> term
          label: (term) ->
            "Search <strong>Catalogue Element</strong> for <strong>#{term}</strong>"

          action: (term) -> ->
              $state.go('mc.search', {q: term})

          icon: 'fa fa-fw fa-search'
        }

        actions.push {
          condition: (term) -> term and $state.includes("mc.resource.list.**") and  $state.$current.params.indexOf('q') >= 0 and $state.params.resource
          label: (term) ->
            naturalName = names.getNaturalName($state.params.resource)
            "Search any <strong>#{naturalName}</strong> for <strong>#{term}</strong>"
          action: (term) ->
            ->
              $state.go('mc.resource.list', {q: term})
          icon: 'fa fa-fw fa-search'
        }

        actions.push {
          condition: (term) -> term and $state.current.name == 'mc.resource.show.property' and  $state.$current.params.indexOf('q') >= 0 and $rootScope.$$searchContext
          label: (term) ->
            "Search current <strong>#{$rootScope.$$searchContext}</strong> for <strong>#{term}</strong>"
          action: (term) ->
            ->
              $state.go('mc.resource.show.property', {q: term})
          icon: 'fa fa-fw fa-search'
        }

        actions.push {
          condition: -> true
          label: (term) ->
            if $rootScope.elementToShow?.isInstanceOf 'valueDomain' and $rootScope.elementToShow?.rule
              "Validate <strong>#{term}</strong> by <strong>#{$rootScope.elementToShow.name}</strong>"
            else
              "Validate <strong>#{term}</strong>"

          action: (term) ->
            ->
              messages.prompt('', '', {type: 'validate-value-by-domain', value: term, domainHint: if $rootScope.elementToShow?.rule then $rootScope.elementToShow else undefined})
          icon: 'fa fa-fw fa-check-circle-o'
        }

        actions.push {
          condition: -> true
          label: (term) ->
            if $rootScope.elementToShow?.isInstanceOf 'valueDomain' and $rootScope.elementToShow?.mappings?.total > 0
              "Convert <strong>#{term}</strong> from <strong>#{$rootScope.elementToShow.name}</strong>"
            else
              "Convert <strong>#{term}</strong>"
          action: (term) ->
            ->
              messages.prompt('', '', {type: 'convert-with-value-domain', value: term, sourceHint: if $rootScope.elementToShow?.mappings?.total > 0 then $rootScope.elementToShow else undefined})
          icon: 'fa fa-fw fa-long-arrow-right'
        }

      $scope.getResults = (term) ->
        deferred = $q.defer()

        results = []

        return if not term

        for action in actions when action.condition(term)
          results.push {
            label:  action.label(term)
            action: action.action(term)
            icon:   action.icon
            term:   term
          }

        deferred.notify results

        if term
          modelCatalogueSearch(term).then (searchResults)->
            for searchResult in searchResults.list
              results.push {
                label:      if searchResult.getLabel then searchResult.getLabel() else searchResult.name
                action:     searchResult.show
                icon:       if searchResult.getIcon  then searchResult.getIcon()  else 'glyphicon glyphicon-file'
                term:       term
                highlight:  true
              }

            deferred.resolve results
        else
          deferred.resolve results

        deferred.promise

      initActions()

      $scope.$on '$stateChangeSuccess', (event, toState, toParams) ->
        $scope.searchSelect = toParams.q

  ])

metadataCurator.controller('metadataCurator.userCtrl', ['$scope', 'security', ($scope, security)->
  $scope.logout = ->
    security.logout()
  $scope.login = ->
    security.requireLogin()
])