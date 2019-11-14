package com.profesys.scientiam.services


import com.profesys.scientiam.configuration.data.DataSource
import com.profesys.scientiam.erp.party.Party
import com.profesys.scientiam.erp.party.PartyType
import com.profesys.scientiam.erp.party.Party
import com.profesys.scientiam.erp.party.Person
import com.profesys.scientiam.pm.Project
import com.profesys.scientiam.pm.DeliverableType
import com.profesys.scientiam.pm.Deliverable
import com.profesys.scientiam.pm.work.Task;
import com.profesys.scientiam.pm.work.WorkEffort
import com.profesys.scientiam.pm.work.WorkEffortType
import com.profesys.scientiam.pm.work.WorkEffortPartyAssignment
import com.profesys.scientiam.security.User
import com.profesys.scientiam.workspace.Ws_issue
import com.profesys.scientiam.configuration.StatusItem
import com.profesys.scientiam.configuration.uom.Uom
import groovy.util.logging.Log4j
import org.grails.web.util.WebUtils

import grails.gorm.transactions.Transactional

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import com.profesys.scientiam.configuration.Enumeration

import groovy.sql.Sql
import groovy.sql.GroovyRowResult

import java.util.ArrayList


@Transactional
class ProjectService {
	
	def TransactionService
    def WorkEffortService  
	
   @Transactional
   def saveProject(WorkEffort workEffortInstance, Project projectInstance) {
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
 
//	   log.debug ( 'projectInstance*: '+ projectInstance.properties)
//	   log.debug ( 'session: '+ session)
	   
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["projectInstance"] ]
		   return result
	   }
	   if (workEffortInstance == null) {
//		   notFound()
		  return fail(code:"Proyecto con Valores Nulos")
	   }
	   
	   log.debug ( 'workEffortInstance.id : ' + workEffortInstance.id )
   	   log.debug ( ' ***************PRE-WorkEffortService.save'  )
	   result = WorkEffortService.save  (  workEffortInstance )   
		  
	   if (!result.workEffortInstance) {
		   log.debug ( 'FALLO LA VALIDACION!  -->!workEffortInstance.save.result: ' + result )
		   throw new RuntimeException(result)
//		   return  ( result )
		 }else{
			 log	.debug ('result.workEffortInstance.id: ' + result.workEffortInstance.id )
			 projectInstance.userUpdated       =   session.user
			 projectInstance.state     		   =  2
		     if (!projectInstance.id){
				 //Es un registro nuevo y tengo que insertar los valores de id y ticket
				 
		    	 projectInstance.userCreated       = session.user
		    	 projectInstance.id      		   = result.workEffortInstance.id
				 projectInstance.workEffort 	   = result.workEffortInstance 
				 projectInstance.state             = 1
				 
			 }
		 }
	 
	   
	     log.debug ( 'PRE--projectInstance.hasErrors  ')
		 if ( projectInstance.hasErrors()) {
			 //		   log.debug ( 'ProjectService.projectInstance.errors: ' +  projectInstance.errors )
						 result.error = projectInstance.errors
						 
						 log.debug ( 'ProjectService.projectInstance.errors: ' +  projectInstance.errors )
						 log.debug ( 'ProjectService. result.error : ' +  result.error )
						 throw new RuntimeException ( result.error.toString() ) ;
					}
		 log.debug ( 'PRE--projectInstance.validate  ')
	   if (!projectInstance.validate()) {
//		   log.debug ( 'ProjectService.projectInstance.errors: ' +  projectInstance.errors )
		    result.error = projectInstance.errors
			
			log.debug ( 'ProjectService.projectInstance.errors: ' +  projectInstance.errors )
			log.debug ( 'ProjectService. result.error : ' +  result.error )
			throw new RuntimeException ( result.error.toString() ) ;
	   }
	  
	   log.debug ( 'PRE--save ') 
	   log.debug ( 'PartyService.projectInstance.save.projectInstance: ' + projectInstance.properties)
	  //projectInstance.save( )
	   projectInstance.save(flush: true, failOnError: true);
		if (projectInstance.hasErrors() ) {
			log.debug ( 'PartyService.projectInstance.save.ERROR: ')
			projectInstance.errors.each {
				 log.error ( 'PartyService.projectInstance.save.errors: ' +  it )
			
			}
			result.error = projectInstance.errors
			throw new RuntimeException(result)
		}
		
		 
	   // Success.
	   return result
	   }

   
   
   @Transactional
   def save(Project projectInstance) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   log.debug ( '*****-INGRESO A saveProject - *******: ' )
	   
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["projectInstance"] ]
		   return result
	   }
	   if (projectInstance == null) {
		   //		   notFound()
					 return fail(code:" Proyecto con Valores Nulos")
				  }
		   //
	   def workEffortInstance = projectInstance.workEffort
//	   if (workEffortInstance == null) {
//	   		workEffortInstance = new WorkEffort()
//	   }
	   if (workEffortInstance == null) {
		   //		   notFound()
					 return fail(code:" workEffort con Valores Nulos")
				  }
			   // log.debug ('projectService.save.workEffortInstance.properties: ' +workEffortInstance.properties)
	   log.debug ( ' ***************PRE-WorkEffortService.save'  )
	   log.debug ( ' ***************workEffortInstance.PROPERTIES: ' + workEffortInstance.properties  )
	   
	   /*
		* Cago valores especificos del workEffort
		*/
	  // workEffortInstance.workEffortType = WorkEffortType.read("PROJECT")
	   
	   result = WorkEffortService.save  (  workEffortInstance )
	   log.error ('projectService.saveproject().WorkEffortService.save.result.properties = '+result.workEffortInstance.properties)
	   log.debug ('projectService.saveproject().WorkEffortService.save.result = '+ result)
	   log.debug ('projectService.saveproject().WorkEffortService.save.result.error ='+result.error)
	   
	   if (!result.workEffortInstance) {
		   log.debug ( 'FALLO LA VALIDACION!  -->!workEffortInstance.save.result: ' + result )
		   throw new RuntimeException(result.error.toString())
//		   return  ( result )
		 }else{
			 log.debug ('result.workEffortInstance.id: ' + result.workEffortInstance.id )
			 projectInstance.userUpdated          =   session.user
			 projectInstance.state     		   =  2
			 log.debug ('result-pre. projectInstance.properties  : ' +  projectInstance.properties   )
			 if (!projectInstance.id){
				 //Es un registro nuevo y tengo que insertar los valores de id y ticket
				 
				 projectInstance.userCreated       = session.user
				 projectInstance.id      		   = result.workEffortInstance.id
				 projectInstance.workEffort 	   = result.workEffortInstance
				 projectInstance.state             = 1
				  
				 
				 log.debug ('result. projectInstance.id          : ' +  projectInstance.id           )
				 log.debug ('result. projectInstance.properties  : ' +  projectInstance.properties   )
				 log.debug ('result. projectInstance.workEffort  : ' +  projectInstance.workEffort.properties  )
			 }
		 }
	 
	   
		 log.debug ( 'PRE--projectInstance.hasErrors: ')
	   
	   if (!projectInstance.validate()) {
//		   log.debug ( 'ProjectService.projectInstance.errors: ' +  projectInstance.errors )
			result.error = projectInstance.errors
			
			log.debug ( 'ProjectService.projectInstance.errors: ' +  projectInstance.errors )
			log.debug ( 'ProjectService. result.error : ' +  result.error )
			throw new RuntimeException ( result.error.toString() ) ;
	   }
	  
	   log.debug ( 'PRE--save ')
	 
	   
		if (!projectInstance.save( flush:true ) ) {
			log.error ( '!projectInstance.save: ' + projectInstance.errors )
			result.error ="Fallo el grabado de Proyectos: " +   projectInstance.errors
			
			//return (result)
			throw new RuntimeException(result)
		}
		
	   result.projectInstance = projectInstance
	   log.debug ( 'projectService.save(): - SUCCESS')
	   // Success.
	   return result
   }
   
   
   
 
   def  ArrayList<GroovyRowResult> getMyProjects() {
	   //Devuelvo todos los proyectos en los que intervengo 
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
 
	   			
	   /*
	    * Esto Funciona perfecto, pero no logro sacar los distincts projects
	    */

	   		def tiposProyecto  = WorkEffortType.findAllByWorkEffortTypeRoot(WorkEffortType.get('STRATEGIC_PLANNING'))


	   		def statusProyecto = StatusItem.where {
				id in ['WEPR_IN_PROGRESS', 'WEPR_ON_HOLD', 'WEPR_PLANNING']
			}.list()
	   		log.error ('statusProyecto: ' + statusProyecto)
			   def userPartyRoles = Party.findAllByUser( session.user )
//	   		log.error ('userPartyRoles: ' + userPartyRoles)
				   
			   //  def results = WorkEffortPartyAssignment.where {
			   //	  workEffort.workEffortType == proyectos &&  state > 0   && party in userPartyRoles
			   //  }.list()
			   
			      def resultsWEPA = WorkEffortPartyAssignment.where {
			 	  workEffort.workEffortType in tiposProyecto  &&   party in userPartyRoles
			   }.list()

//			    log.error ('resultsWEPA : ' + resultsWEPA  )
//
//				log.error ('resultsWEPA.workEffort.id: ' + resultsWEPA.workEffort.id )
//
//	   			log.error ('resultsWEPA.description: ' + resultsWEPA.description )

//	   			def resultsWE = WorkEffort.createCriteria()
//	 		 	def results = resultsWE.where {
//			   		id in resultsWEPA.workEffort.id && state > 0
//	  			 }
			   def results = WorkEffort.where {
				   id in resultsWEPA.workEffort.id &&
						   state > 0 && workEffortType in tiposProyecto &&
				   			currentStatus in statusProyecto

			   }.list(sort:"description" )
			   
//				 	log.error ('results: ' + results)
				
	   return  results
	   
   }
   
   @Transactional
   def  ArrayList<GroovyRowResult> getAllProjects() {
	   //Devuelvo todos los proyectos en los que intervengo
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
				   
	   /*
		* Esto Funciona perfecto, pero no logro sacar los distincts projects
		*/    log.debug ('getAllProjects-ingreso ')
	           def tipoProyectos = WorkEffortType.findAllByWorkEffortTypeRoot(WorkEffortType.get('STRATEGIC_PLANNING'))
			   //def proyectos = WorkEffortType.get()
		 		log.error ('tipoProyectos: ' + tipoProyectos )
			   def userPartyRoles = Party.findAllByUser( session.user )
				   
			 //  def results = WorkEffortPartyAssignment.where {
			//	  workEffort.workEffortType == proyectos &&  state > 0   && party in userPartyRoles
			 //  }.list()
			   
//				  def resultsWEPA = WorkEffortPartyAssignment.where {
//				   workEffort.workEffortType in tipoProyectos &&  state > 0   && party in userPartyRoles
//			   }.list()
			   
			  // def results = WorkEffort.where {
				  // id in resultsWEPA.workEffort.id
				// }.list()
			    def results = WorkEffort.where {
					workEffortType in tipoProyectos &&  state > 0
			   }.list( )

			   

	   return  results
	   
   }
   
   
   @Transactional
   def  ArrayList<GroovyRowResult> getWorkEffort(WorkEffort workEffortInstance) {
	   //Devuelvo todos los proyectos en los que intervengo
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
//	   def projectList = Project.findAllByStateGreaterThan(0)
//
 
				   
	   /*
		* Esto Funciona perfecto, pero no logro sacar los distincts projects
		*/    
//	   log.debug ('getAllProjects-ingreso ')
//			   def proyectos = WorkEffortType.get('PROJECT')
//		 
//			   def userPartyRoles = Party.findAllByUser( session.user )
//				   
//			 //  def results = WorkEffortPartyAssignment.where {
//			//	  workEffort.workEffortType == proyectos &&  state > 0   && party in userPartyRoles
//			 //  }.list()
//			   
//				  def resultsWEPA = WorkEffortPartyAssignment.where {
//				   workEffort.workEffortType == proyectos &&  state > 0   && party in userPartyRoles
//			   }.list()
//			   
//			  // def results = WorkEffort.where {
//				  // id in resultsWEPA.workEffort.id
//				// }.list()
//				def results = WorkEffort.where {
//				   workEffortType == proyectos &&  state > 0
//			   }.list( )
			   
		  def  results =  workEffortInstance
	   return  results
	   
   }
   
   
   def listGoals(WorkEffort workEffortInstance) {
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["workEffortInstance"] ]
		   return result
	   }
	   def goalType = WorkEffortType.get('GOAL')
	   def entryCriteria = WorkEffort.createCriteria()
	   
	   result.goalsInstanceList  = entryCriteria.list {
		   and{
			eq("workEffortParent",workEffortInstance)
			eq("workEffortType",goalType)
			gt("state",0)
		   }
			   
	  
		 }
	   result.goalsInstanceTotal = result.goalsInstanceList.size()

		   log.debug ('result.goalsInstanceList : ' + result.goalsInstanceList  )
		   log.debug ('result.goalsInstanceTotal : ' +result.goalsInstanceTotal  )

	   // Success.
	   return result
   }

	def listDeliverables(WorkEffort workEffortInstance) {
		def result = [:]
		def fail = { Map m ->
			result.error = [ code: m.code, args: ["workEffortInstance"] ]
			return result
		}
		log.debug ('workEffortInstance:  ' + workEffortInstance )

		 result.deliverableTypeInstanceList  = DeliverableType.list()

		def entryCriteria = Deliverable.createCriteria()
		result.deliverableInstanceList  = entryCriteria.list {
			and{
				eq("workEffort",workEffortInstance)

				gt("state",0)
			}


		}

		log.error ('result.deliverableInstanceList :  '    + result.deliverableInstanceList  )
		log.error ('result.deliverableTypeInstanceList : ' + result.deliverableTypeInstanceList  )

		// Success.
		return result
	}

   def listMilestones(WorkEffort workEffortInstance) {
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["workEffortInstance"] ]
		   return result
	   }
	   log.debug ('workEffortInstance:  ' + workEffortInstance )
	   def milestoneType = WorkEffortType.get('MILESTONE')
	   
	   def entryCriteria = WorkEffort.createCriteria()
	   result.milestonesInstanceList  = entryCriteria.list {
		   and{
			 eq("workEffortParent",workEffortInstance)
			 eq("workEffortType",milestoneType)
	 		 gt("state",0)
			}
			   
	  
		 }
	   result.milestonesInstanceTotal = result.milestonesInstanceList.size()

		   log.debug ('result.milestonesInstanceList :  ' + result.milestonesInstanceList  )
		   log.debug ('result.milestonesInstanceTotal : ' + result.milestonesInstanceTotal  )

	   // Success.
	   return result
   }
   
   def  ArrayList<GroovyRowResult> getProjectIndex(params) {
	   //Devuelvo todos los proyectos en los que intervengo
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
	   log.debug ('service.params: ' + params )
	   
	   def projecttype = WorkEffortType.get('PROJECT')
 
	   def userPartyRoles = Party.findAllByUser( session.user )
	   //
		  def resultsWEPA = WorkEffortPartyAssignment.where {
		   workEffort.workEffortType == projecttype &&  state > 0 &&  workEffort.state > 0  && party in userPartyRoles
	   }.list()
	   
	  def projectStatusParamsToArray = params.projectStatus
	  
	  log.debug ( 'projectService.params: ' +  params)
	  log.debug  ('projectStatusParamsToArray: ' + projectStatusParamsToArray)
			  
			  StatusItem projectStatusList = []
			  
			  def projectStatusItemList
			   if (! projectStatusParamsToArray  ){
				   log.debug  ('projectStatusParamsToArray NULO : ')
				  // projectStatusItemList = StatusItem.findAllByStatusType (StatusType.get('TASK_STATUS' ))
				   projectStatusItemList = [ StatusItem.get('WEPR_IN_PROGRESS'),  StatusItem.get('WEPR_PLANNING')]
					}  else  {
				   log.debug  ('projectStatusParamsToArray CON VALOR : ')
					//   projectStatusItemList = StatusItem.where { id in  projectStatusParamsToArray}
						 projectStatusItemList = StatusItem.getAll ( projectStatusParamsToArray)
					
				   
				   }
			   log.debug('projectStatusItemList.typeOf() :' + projectStatusItemList )
				log.debug ( 'Encontré los statusItem: ' + projectStatusItemList )
				   
			   log.debug ( 'POST StatusItem.where ')
//			   log.debug ( 'projectStatusParamsToArray: ' + projectStatusParamsToArray)
//			   log.debug ( 'projectStatusList: '      + projectStatusList )
//			   log.debug ( 'projectStatusItemList: ' + projectStatusItemList)

				def results = Project.where {
					workEffort {
						state > 0

						// &&	currentStatus in (projectStatusItemList)
								}
					   }.list()
						
				//
//					   def idsArray
//					   if  (!resultsWE){
//						   idsArray = '';
//					   }else{
//
//					   	idsArray = resultsWE.id
//					   }
//
//			  log.debug ('resultsWE: ' + resultsWE)
//			   def results = Project.where {
//				   id in idsArray
//				}.list()
									
				log.debug ('results: ' + results)
				
	   return  results
	   
   }

	def listIterations(params) {
		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session

		def result = [:]

		log.error ( 'params: ' +  params)
		def projectSelection
		if ( params.projectSelection){
			projectSelection =  params.projectSelection
			log.error ('projectSelection : ' +projectSelection)
		}


		def projectInstance= Project.get ( projectSelection )

		// def projectInstance = WorkEffort.get (params.project)
		def workEffortType = WorkEffortType.get('ITERATION')
		def entryCriteria = WorkEffort.createCriteria()

		result.iterationInstanceList  = entryCriteria.list {
			and{
				eq("project",projectInstance )
				eq("workEffortTypeRoot",workEffortType)
				gt("state",0)
			}


		}
		result.iterationInstanceTotal = result.iterationInstanceList.size()

		log.debug ('result.iterationInstanceList : ' + result.iterationInstanceList  )
		log.debug ('result.iterationInstanceTotal : ' +result.iterationInstanceTotal  )

		// Success.
		return result
	}
}
