/** Generated automatically from conceptualDomain. Do not edit this file manually! */
    (function (window) {
        window['fixtures'] = window['fixtures'] || {};
        var fixtures = window['fixtures']
        fixtures['conceptualDomain'] = fixtures['conceptualDomain'] || {};
        var conceptualDomain = fixtures['conceptualDomain']

        window.fixtures.conceptualDomain.incoming6 = {
   "total": 11,
   "previous": "/conceptualDomain/55/incoming/relationship?max=2&offset=8",
   "page": 2,
   "itemType": "org.modelcatalogue.core.Relationship",
   "listType": "org.modelcatalogue.core.util.Relationships",
   "next": "",
   "list": [{
      "id": 272,
      "direction": "destinationToSource",
      "removeLink": "/conceptualDomain/55/incoming/relationship",
      "relation": {
         "id": 52,
         "outgoingRelationships": {
            "count": 1,
            "itemType": "org.modelcatalogue.core.Relationship",
            "link": "/conceptualDomain/52/outgoing"
         },
         "description": "this is a container for the domain for university libraries",
         "name": "university libraries",
         "link": "/conceptualDomain/52",
         "isContextFor": {
            "count": 0,
            "itemType": "org.modelcatalogue.core.Relationship",
            "link": "/conceptualDomain/52/outgoing/context"
         },
         "elementTypeName": "Conceptual Domain",
         "includes": {
            "count": 0,
            "itemType": "org.modelcatalogue.core.Relationship",
            "link": "/conceptualDomain/52/outgoing/inclusion"
         },
         "elementType": "org.modelcatalogue.core.ConceptualDomain",
         "incomingRelationships": {
            "count": 0,
            "itemType": "org.modelcatalogue.core.Relationship",
            "link": "/conceptualDomain/52/incoming"
         },
         "version": 1
      },
      "type": {
         "id": 3,
         "sourceClass": "org.modelcatalogue.core.CatalogueElement",
         "sourceToDestination": "relates to",
         "destinationClass": "org.modelcatalogue.core.CatalogueElement",
         "name": "relationship",
         "link": "/relationshipType/3",
         "elementTypeName": "Relationship Type",
         "elementType": "org.modelcatalogue.core.RelationshipType",
         "destinationToSource": "is relationship of",
         "version": 0
      }
   }],
   "offset": 10,
   "success": true,
   "size": 1
}

    })(window)