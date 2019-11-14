package com.profesys.scientiam.services

import com.profesys.scientiam.pm.Deliverable

import java.util.ArrayList;
import java.util.Date;

import com.profesys.scientiam.pm.Project;
import com.profesys.scientiam.pm.ProjectTaxonomy;
import com.profesys.scientiam.pm.work.WorkEffort
import com.profesys.scientiam.pm.Deliverable
import com.profesys.scientiam.pm.DeliverableType
import com.profesys.scientiam.pm.work.WorkEffortEstimationType;
import com.profesys.scientiam.pm.work.WorkEffortPartyAssignment;
import com.profesys.scientiam.pm.work.WorkEffortPurposeType
import com.profesys.scientiam.pm.work.WorkEffortType
import com.profesys.scientiam.workspace.Ws_issue
import com.profesys.scientiam.security.User
import com.profesys.scientiam.configuration.uom.Uom
import com.profesys.scientiam.configuration.Enumeration
import com.profesys.scientiam.configuration.StatusItem
import com.profesys.scientiam.erp.party.Party;

import grails.gorm.transactions.Transactional
import groovy.sql.GroovyRowResult;

import org.grails.web.util.WebUtils

 


import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import groovy.util.logging.Log4j


@Transactional
class WorkEffortService {

   def TransactionService
   
   def list(String ticketParam) {
        def result = [:]
        def fail = { Map m ->
            result.error = [ code: m.code, args: ["WorkEffort"] ]
            return result
        }
		
		def entryCriteria = WorkEffort.createCriteria()
		
		result.workEffortInstanceList  = entryCriteria.list {
			and{
			  eq("ticket",ticketParam)
			}
			    
	   
		  }
        result.workEffortInstanceTotal = result.workEffortInstanceList.size()
 
			log.debug ('result.workEffortInstanceList : ' +result.workEffortInstanceList  )
			log.debug ('result.workEffortInstanceTotal : ' +result.workEffortInstanceTotal  )
 
        // Success.
        return result
    }
 
   
   @Transactional
   def save(WorkEffort workEffortInstance) {
	   
	   log.debug ( '*********************save(WorkEffort workEffortInstance) ')
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["workEffortInstance"] ]
		   return result
	   }
	   log.debug ( ' : ' +workEffortInstance.properties)
	   if (workEffortInstance == null) {
//		   notFound()
		   log.debug ( 'workEffortInstance Inexistente: ' )
		   result.error ="workEffortInstance inexistente"
		  return fail(result)
	   }
	   //Lo marco como modificación porque si entro luego a la creación de workEffort verificaríamos que es nuevo y lo pisa con estado 1
	   //Creo el registro de workEffort
     // def  workEffortInstance 
	   workEffortInstance.userUpdated     = session.user
	  if (!workEffortInstance.id ) {
		  
		   
		  workEffortInstance.id      	     =  TransactionService.serviceID('WORK',session.user.toString() )
		  workEffortInstance.ticket  		 =  TransactionService.serviceTicket('WORK',session.user.toString() )
		  workEffortInstance.code  		 	 =  TransactionService.getCode( )
		  workEffortInstance.state           = 1
		  workEffortInstance.userCreated     = session.user

	//	  wor	kEffortInstance.actualStartDate 			= null
	//	  workEffortInstance.actualCompletionDate	 	= null
		  
//		  workEffortInstance.Enumeration 			scopeEnum= null
////		  WorkEffortType 			workEffortType= null
////		  WorkEffortPurposeType 	workEffortPurposeType= null
////		  WorkEffort     			workEffortParent= null
////		  Project                 project= null
			 workEffortInstance.projectTaxonomy= null
			 //workEffortInstance.ownerParty= null
			//		  
			////		  Datos de Esfuerzos y control de tiempos
			// workEffortInstance.estimatedStartDate      = null
			// workEffortInstance.estimatedCompletionDate = null
//			 workEffortInstance.actualStartDate         = null
//			 workEffortInstance.actualCompletionDate    = null
//			 workEffortInstance.followUpDate            = null
//			 workEffortInstance.dueDate                 = null
			 workEffortInstance.delegatedFollowUpDate   = null 
			 workEffortInstance.delegatedDueDate        = null
			 workEffortInstance.estimationType          = null
			 workEffortInstance.estimatedUnits		    = 0
			 workEffortInstance.actualUnits             = 0  
			 workEffortInstance.totalUnitsAllowed	    = 0
			 workEffortInstance.totalAllowedUnits	    = 0
			 workEffortInstance.totalAllowedMoney	    = 0
			 workEffortInstance.percentCompleted	    = 0
 
//		  StatusItem 				currentStatus= null
//		  Uom            			moneyUom		= null	  
//		  Integer        			sequentialOrder= 0
//		  String         			orderTag= null
//		  String         			tagCloud= null
		  
	  }else{
	  	  //workEffortInstance = WorkEffort.get(workEffortInstance.id)
			if (workEffortInstance == null) {
				 
				log.debug ( 'No se pudo crear el  workEffortInstance ' )
						 
				  result.error ="workEffortInstance inexistente,No se pudo crear"
			      return fail(result)
			    }
			workEffortInstance.state           = 2
	  }
	 
	   
	   log.debug ( 'workeffortService.save().workEffortInstance.code: ' + workEffortInstance.code )
	   if (!workEffortInstance.validate()) {
		   
		   result.error ="Esfuerzo con Errores - "+ workEffortInstance.errors
		   result.workEffortInstance = workEffortInstance
		   log.error ( 'Esfuerzo con Errores- workEffortInstance.errors: ' + workEffortInstance.errors )
		   log.error ( 'Esfuerzo con Errores. result: ' +  result )

		   return ( result)
	   }else{
		   log.debug ('workEffortservice-save: No tiene errores')
		   	  
		   if (!workEffortInstance.save( flush:true)) {
			   log.error ( '!workEffortInstance.save: ' + workEffortInstance.errors )
			   result.error ="Fallo el grabado de workEffortInstance"+ workEffortInstance.errors
			  
			   return (result)
		   }
	   }

   log.debug ('workEffortservice-save: sale por la opcion de resultado exitoso')
   result.workEffortInstance = workEffortInstance
   
   // Success.
   return result
   }
   
  /*
   * Procedimiento para obtener todos los workEffort necesarios para 
   * asociarle los participantes
   */
   
   @Transactional
   def  ArrayList<GroovyRowResult> getAllWorkEffort() {
	   //Devuelvo todos los proyectos en los que intervengo
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
	   /*
		* Esto Funciona perfecto, pero no logro sacar los distincts projects
		*/    log.debug ('getAllProjects-ingreso ')
			   def proyectos = WorkEffortType.get('PROJECT')
		 
			   def userPartyRoles = Party.findAllByUser( session.user )
				   
			 //  def results = WorkEffortPartyAssignment.where {
			//	  workEffort.workEffortType == proyectos &&  state > 0   && party in userPartyRoles
			 //  }.list()
			   
				  def resultsWEPA = WorkEffortPartyAssignment.where {
				   workEffort.workEffortType == proyectos &&  state > 0   && party in userPartyRoles
			   }.list()
			   
			  // def results = WorkEffort.where {
				  // id in resultsWEPA.workEffort.id
				// }.list()
				def results = WorkEffort.where {
				   workEffortType == proyectos &&  state > 0
			   }.list( )
			   
		   
	   return  results
	   
   }
   
   @Transactional
   def  ArrayList<GroovyRowResult> getAllMyActionableWorkEffort() {
	   //Devuelvo todos los proyectos en los que intervengo
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
	   /*
		* Esto Funciona perfecto, pero no logro sacar los distincts projects
		*/   
	   		 log.debug ('getAllActionableWorkEffort ')
			//	Char actionable='Y'
					char a='Y'
			   def actionableWorkEffortTypes = WorkEffortType.where{isActionable==a}
			     //
			   log.debug ('actionableWorkEffortTypes: ' +actionableWorkEffortTypes)
			   def userPartyRoles = Party.findAllByUser( session.user )
			   //	   
			   
				  def resultsWEPA = WorkEffortPartyAssignment.where {
				   // workEffort.workEffortType in actionableWorkEffortTypes &&
				     state > 0   && party in userPartyRoles
			   }.list()
			 
			  def   workEffortStatusItemList = [ StatusItem.get('TASK_IN_PROGRESS'),  StatusItem.get('TASK_PLANNING')]
				 
			 
 			   def results = WorkEffort.where {
	   				   id in resultsWEPA.workEffort.id   && 
						  state > 0  &&
						 //   workEffortType == actionableWorkEffortTypes    &&
						   currentStatus in ( workEffortStatusItemList  )				
	   				}.list()
						
			   
		   
	   return  results
	   
   }
   

   def  ArrayList<GroovyRowResult> getWorkEffortTypeActions() {
	   //Devuelvo todos los proyectos en los que intervengo
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
	   /*
		* Esto Funciona perfecto, pero no logro sacar los distincts projects
		*/   		   
		 
	   def rootAction = WorkEffortType.getAll('ACTION','TIMELINE')
		log.error ('workEffortService.getWorkEffortTypeActions.rootAction: ' + rootAction )
		 
				def results = WorkEffortType.where {
				   workEffortTypeRoot in ( rootAction ) &&
						   state > 0
			   }.list( ).sort { it.description }
			   
		  log.error ( 'workEffortService.getWorkEffortTypeActions.results: ' +results)
		   
	   return  results
	   
   }
   
   
   @Transactional
   def saveGoalMilestone(WorkEffort workEffortInstance) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
	   
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["workEffortInstance"] ]
		   return result
	   }
	  
	  
	   if (workEffortInstance == null) {
					 return fail(code:" workEffort con Valores Nulos")
				  }
	   log.debug ( ' ***************PRE-WorkEffortService.save'  )
	   log.debug ( ' ***************workEffortInstance.PROPERTIES: ' + workEffortInstance.properties  )
	   
	   /*
		* Cago valores especificos del workEffort
		*/
	  // workEffortInstance.workEffortType = WorkEffortType.read("PROJECT")
	   
	   workEffortInstance.scopeEnum = Enumeration.read('WES_PUBLIC')
	   workEffortInstance.currentStatus= StatusItem.get ('WEGS_CREATED')
	   result = this.save  (  workEffortInstance )
	  
	   if (!result.workEffortInstance) {
		   log.debug ( 'FALLO LA VALIDACION!  -->!workEffortInstance.save.result: ' + result )
		   throw new RuntimeException(result.error.toString())
//		   return  ( result )
		 } 
 
 
	   
	   // Success.
	   return result
   }

	@Transactional
	def saveDeliverable(Deliverable deliverableInstance) {
		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session


		def result = [:]
		def fail = { Map m ->
			result.error = [ code: m.code, args: ["deliverableInstance"] ]
			return result
		}


		if (deliverableInstance == null) {
			return fail(code:" deliverable con Valores Nulos")
		}
		log.debug ( ' ***************PRE-DeliverableService.save'  )
		log.error ( ' ***************deliverableInstance.PROPERTIES: ' + deliverableInstance.properties  )
		deliverableInstance.clearErrors()
		/*
         * Cago valores especificos del deliverable
         */
		// deliverableInstance.deliverableType = DeliverableType.read("PROJECT")
        // result = this.save  (  deliverableInstance )
		deliverableInstance.userUpdated     = session.user
		log.error ("****deliverableInstance.id : " +deliverableInstance.id)
		if (!deliverableInstance.id ) {
			log.error ("entro por if !deliverableInstance.id ")

			deliverableInstance.id      	     =  TransactionService.serviceID('DELI',session.user.toString() )
			deliverableInstance.ticket  		 =  TransactionService.serviceTicket('DELI',session.user.toString() )
			deliverableInstance.code  		 	 =  TransactionService.getCode( )
			deliverableInstance.state           = 1
			deliverableInstance.userCreated     = session.user

		}else{
			//workEffortInstance = WorkEffort.get(workEffortInstance.id)
			log.error ("entro por else !deliverableInstance.id ")

			if (deliverableInstance == null) {

				log.debug ( 'No se pudo crear el  deliverableInstance ' )

				result.error ="deliverable Instance inexistente,No se pudo crear"
				return fail(result)
			}
			deliverableInstance.state           = 2
		}


		log.error  ( 'workeffortService.save().workEffortInstance.code: ' + deliverableInstance.code )
		if (!deliverableInstance.validate()) {

			result.error ="Esfuerzo con Errores - "+ deliverableInstance.errors
			log.error ( 'Esfuerzo con Errores- workEffortInstance.errors: ' + deliverableInstance.errors )
			log.debug ( 'Esfuerzo con Errores. result: ' +  result )
			return (result)
		}else{
			log.error ('deliverableInstance-save: No tiene errores')

			if (!deliverableInstance.save( flush:true)) {
				log.error ( '!workEffortInstance.save: ' + deliverableInstance.errors )
				result.error ="Fallo el grabado de deliverableInstance"+ deliverableInstance.errors

				return result
			}
		}
		log.error ('Salgo correctamente')

		result.deliverableInstance = deliverableInstance
		// Success.
		return result
	}
	
	
}
