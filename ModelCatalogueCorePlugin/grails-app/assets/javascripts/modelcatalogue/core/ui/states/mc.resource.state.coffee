angular.module('mc.core.ui.states.mc.resource', []).config(['$stateProvider', ($stateProvider) ->

    $stateProvider.state 'mc.resource', {
      abstract: true
      url: '/:resource'
      templateUrl: 'modelcatalogue/core/ui/state/parent.html'
    }
])