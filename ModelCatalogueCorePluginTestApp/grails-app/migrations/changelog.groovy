databaseChangeLog = {
    include file: 'changelog_000_ddl.groovy'
    include file: 'changelog_001_archivedToDeprecated.groovy'
    include file: 'changelog_002_movingUserToCore.groovy'
    include file: 'changelog_003_relationshipClassification.groovy'
    include file: 'changelog_004_favourites.groovy'
    include file: 'changelog_005_newModelCatalogueId.groovy'
    include file: 'changelog_006_filterByClassifications.groovy'
    include file: 'changelog_007_versioning_changes.groovy'
}
