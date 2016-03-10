package x.org.modelcatalogue.core

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.modelcatalogue.builder.api.CatalogueBuilder
import org.modelcatalogue.core.AbstractIntegrationSpec
import org.modelcatalogue.core.DataClass
import org.modelcatalogue.core.DataModel
import org.modelcatalogue.core.ElementService
import org.modelcatalogue.core.api.ElementStatus
import org.modelcatalogue.integration.obo.OboLoader
import spock.lang.Shared
import spock.lang.Unroll

class HPOUpdatesSpec extends AbstractIntegrationSpec  {

    @Shared GrailsApplication grailsApplication

    CatalogueBuilder catalogueBuilder
    ElementService elementService


    def setup() {
        initRelationshipTypes()
    }

    @Unroll
    def "update HPO with id pattern #idPattern"() {
        OboLoader loader = new OboLoader(catalogueBuilder)

        when:
        InputStream test1 = getClass().getResourceAsStream('test1.obo')
        loader.load(test1, 'HPO', idPattern)

        then:
        noExceptionThrown()

        when:
        DataModel hpo = DataModel.findByName('HPO')

        then:
        hpo
        hpo.countDeclares() == 12

        when:
        hpo = elementService.finalizeDataModel(hpo, hpo.semanticVersion, "Imported from HBO")

        then:
        noExceptionThrown()
        hpo
        hpo.status == ElementStatus.FINALIZED
        hpo.countDeclares() == 12

        when:
        InputStream test2 = getClass().getResourceAsStream('test2.obo')
        loader.load(test2, 'HPO', idPattern)
        DataModel hpoDraft = DataModel.findByNameAndStatus('HPO', ElementStatus.DRAFT)

        then:
        noExceptionThrown()
        hpoDraft
        hpoDraft.countDeclares() == 13

        when:
        DataClass renalCyst = DataClass.findByNameAndDataModel('Renal cyst', hpoDraft)
        DataClass something = DataClass.findByNameAndDataModel('Something Different', hpoDraft)

        then:
        renalCyst
        something
        something in renalCyst.parentOf


        where:
        idPattern << [
            "${grailsApplication.config.grails.serverURL}/catalogue/ext/${URLEncoder.encode(OboLoader.OBO_ID, 'UTF-8')}/:id".toString().replace(':id', '$id'),
            'http://purl.obolibrary.org/obo/${id.replace(\'%3A\', \'_\')}'
        ]

    }


}
