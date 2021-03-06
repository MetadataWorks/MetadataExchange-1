package org.modelcatalogue.core.util.builder

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.modelcatalogue.core.CatalogueElement
import org.modelcatalogue.core.Relationship
import org.modelcatalogue.builder.api.RelationshipConfiguration
import org.modelcatalogue.core.util.FriendlyErrors

@Log4j @CompileStatic
class RelationshipProxy<T extends CatalogueElement, U extends CatalogueElement> {

    final String relationshipTypeName
    final CatalogueElementProxy<T> source
    final CatalogueElementProxy<U> destination
    final Map<String, String> extensions = [:]
    final boolean archived

    RelationshipProxy(String relationshipTypeName, CatalogueElementProxy<T> source, CatalogueElementProxy<T> destination, Closure extensions) {
        this.relationshipTypeName = relationshipTypeName
        this.source = source
        this.destination = destination

        RelationshipConfiguration configuration = new DefaultRelationshipConfiguration()
        configuration.with extensions

        if (configuration.extensions) {
            this.extensions.putAll(configuration.extensions)
        }

        this.archived = configuration.archived
    }

    RelationshipProxy(String relationshipTypeName, CatalogueElementProxy<T> source, CatalogueElementProxy<T> destination, Map<String, String> extensions, boolean archived = false) {
        this.relationshipTypeName = relationshipTypeName
        this.source = source
        this.destination = destination
        if (extensions) {
            this.extensions.putAll(extensions)
        }
        this.archived = archived
    }

    Relationship resolve(CatalogueElementProxyRepository repository) {
        try {
            Relationship relationship = repository.resolveRelationship(this)
            if (relationship.hasErrors()) {
                log.error(FriendlyErrors.printErrors("Cannot create relationship of type $relationshipTypeName between $source and $destination", relationship.errors))
                throw new IllegalStateException(FriendlyErrors.printErrors("Cannot create relationship of type $relationshipTypeName between $source and $destination", relationship.errors))
            }
            if (extensions) {
                relationship.ext.putAll(extensions)
            }
            return relationship
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve $this:\n\n$e", e)
        }

    }

    @Override
    String toString() {
        "Proxy for Relationship[type: $relationshipTypeName, source: $source, destination: $destination]"
    }
}
