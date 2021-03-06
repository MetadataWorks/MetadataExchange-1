package org.modelcatalogue.core.publishing

import org.modelcatalogue.core.CatalogueElement
import org.modelcatalogue.core.api.ElementStatus
import org.modelcatalogue.core.util.FriendlyErrors
import org.modelcatalogue.core.util.HibernateHelper
import org.modelcatalogue.core.util.builder.ProgressMonitor
import org.springframework.validation.ObjectError
import rx.Observer

@Deprecated
class LegacyFinalizationChain extends PublishingChain {


    private LegacyFinalizationChain(CatalogueElement published) {
        super(published)
    }

    static LegacyFinalizationChain create(CatalogueElement published) {
        return new LegacyFinalizationChain(published)
    }

    protected CatalogueElement doRun(Publisher<CatalogueElement> publisher, Observer<String> monitor) {
        if (published.published || isUpdatingInProgress(published)) {
            return published
        }

        if (published.status != ElementStatus.DRAFT) {
            published.errors.rejectValue('status', 'org.modelcatalogue.core.CatalogueElement.element.must.be.draft', 'Element is not draft!')
            return published
        }

        monitor.onNext("Finalizing $published ...")

        startUpdating()

        for (CatalogueElement element in required) {
            if (!element.published) {
                return rejectRequiredDependency(element)
            }
        }

        for (Collection<CatalogueElement> elements in queue) {
            for (CatalogueElement element in elements) {
                if (element.id in processed || isUpdatingInProgress(element)) {
                    continue
                }
                if (!ableToFinalize(element)) {
                    element.errors.rejectValue('status', 'org.modelcatalogue.core.CatalogueElement.dependency.not.finalized', "Dependencies outside the current data model $published.dataModel must be finalized.")
                    return rejectFinalizationDependency(element, monitor)
                }
                processed << element.id
                CatalogueElement finalized = element.publish(publisher, ProgressMonitor.NOOP)
                if (finalized.hasErrors()) {
                    return rejectFinalizationDependency(finalized, monitor)
                }
            }
        }
        return doPublish(publisher, monitor)
    }

    private boolean ableToFinalize(CatalogueElement element) {
        if (element.status == ElementStatus.FINALIZED) {
            // it's finalized already
            return true
        }

        if (element.dataModel == published.dataModel) {
            // in the same data model
            return true
        }

        if (element.dataModel == published) {
            // the data model is the initiator of the publish chain
            return true
        }
        return false
    }

    private CatalogueElement doPublish(Publisher<CatalogueElement> archiver, Observer<String> monitor) {
        published.status = ElementStatus.FINALIZED
        published.save(flush: true, deepValidate: false)

        if (published.latestVersionId) {
            List<CatalogueElement> previousFinalized = HibernateHelper.getEntityClass(published).findAllByLatestVersionIdAndStatus(published.latestVersionId, ElementStatus.FINALIZED)
            for (CatalogueElement e in previousFinalized) {
                if (e != published) {
                    archiver.archive(e, true)
                }
            }
        }

        monitor.onNext("... finalized $published")

        published
    }


    private CatalogueElement rejectFinalizationDependency(CatalogueElement element, Observer<String> monitor) {
        monitor.onError(new IllegalArgumentException(FriendlyErrors.printErrors("Rejected dependency for $element", element.errors)))
        restoreStatus()
        for (ObjectError error in element.errors.getFieldErrors('status')) {
            published.errors.reject(error.code, error.arguments, error.defaultMessage)
        }
        for (ObjectError error in element.errors.globalErrors) {
            published.errors.reject(error.code, error.arguments, error.defaultMessage)
        }
        published.errors.reject('org.modelcatalogue.core.CatalogueElement.cannot.finalize.dependency', "Cannot finalize dependency ${element}, please, resolve the issue first. You'll see more details when you try to finalize it manualy")
        published
    }

    private CatalogueElement rejectRequiredDependency(CatalogueElement element) {
        restoreStatus()
        published.errors.reject('org.modelcatalogue.core.CatalogueElement.required.finalization.dependency', "Dependency ${element} is not finalized. Please, finalize it first.")
        published
    }

}
