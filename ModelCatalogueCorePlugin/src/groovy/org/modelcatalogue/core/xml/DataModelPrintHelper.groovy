package org.modelcatalogue.core.xml

import grails.gorm.DetachedCriteria
import org.modelcatalogue.core.*
import org.modelcatalogue.core.util.ClassificationFilter

class DataModelPrintHelper extends CatalogueElementPrintHelper<Classification> {

    @Override
    String getTopLevelName() {
        "dataModel"
    }

    @Override
    void processElements(Object theMkp, Classification element, PrintContext context, Relationship rel) {
        super.processElements(theMkp, element, context, rel)

        for (CatalogueElement other in context.modelService.getTopLevelModels(ClassificationFilter.includes(element), [:]).items) {
                printElement(theMkp, other, context, null)
        }

        for (Class<? extends CatalogueElement> type in [DataClass, DataElement, ValueDomain, DataType, MeasurementUnit]) {
            for (CatalogueElement other in allClassified(type, element, context)) {
                if (!context.wasPrinted(other)) {
                    printElement(theMkp, other, context, null)
                }
            }
        }
    }


    private static <E extends CatalogueElement> List<E> allClassified(Class<E> type, Classification classification, PrintContext context) {
        DetachedCriteria<E> criteria = new DetachedCriteria<E>(type).build {
            'in'('status', [org.modelcatalogue.core.api.ElementStatus.DEPRECATED, org.modelcatalogue.core.api.ElementStatus.FINALIZED, org.modelcatalogue.core.api.ElementStatus.DRAFT])
            not {
                'in'('id', context.idsOfPrinted)
            }
        }
        context.classificationService.classified(criteria, ClassificationFilter.includes([classification])).list()
    }
}