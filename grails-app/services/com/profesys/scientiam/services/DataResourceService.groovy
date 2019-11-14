package com.profesys.scientiam.services

import com.profesys.scientiam.resource.DataResource
import com.profesys.scientiam.resource.DataResourceType
import grails.gorm.transactions.Transactional
import org.grails.web.util.WebUtils

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import groovy.util.logging.Log4j



@Transactional
class DataResourceService {

   def list(String ticketParam) {
        def result = [:]
        def fail = { Map m ->
            result.error = [ code: m.code, args: ["Links"] ]
            return result
        }
		
		def entryCriteria = DataResource.createCriteria()
		
		result.dataResourceInstanceList  = entryCriteria.list {
			and{
			  eq("ticket",ticketParam)
				gt(state,0)
			}
			    
	   
		  }
       // params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        //result.linksInstanceList  = Book.list(params)
        result.linksInstanceTotal = result.linksInstanceList.size()
 
			log.debug ('result.linksInstanceList : ' +result.linksInstanceList  )
			log.debug ('result.linksInstanceTotal : ' +result.linksInstanceTotal  )
        // Success.
        return result
    }
 
   
   @Transactional
   def saveURL(DataResource dataResourceInstance) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session

	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["dataResourceInstance"] ]
		   return result
	   }

	   log.debug ( 'params.properties: '   + params.properties)
	   log.error ( 'params.dataResource: ' + params.dataResource)

	   if (dataResourceInstance== null) {
//		   notFound()
		   log.error ( 'Link Inexistente: ' )
		   result.error ="Link inexistente"
		   return fail(result)
	   }
	   //Lo marco como modificación porque si entro luego a la creación de dataResourceInstanceverificaríamos que es nuevo y lo pisa con estado 1
	   //Creo el registro de dataResource
	   // def  dataResourceInstance
	   dataResourceInstance.userUpdated     = session.user
	   if (!dataResource.id ) {


		   dataResourceInstance.id      	     =  TransactionService.serviceID('DR',session.user.toString() )
		   dataResourceInstance.ticket  		 = params.ticket
		   dataResourceInstance.dataResourceType = DataResourceType.get(params.dataResourceType.id)
		   dataResourceInstance.state           = 1
		   dataResourceInstance.userCreated     = session.user

			   if (!dataResourceInstance.hasErrors()) {


				   dataResourceInstance.save flush:true
			   }
			   
			   result.dataResourceInstance = dataResourceInstance
	   }else{
		   //dataResourceInstance = WorkEffort.get(dataResourceInstance.id)
		   if (dataResourceInstance == null) {

			   log.debug ( 'No se pudo crear el  dataResourceInstance ' )

			   result.error ="dataResourceInstance inexistente,No se pudo crear"
			   return fail(result)
		   }
		   dataResourceInstance.state           = 2
	   }


	   log.debug ( 'dataResourceService.save().dataResourceInstance.code: ' + dataResourceInstance.code )
	   if (!dataResourceInstance.validate()) {

		   result.error ="Esfuerzo con Errores - "+ dataResourceInstance.errors
		   log.error ( 'dataResourceInstance con Errores- dataResourceInstance.errors: ' + dataResourceInstance.errors )
		   log.debug ( 'Esfuerzo con Errores. result: ' +  result )
		   return (result)
	   }else{
		   log.debug ('dataResourceservice-save: No tiene errores')

		   if (!dataResourceInstance.save( flush:true)) {
			   log.error ( '!dataResourceInstance.save: ' + dataResourceInstance.errors )
			   result.error ="Fallo el grabado de dataResourceInstance"+ dataResourceInstance.errors

			   return (result)
		   }
	   }

	   log.debug ('dataResourceservice-save: sale por la opcion de resultado exitoso')
	   result.dataResourceInstance = dataResourceInstance

	   // Success.
	   return result

	   }
   
 
   
}
