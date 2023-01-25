package it.govhub.openapi.codegen;

import java.util.List;
import java.util.Map;

import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.languages.SpringCodegen;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;

/**
 * Estensione del generatore openapi di Spring.
 * 
 * Per evitare conflitto con l'utilizzo di hateoas che aggiunge l'attributo links a tutti i beans facendoli ereditare da un
 * RepresentationModel, rimuoviamo dall'OpenAPI l'attributo links.
 * 
 * In questo modo è possibile continuare a descriverli nel dettaglio all'interno dello YAML, senza andare in conflitto
 * con hateoas.
 *
 */
public class GovhubCodegenGenerator extends SpringCodegen {
	
    @Override
    public String getName() {
        return "govhub-codegen";
    }
    
    @Override
    public Map<String, ModelsMap> postProcessAllModels(Map<String, ModelsMap> objs) {
    	objs = super.postProcessAllModels(objs);
    	
        for (ModelsMap modelsAttrs : objs.values()) {
            for (ModelMap mo : modelsAttrs.getModels()) {
                CodegenModel codegenModel = mo.getModel();
                
                findAndRemoveIndexLinks(codegenModel.vars);
                findAndRemoveIndexLinks(codegenModel.allVars);
                findAndRemoveIndexLinks(codegenModel.optionalVars);
                findAndRemoveIndexLinks(codegenModel.requiredVars);
                findAndRemoveIndexLinks(codegenModel.nonNullableVars);
                findAndRemoveIndexLinks(codegenModel.readOnlyVars);
                findAndRemoveIndexLinks(codegenModel.readWriteVars);
            }
        }
    	return objs;
    }
    
    private void findAndRemoveIndexLinks(List<CodegenProperty> props) {
    	
    	int i;
    	for (i=0; i<props.size(); i++) {
    		if (props.get(i).baseName.equals("_links")) {
    			props.remove(i);
    			break;
    		}
    	}
    }

}
