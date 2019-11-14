package com.profesys.scientiam.services

import com.profesys.scientiam.pm.userStory.UserStory
import com.profesys.scientiam.pm.work.WorkEffort
import com.profesys.scientiam.pm.work.WorkEffortPurposeType
import com.profesys.scientiam.pm.work.WorkEffortType
import com.profesys.scientiam.workspace.Ws_issue

import com.profesys.scientiam.security.User
import com.profesys.scientiam.configuration.uom.Uom
import com.profesys.scientiam.configuration.Enumeration
import com.profesys.scientiam.configuration.StatusItem
import grails.gorm.transactions.Transactional

import org.grails.web.util.WebUtils
 
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import groovy.util.logging.Log4j



@Transactional
class StoryService {

   def TransactionService
   
   def list(String ticketParam) {
        def result = [:]
        def fail = { Map m ->
            result.error = [ code: m.code, args: ["Story"] ]
            return result
        }
		
		def entryCriteria = Story.createCriteria()
		
		result.storyInstanceList  = entryCriteria.list {
			and{
			  eq("ticket",ticketParam)
			}
			    
	   
		  }

        result.storyInstanceTotal = result.storyInstanceList.size()
 
		log.debug ('result.storyInstanceList : ' + result.storyInstanceList.toString()  )
		log.debug ('result.storyInstanceTotal : ' + result.storyInstanceTotal.toString()  )

        // Success.
        return result
    }
 
   
   @Transactional
   def save(UserStory storyInstance) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["storyInstance"] ]
		   return result
	   }
	   log.debug ( 'storyInstance.properties: ' +storyInstance.properties)
	   if (storyInstance == null) {
//		   notFound()
		   log.debug ( 'Historia Inexistente: ' )
		  return fail(code:"Historia inexistente")
	   }
	   //Lo marco como modificación porque si entro luego a la creación de workEffort verificaríamos que es nuevo y lo pisa con estado 1
	   storyInstance.state           = 2
	   //Creo el registro de workEffort
	   
	   if (!storyInstance.workEffort){
		  
		   def workEffortInstance = new WorkEffort()
//		   workEffortInstance.workEffortName = 'Generación por Historia de usuario'
		   workEffortInstance.workEffortPurposeType  = WorkEffortPurposeType.read('WEPT_ANALYSIS_DESIGN')
		   workEffortInstance.workEffortType = WorkEffortType.read("USER_STORY")
		   workEffortInstance.id  =  TransactionService.serviceID('USERSTORY',session.user.toString() )
		   workEffortInstance.userCreated     = session.user // User.read ( session.user.id )
		   workEffortInstance.code = storyInstance.code
		   workEffortInstance.actualCompletionDate= null
		//   workEffortInstance.actualMilliSeconds= 0
//		   workEffortInstance.actualSetupMillis= 0
		   workEffortInstance.actualStartDate = null
		   
		   workEffortInstance.description = storyInstance.description
		   workEffortInstance.scopeEnum = Enumeration.read('WES_PUBLIC')
		   workEffortInstance.estimatedCompletionDate= null
		//   workEffortInstance.estimatedMilliSeconds= 0
//		   workEffortInstance.estimatedSetupMillis= 0
		   workEffortInstance.estimatedStartDate=null
		//   workEffortInstance.percentComplete = 0
		   workEffortInstance.currentStatus= StatusItem.get ('WEPR_PLANNING')
		//   workEffortInstance.totalMilliSecondsAllowed= 0
		   workEffortInstance.moneyUom =  Uom.read ('ARS')
		  
		   workEffortInstance.state           = 1
		   workEffortInstance.userUpdated     = session.user
		   workEffortInstance.ticket  = workEffortInstance.id
		   if (!workEffortInstance.save( flush:true)) {
			   log.debug ( '!workEffortInstance.save: ' + workEffortInstance.errors )
			   return fail(code:"Fallo el grabado de workEffort:"+ workEffortInstance.errors)
		   	}
		   
		   //Si grabé el workEffort es porque es nuevo...y tengo que actualizar la instancia de storyInstance
		   log.debug ('workEffortInstance.id: ' + workEffortInstance.id)
		   storyInstance.workEffort      =  WorkEffort.read ( workEffortInstance.id )
		   storyInstance.id              = workEffortInstance.id
		   log.debug (' storyInstance.workEffort.id: ' +  storyInstance.workEffort.id)
		   storyInstance.userCreated     =  session.user
		   storyInstance.state           = 1
		  }
	   
	  
	   
	   //Primero tengo que hacer el borrado logico del registro de origen 
	   //si la userstory viene desde temas
	   def idOrigen = storyInstance.idOrigen 
	   log.debug ("storyInstance.idOrigen :" + storyInstance.idOrigen  )
	   def issueInstance = Ws_issue.get(idOrigen )
	    
	  if ( issueInstance ){
		  log.debug ("issueInstance:" + issueInstance.properties )
		  issueInstance.state = 0 
		  if (!issueInstance.validate()) {
			  log.debug ( 'issueInstance con Errores: ' + issueInstance.errors )
			  return fail(code:"issueInstance con Errores"+ issueInstance.errors)
		   
		  }else{
			  log.debug ('issueInstance-save: No tiene errores')
					
			  if (!issueInstance.save( flush:true)) {
				  log.debug ( '!issueInstance.save: ' + issueInstance.errors )
				  return fail(code:"Fallo el grabado de issueInstance"+ issueInstance.errors)
			  }
		  } 
	  
	   }
 
//	   log.debug ('workEffortInstance.ident():' + workEffortInstance.ident())
	   /*
		* Actualizo los campos de auditoría
		*/
	 log.debug("campos auditoria")
	   storyInstance.userUpdated     = User.read (  session.user.id )
					 
	   
	   if (!storyInstance.validate()) {
		   log.debug ( 'Historia con Errores: ' + storyInstance.errors )
		   return fail(code:"Historia con Errores"+ storyInstance.errors)
		
	   }else{
		   log.debug ('storyservice-save: No tiene errores')
		   	  
		   if (!storyInstance.save( flush:true)) {
			   log.debug ( '!storyInstance.save: ' + storyInstance.errors )
			   return fail(code:"Fallo el grabado de storyInstance"+ storyInstance.errors)
		   }
				   
			      
		  	
	   }

   log.debug ('storyservice-save: sale por la opcion de resultado exitoso')
   result.storyInstance = storyInstance
   
   // Success.
   return result
   }
   
   @Transactional
   def save(Story storyInstance ) {
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["Story"] ]
		   return result
	   }
	   log.debug ( 'storyInstance.properties: ' +storyInstance.properties)
	   if (storyInstance == null) {
//		   notFound()
		  return fail(code:"Nota inexistente")
	   }
	   if (!storyInstance.hasErrors()) {
		   

		   storyInstance.save flush:true
	   }
	   
	   result.storyInstance = storyInstance
	   
	   // Success.
	   return result
	   }
   
}
