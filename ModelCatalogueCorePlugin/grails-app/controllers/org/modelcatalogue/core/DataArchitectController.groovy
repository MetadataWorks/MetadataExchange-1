package org.modelcatalogue.core

import org.modelcatalogue.core.dataarchitect.CSVService
import org.modelcatalogue.core.util.Elements
import org.modelcatalogue.core.util.ListWithTotal
import org.modelcatalogue.core.util.ListWithTotalAndType
import org.modelcatalogue.core.util.Lists
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile

class DataArchitectController extends AbstractRestfulController<CatalogueElement> {

    static responseFormats = ['json', 'xlsx','xml','xsd']

    def dataArchitectService
    def modelService
    @Autowired CSVService csvService

    DataArchitectController() {
        super(CatalogueElement, false)
    }

    def index(){}

    def uninstantiatedDataElements(Integer max){
        handleParams(max)
        respond Lists.wrap(params, DataElement, "/dataArchitect/uninstantiatedDataElements", dataArchitectService.uninstantiatedDataElements(params))
    }


    def metadataKeyCheck(Integer max){
        handleParams(max)
        respond Lists.wrap(params, DataElement, "/dataArchitect/metadataKeyCheck", dataArchitectService.metadataKeyCheck(params))
    }

    def getSubModelElements(){
        Long id = params.long('modelId') ?: params.long('id')
        respond Lists.lazy(params, DataElement, "/dataArchitect/getSubModelElements") {
            if (id){
                Model model = Model.get(id)
                ListWithTotalAndType<Model> subModels = modelService.getSubModels(model)
                return modelService.getDataElementsFromModels(subModels.items).items
            }
            return []
        }
    }

    def findRelationsByMetadataKeys(Integer max){
        handleParams(max)
        ListWithTotal results
        def keyOne = params.keyOne
        def keyTwo = params.keyTwo
        if(keyOne && keyTwo) {
            try {
                results = dataArchitectService.findRelationsByMetadataKeys(keyOne, keyTwo, params)
            } catch (Exception e) {
                println(e)
                return
            }

            //FIXME we need new method to do this and integrate it with the ui
            try {
                dataArchitectService.actionRelationshipList(results.items)
            } catch (Exception e) {
                println(e)
                return
            }

            respond Lists.wrap(params, Relationship, "/dataArchitect/findRelationsByMetadataKeys", results)

        }else{
            respond "please enter keys"
        }

    }

    def modelsFromCSV(){
        MultipartFile file = request.getFile('csv')

        if (!file) {
            respond status: HttpStatus.BAD_REQUEST
            return
        }

        List<Object> elements = []

        file.inputStream.withReader {
            elements = dataArchitectService.matchModelsWithCSVHeaders(csvService.readHeaders(it, params.separator ?: ';'))
        }

        respond elements
    }

    def elementsFromCSV(){
        MultipartFile file = request.getFile('csv')

        if (!file) {
            respond status: HttpStatus.BAD_REQUEST
            return
        }

        List<Object> elements = []

        file.inputStream.withReader {
            elements = dataArchitectService.matchDataElementsWithCSVHeaders(csvService.readHeaders(it, params.separator ?: ';'))
        }

        respond elements
    }

    def generateSuggestions() {
        try {
            dataArchitectService.generateMergeModelActions()
            respond status: HttpStatus.OK
        } catch (e) {
            log.error("Error generating suggestions", e)
            respond status: HttpStatus.BAD_REQUEST
        }
    }
	
	def gelCreateFormXML() {
		try {
			Model model=Model.get(params.id) 
			respond Lists.wrap(params,"/dataArchitect/gelXmlModelShredder/${params.id}",modelService.gelXmlModelShredder(model))
		
			
		} catch (e) {
			log.error("Error generating xmlmodel", e)
			respond status: HttpStatus.BAD_REQUEST
		}
	}
	
	/**
	 * Return XSD file for 
	 * @return
	 */
	def printXSDModel(){
		def model=Model.get(params.id)
		def result= modelService.printXSDModel(Model.get(params.id))
		try {
			
			//response.addHeader("Content-disposition", "inline; filename="+"\"${model.name}.xml\"")
			//response.outputStream << result
			//response.outputStream.flush()
			//response.flushBuffer()
			respond Lists.wrap(params, DataElement, "/dataArchitect/printXSDModel/${params.id}",result)
			//render (status: HttpStatus.OK,text:result)
		}catch(e){
			respond status: HttpStatus.BAD_REQUEST
		}
		
		//response.addHeader("Content-disposition", "inline; filename="+"\"${model.name}.xml\"")
		//response.outputStream << result
		//response.outputStream.flush()
		//response.flushBuffer()

	}


}
