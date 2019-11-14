package com.profesys.scientiam.services

import com.profesys.scientiam.erp.party.RoleType
import com.profesys.scientiam.pm.ProjectType
import groovy.util.logging.Slf4j

import java.text.SimpleDateFormat
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.profesys.scientiam.configuration.data.DataSource
import com.profesys.scientiam.erp.party.Party
import com.profesys.scientiam.erp.party.PartyType
import com.profesys.scientiam.erp.party.Party
import com.profesys.scientiam.erp.party.Person
import com.profesys.scientiam.pm.userStory.UserStory;
import com.profesys.scientiam.pm.work.Task
import com.profesys.scientiam.pm.work.TaskExecutionType
import com.profesys.scientiam.pm.work.WorkEffort
import com.profesys.scientiam.pm.work.WorkEffortAssoc
import com.profesys.scientiam.pm.Project
import com.profesys.scientiam.pm.work.WorkEffortAssocType;
import com.profesys.scientiam.pm.work.WorkEffortPartyAssignment;
import com.profesys.scientiam.pm.work.WorkEffortType
import com.profesys.scientiam.pm.work.WorkEffortPurposeType
import com.profesys.scientiam.security.User
import com.profesys.scientiam.workspace.Ws_issue
import com.profesys.scientiam.configuration.StatusItem
import com.profesys.scientiam.configuration.StatusType
import com.profesys.scientiam.configuration.uom.Uom

import org.grails.web.util.WebUtils

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.sql.GroovyRowResult;
// import org.hibernate.criterion.CriteriaSpecification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import com.profesys.scientiam.configuration.Enumeration
import groovy.util.logging.Log4j


@Transactional(readOnly = false)
class TaskService {

	def TransactionService
    def WorkEffortService


	def listByUserStory(WorkEffort workEffort) {
		def result = [:]
		def fail = { Map m ->
			result.error = [ code: m.code, args: ["Task"] ]
			return result
		}
		
//	 
		
		def entryCriteria = WorkEffortAssoc.createCriteria()
		
		result.workEffortAssocInstanceList  = entryCriteria.list {
						 
						and{
						  eq("workEffortIdFrom",workEffort)
						  gt("state",0)
						}
			
			
					  }
		
	   // params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
		//result.noteInstanceList  = Book.list(params)
		result.workEffortAssocInstanceTotal = result.workEffortAssocInstanceList.size()
		
		
			log.debug ('workEffort:' +workEffort.properties)
			log.debug ('workEffort.id:' +workEffort.id)
			log.debug ('result.taskInstanceList : ' + result.workEffortAssocInstanceList  )
			log.debug ('result.taskInstanceTotal : ' +result.workEffortAssocInstanceTotal  )
//        if(!result.noteInstanceList || !result.noteInstanceTotal)
//            return fail(code:"default.list.failure")
 
		// Success.
		return result
	}
	
	
	
   @Transactional
   def saveTask(WorkEffort workEffortInstance, Task taskInstance) {
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   log.debug ( '*****-INGRESO A saveTask - *******: ' )
	   
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["taskInstance"] ]
		   return result
	   }
	   if (workEffortInstance == null) {
//		   notFound()
		  return fail(code:"Esfuerzo de Tarea con Valores Nulos")
	   }
	   if (taskInstance == null) {
		   //		   notFound()
					 return fail(code:" Tarea con Valores Nulos")
				  }
		   //
	   log.debug ( ' ***************PRE-WorkEffortService.save'  )
	   workEffortInstance.workEffortType = WorkEffortType.read("TASK")

	   
	   
	    workEffortInstance.workEffortPurposeType  = WorkEffortPurposeType.read('WEPT_ANALYSIS_DESIGN')
//	   workEffortInstance.code 			 = taskInstance.code
	   workEffortInstance.actualCompletionDate= null
	   workEffortInstance.actualStartDate = null
	   workEffortInstance.scopeEnum = Enumeration.read('WES_PUBLIC')
	   workEffortInstance.estimatedCompletionDate= null
	   workEffortInstance.estimatedStartDate=null
	   workEffortInstance.percentCompleted = 0
	   workEffortInstance.currentStatus= StatusItem.get ('TASK_PLANNING')
	   workEffortInstance.moneyUom =  Uom.read ('ARS')
	  
 
	   
	   result = WorkEffortService.save  (  workEffortInstance )
	   log.debug ('taskService.savetask().WorkEffortService.save.properties='+result.properties)
	   log.debug ('taskService.savetask().WorkEffortService.save.result='+result)
	   log.debug ('taskService.savetask().WorkEffortService.save.result='+result.error)
	   
	   if (!result.workEffortInstance) {
		   log.debug ( 'FALLO LA VALIDACION!  -->!workEffortInstance.save.result: ' + result )
		   throw new RuntimeException(result.error.toString())
//		   return  ( result )
		 }else{
			 log.debug ('result.workEffortInstance.id: ' + result.workEffortInstance.id )
			 taskInstance.userUpdated          =   session.user
			 taskInstance.state     		   =  2
			 log.debug ('result-pre. taskInstance.properties  : ' +  taskInstance.properties   )
		     if (!taskInstance.id){
				 //Es un registro nuevo y tengo que insertar los valores de id y ticket

		    	 taskInstance.userCreated       = session.user
		    	 taskInstance.id      		   = result.workEffortInstance.id
				 taskInstance.workEffort 	   = result.workEffortInstance
				 taskInstance.state             = 1
				 //taskInstance.status 			= null
				 
				 log.debug ('result. taskInstance.id          : ' +  taskInstance.id           )
				 log.debug ('result. taskInstance.properties  : ' +  taskInstance.properties   )
				 log.debug ('result. taskInstance.workEffort  : ' +  taskInstance.workEffort.properties  )




			 }
		 }


	     log.debug ( 'PRE--taskInstance.hasErrors: ')

	   if (!taskInstance.validate()) {
//		   log.debug ( 'TaskService.taskInstance.errors: ' +  taskInstance.errors )
		    result.error = taskInstance.errors
			
			log.debug ( 'TaskService.taskInstance.errors: ' +  taskInstance.errors )
			log.debug ( 'TaskService. result.error : ' +  result.error )
			throw new RuntimeException ( result.error.toString() ) ;
	   }
	  
	   log.debug ( 'PRE--save ') 
	 
	   
		if (!taskInstance.save( flush:true ) ) {
			log.error ( '!taskInstance.save: ' + taskInstance.errors )
			result.error ="Fallo el grabado de Tareas: " +   taskInstance.errors
			
			//return (result)
			throw new RuntimeException(result)
		}



	   log.error ( '**************grabo workEffortpartyassignment')
	   if ( taskInstance.state == 1) {
		   //Solo grabo propietario si es el alta del registro

		   def wepaInstance = new WorkEffortPartyAssignmentService()

		   wepaInstance.party = session.user
		   wepaInstance.roleType = RoleType.get('OWNER')
		   wepaInstance.fromDate = new Date()
		   wepaInstance.statusDateTime = new Date()
		   wepaInstance.description = 'Creación Inicial'
		   wepaInstance.detail = 'Creación Inicial'

		   wepaInstance.workEffort = taskInstance.workEffort

		   wepaInstance.delegateReasonEnum = Enumeration.get('WEDR_INITIAL')
		   wepaInstance.expectationEnum = Enumeration.get('WEE_FYI')


		   wepaInstance.availabilityStatus = StatusItem.get('WEFA_AVAILABLE')
		   wepaInstance.status = StatusItem.get('WOEA_ASSIGNED')

		   //Seguridad & Auditoria
		   wepaInstance.state = 1 // 0-Borrado 1-ok  2-modificado
		   wepaInstance.userCreated = session.user
		   wepaInstance.userUpdated = session.user
		   wepaInstance.dateCreated = new Date()
		   wepaInstance.lastUpdated = new Date()

		   if (!wepaInstance.validate()) {
			   log.debug('Esfuerzo con Errores: ' + wepaInstance.errors)
			   return fail(code: "Esfuerzo con Errores" + wepaInstance.errors)

		   } else {
			   log.debug('wepaInstance-save: No tiene errores ')
			   if (!wepaInstance.save(flush: true)) {
				   log.debug('!wepaInstance.save: ' + wepaInstance.errors)
				   throw new ValidationException("Errores de Validacion : ", wepaInstance.errors)
			   }
		   }
	   }


		
	   result.taskInstance = taskInstance
	   log.debug ( 'taskService.save(): - SUCCESS')
	   // Success.
	   return result
	   }


	@Transactional
	def saveTaskSource(WorkEffort workEffortInstance, Task taskInstance) {

		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session
		log.debug ( '*****-INGRESO A saveTask - *******: ' )

		def result = [:]
		def fail = { Map m ->
			result.error = [ code: m.code, args: ["taskInstance"] ]
			return result
		}
		if (workEffortInstance == null) {
//		   notFound()
			return fail(code:"Esfuerzo de Tarea con Valores Nulos")
		}
		if (taskInstance == null) {
			//		   notFound()
			return fail(code:" Tarea con Valores Nulos")
		}
		//
		log.debug ( ' ***************PRE-WorkEffortService.save'  )
		workEffortInstance.workEffortType = WorkEffortType.read("TASK")



		workEffortInstance.workEffortPurposeType  = WorkEffortPurposeType.read('WEPT_ANALYSIS_DESIGN')
//	   workEffortInstance.code 			 = taskInstance.code
		workEffortInstance.actualCompletionDate= null
		workEffortInstance.actualStartDate = null
		workEffortInstance.scopeEnum = Enumeration.read('WES_PUBLIC')
		workEffortInstance.estimatedCompletionDate= null
		workEffortInstance.estimatedStartDate=null
		workEffortInstance.percentCompleted = 0
		workEffortInstance.currentStatus= StatusItem.get ('TASK_PLANNING')
		workEffortInstance.moneyUom =  Uom.read ('ARS')



		result = WorkEffortService.save  (  workEffortInstance )
		log.debug ('taskService.savetask().WorkEffortService.save.properties='+result.properties)
		log.debug ('taskService.savetask().WorkEffortService.save.result='+result)
		log.debug ('taskService.savetask().WorkEffortService.save.result='+result.error)

		if (!result.workEffortInstance) {
			log.debug ( 'FALLO LA VALIDACION!  -->!workEffortInstance.save.result: ' + result )
			throw new RuntimeException(result.error.toString())
//		   return  ( result )
		}else{
			log.debug ('result.workEffortInstance.id: ' + result.workEffortInstance.id )
			taskInstance.userUpdated          =   session.user
			taskInstance.state     		   =  2
			log.debug ('result-pre. taskInstance.properties  : ' +  taskInstance.properties   )
			if (!taskInstance.id){
				//Es un registro nuevo y tengo que insertar los valores de id y ticket

				taskInstance.userCreated       = session.user
				taskInstance.id      		   = result.workEffortInstance.id
				taskInstance.workEffort 	   = result.workEffortInstance
				taskInstance.state             = 1
				//taskInstance.status 			= null

				log.debug ('result. taskInstance.id          : ' +  taskInstance.id           )
				log.debug ('result. taskInstance.properties  : ' +  taskInstance.properties   )
				log.debug ('result. taskInstance.workEffort  : ' +  taskInstance.workEffort.properties  )




			}
		}


		log.debug ( 'PRE--taskInstance.hasErrors: ')

		if (!taskInstance.validate()) {
//		   log.debug ( 'TaskService.taskInstance.errors: ' +  taskInstance.errors )
			result.error = taskInstance.errors

			log.debug ( 'TaskService.taskInstance.errors: ' +  taskInstance.errors )
			log.debug ( 'TaskService. result.error : ' +  result.error )
			throw new RuntimeException ( result.error.toString() ) ;
		}

		log.debug ( 'PRE--save ')


		if (!taskInstance.save( flush:true ) ) {
			log.error ( '!taskInstance.save: ' + taskInstance.errors )
			result.error ="Fallo el grabado de Tareas: " +   taskInstance.errors

			//return (result)
			throw new RuntimeException(result)
		}



		log.error ( '**************grabo workEffortpartyassignment')
		if ( taskInstance.state == 1) {
			//Solo grabo propietario si es el alta del registro

			def wepaInstance = new WorkEffortPartyAssignmentService()

			wepaInstance.party = session.user
			wepaInstance.roleType = RoleType.get('OWNER')
			wepaInstance.fromDate = new Date()
			wepaInstance.statusDateTime = new Date()
			wepaInstance.description = 'Creación Inicial'
			wepaInstance.detail = 'Creación Inicial'

			wepaInstance.workEffort = taskInstance.workEffort

			wepaInstance.delegateReasonEnum = Enumeration.get('WEDR_INITIAL')
			wepaInstance.expectationEnum = Enumeration.get('WEE_FYI')


			wepaInstance.availabilityStatus = StatusItem.get('WEFA_AVAILABLE')
			wepaInstance.status = StatusItem.get('WOEA_ASSIGNED')

			//Seguridad & Auditoria
			wepaInstance.state = 1 // 0-Borrado 1-ok  2-modificado
			wepaInstance.userCreated = session.user
			wepaInstance.userUpdated = session.user
			wepaInstance.dateCreated = new Date()
			wepaInstance.lastUpdated = new Date()

			if (!wepaInstance.validate()) {
				log.debug('Esfuerzo con Errores: ' + wepaInstance.errors)
				return fail(code: "Esfuerzo con Errores" + wepaInstance.errors)

			} else {
				log.debug('wepaInstance-save: No tiene errores ')
				if (!wepaInstance.save(flush: true)) {
					log.debug('!wepaInstance.save: ' + wepaInstance.errors)
					throw new ValidationException("Errores de Validacion : ", wepaInstance.errors)
				}
			}
		}



		result.taskInstance = taskInstance
		log.debug ( 'taskService.save(): - SUCCESS')
		// Success.
		return result
	}
   
   @Transactional
   def save(Task taskInstance) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   log.error ( '*****-INGRESO A save - *******: ' )
	   
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["taskInstance"] ]
		   return result
	   }
	   def workEffortInstance = taskInstance.workEffort
	   if (workEffortInstance == null) {
//		   notFound()
		  return fail(code:"Esfuerzo de Tarea con Valores Nulos")
	   }
	   log.debug ('taskInstance.workEffort.percentCompleted: ' + taskInstance.workEffort.percentCompleted )
	   if (taskInstance == null) {
		   //		   notFound()
					 return fail(code:" Tarea con Valores Nulos")
				  }
		   //
	   log.debug ( ' ***************PRE-WorkEffortService.save'  )

	   /*
	    * Cago valores especificos del workEffort
	    */
	   workEffortInstance.workEffortType = WorkEffortType.read("TASK")
	   //log.error ('taskService.save().WorkEffortService.save.properties='+ result.properties)
	   
	   result = WorkEffortService.save  (  workEffortInstance )
	   log.error ('taskService.save().WorkEffortService.save.properties='+ result.properties)
	   log.debug ('taskService.save().WorkEffortService.save.result='    + result)
	   log.error ('taskService.save().WorkEffortService.save.result='    + result.error)
	   
	   if (!result.workEffortInstance) {
		   log.error ( 'FALLO LA VALIDACION!  -->!workEffortInstance.save.result: ' + result )
		   throw new RuntimeException(result.error.toString())
//		   return  ( result )
		 }else{
			 log.error ('result.workEffortInstance.id: ' + result.workEffortInstance.id )
			 taskInstance.userUpdated          =   session.user
			 taskInstance.state     		   =  2
			 log.debug ('result-pre. taskInstance.properties  : ' +  taskInstance.properties   )
		     if (!taskInstance.id){
				 //Es un registro nuevo y tengo que insertar los valores de id y ticket
				 
		    	 taskInstance.userCreated       = session.user
		    	 taskInstance.id      		   = result.workEffortInstance.id
				 taskInstance.workEffort 	   = result.workEffortInstance
				 taskInstance.state             = 1
				  
				 
				 log.debug ('result. taskInstance.id          : ' +  taskInstance.id           )
				 log.debug ('result. taskInstance.properties  : ' +  taskInstance.properties   )
				 log.error ('result. taskInstance.workEffort  : ' +  taskInstance.workEffort.properties  )
			 }
		 }
	 
	   
	     log.debug ( 'PRE--taskInstance.hasErrors: ')
	   
	   if (!taskInstance.validate()) {
//		   log.debug ( 'TaskService.taskInstance.errors: ' +  taskInstance.errors )
		    result.error = taskInstance.errors
			
			log.error ( 'TaskService.taskInstance.errors: ' +  taskInstance.errors )
			log.error ( 'TaskService. result.error : ' +  result.error )
			throw new RuntimeException ( result.error.toString() ) ;
	   }
	  
	   log.debug ( 'PRE--save ')
	 
	   
		if (!taskInstance.save( flush:true ) ) {
			log.error ( '!taskInstance.save: ' + taskInstance.errors )
			result.error ="Fallo el grabado de Tareas: " +   taskInstance.errors
			
			//return (result)
			throw new RuntimeException(result)
		}

	   log.error ( '**************grabo workEffortpartyassignment')
	   if ( taskInstance.state == 1) {
		   //Solo grabo propietario si es el alta del registro

		   //*************************Aca va el alta de propietario
		   def wepaInstance = new WorkEffortPartyAssignment()
		   def wepaParty = Party.findByUser(session.user)
		   log.error ('wepaParty_ ' + wepaParty)
		   wepaInstance.party = wepaParty
		   if (wepaInstance.party) {
			   wepaInstance.id = TransactionService.getUIID()
			   wepaInstance.roleType = RoleType.get('OWNER')
			   wepaInstance.fromDate = new Date()
			   wepaInstance.statusDateTime = new Date()
			   wepaInstance.description = 'Creación Inicial'
			   wepaInstance.detail = 'Creación Inicial'
			   wepaInstance.workEffort = taskInstance.workEffort

			   wepaInstance.delegateReasonEnum = Enumeration.get('WEDR_INITIAL')
			   wepaInstance.expectationEnum = Enumeration.get('WEE_FYI')


			   wepaInstance.availabilityStatus = StatusItem.get('WEFA_AVAILABLE')
			   wepaInstance.status = StatusItem.get('WOEA_ASSIGNED')

			   //Seguridad & Auditoria
			   wepaInstance.state = 1 // 0-Borrado 1-ok  2-modificado
			   wepaInstance.userCreated = session.user
			   wepaInstance.userUpdated = session.user
			   wepaInstance.dateCreated = new Date()
			   wepaInstance.lastUpdated = new Date()

			   if (!wepaInstance.validate()) {
				   log.error('Esfuerzo con Errores: ' + wepaInstance.errors)
				   return fail(code: "Esfuerzo con Errores" + wepaInstance.errors)

			   } else {
				   log.debug('wepaInstance-save: No tiene errores ')
				   if (!wepaInstance.save(flush: true)) {
					   log.error('!wepaInstance.save: ' + wepaInstance.errors)
					   throw new ValidationException("Errores de Validacion : ", wepaInstance.errors)
				   }
			   }
	   }



	   }
	   result.taskInstance = taskInstance
	   log.debug ( 'taskService.save(): - SUCCESS')
	   // Success.
	   return result
   }

   /*
    * Convierto directamente una historia de usuario en una tarea
    * para los casos en los que la simplicidad o unicidad de la historia 
    * indique que lo mejor sea no crear la tarea a mano o generarlas  a 
    * traves de procesos.
    * Cambio los estados de la historia de Usuario Original
    * A su vez debo dejar grabada la relación entre los workEfforts
    */
   
   
  
   @Transactional
   def convert(String userStoryId) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   def taskInstance  = new Task()
	   def workEffortInstance  
	   def userStoryOriginal  = UserStory.read(userStoryId)
	   
	   log.debug ( 'userStoryOriginal.properties: ' + userStoryOriginal.properties )
	   def workEffortOriginal = WorkEffort.read ( userStoryOriginal.workEffort.id  )
	   
	   
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["taskInstance"] ]
		   return result
	   }
	   
	   log.debug ( 'Verifico workEffortOriginal' )
	   if (workEffortOriginal == null) {
//		   notFound()
		   log.debug ( 'Esfuerzo de Origen Inexistente' )
		  return fail(code:"Esfuerzo de Origen Inexistente")
	   }
	   log.debug ( 'Verifico userStoryOriginal' )
	   if (userStoryOriginal == null) {
		   //		   notFound()
					  log.debug ( 'Historia Usuario de Origen Inexistente' )
					 return fail(code:"Historia Usuario de Origen Inexistente")
				  
	   }
		 //Lo marco como modificación porque si entro luego a la creación de workEffort verificaríamos que es nuevo y lo pisa con estado 1

		 //Creo el registro de workEffort
		 log.debug ( "***********Creo el workEffort*************")
		 workEffortInstance = new WorkEffort ()
		 log.debug ( "***********Creo y cargo el taskInstance*************")
		 taskInstance       = new Task (  )
		 	//
		 
	   
	   if (workEffortInstance){

		 //  def workEffortInstance = new WorkEffort()
//		   workEffortInstance.workEffortName = 'Generación por Tarea de usuario'
		   workEffortInstance.workEffortPurposeType  = WorkEffortPurposeType.read('WEPT_ANALYSIS_DESIGN')
		   workEffortInstance.workEffortType = WorkEffortType.read("TASK")
		  // workEffortInstance.id  =  TransactionService.serviceID('TASK',session.user.toString() )
		   workEffortInstance.id  =  TransactionService.getUIID()
		   workEffortInstance.currentStatus= StatusItem.get ('WEPR_PLANNING')
		   workEffortInstance.moneyUom =  Uom.read ('ARS')
		   workEffortInstance.userCreated     = session.user // User.read ( session.user.id )
		   workEffortInstance.scopeEnum = Enumeration.read('WES_PUBLIC')
		   workEffortInstance.ticket  = workEffortInstance.id
		  //
		   workEffortInstance.description = workEffortOriginal.description
		    workEffortInstance.code = workEffortOriginal.code
		     workEffortInstance.actualCompletionDate= null
//		   workEffortInstance.actualMilliSeconds= 0
//		   workEffortInstance.actualSetupMillis= 0
		   workEffortInstance.actualStartDate = null
		   workEffortInstance.description = workEffortOriginal.description
		   workEffortInstance.estimatedCompletionDate= null
//		   workEffortInstance.estimatedMilliSeconds= 0
//		   workEffortInstance.estimatedSetupMillis= 0
		   workEffortInstance.estimatedStartDate=null
//		   workEffortInstance.percentComplete = 0
//		   workEffortInstance.totalMilliSecondsAllowed= 0
		   workEffortInstance.state           = 1
		   workEffortInstance.userUpdated     = session.user
		   
	   }
	   if (!workEffortInstance.validate()) {
		   log.debug ( 'Esfuerzo con Errores: ' + workEffortInstance.errors )
		   return fail(code:"Esfuerzo con Errores"+ workEffortInstance.errors)

	   }else{
		   log.debug ('workEffortInstance-save: No tiene errores ')
		   if (!workEffortInstance.save( flush:true )) {
			   log.debug ( '!workEffortInstance.save: ' + workEffortInstance.errors )
			    throw new ValidationException("Errores de Validacion : ", taskInstance.errors)
			}
	   }
	   if ( taskInstance ){
				   //Si grabé el workEffort es porque es nuevo...y tengo que actualizar la instancia de taskInstance
				   log.debug ('workEffortInstance.id: ' + workEffortInstance.id)
				   //Cargo los datos de task
				   //	   taskInstance.version                     =     userStoryOriginal.version
				   //	   taskInstance.date_created                =     userStoryOriginal.date_created
				   //	   taskInstance.date_deleted                =     userStoryOriginal.date_deleted
				  taskInstance.detail                      =     userStoryOriginal.detail
				 
				  //taskInstance.last_updated               =     userStoryOriginal.last_updated

				  taskInstance.complexity                  =     userStoryOriginal.complexity
				  taskInstance.level                       =     userStoryOriginal.level
				  taskInstance.urgency                     =     userStoryOriginal.urgency
				 // taskInstance.taskSituation	           =     ''
				 // taskInstance.delegated_to                =     userStoryOriginal.delegated_to
				 // taskInstance.dueDate                    =     userStoryOriginal.dueDate
				  taskInstance.energy                      =     userStoryOriginal.energy
//				  taskInstance.follow_up_date              =     userStoryOriginal.follow_up_date
				  taskInstance.idOrigen                   =     userStoryOriginal.id
				  taskInstance.taskExcecutionType         =     TaskExecutionType.get ('ASAP')
//				  taskInstance.projectTaxonomy        		=     userStoryOriginal.projectTaxonomy
//				  taskInstance.description                 =     userStoryOriginal.description
//				  taskInstance.project                     =     userStoryOriginal.project
//				  taskInstance.status                  	   =     userStoryOriginal.status 
//				  taskInstance.code                        =     userStoryOriginal.code
				  log.debug ('pre id?')
				   taskInstance.workEffort      =  WorkEffort.get ( workEffortInstance.id )
				   taskInstance.id              =  workEffortInstance.id
				   taskInstance.userCreated     =  session.user
				   taskInstance.userUpdated     =  session.user
				   taskInstance.state           = 1
				   

	   }

	   log.debug  ( "workEffortInstance : " + workEffortInstance.properties )
	   log.debug  ( "workEffortOriginal.properties : " + workEffortOriginal.properties )
	   log.debug  ( "workEffortOriginal.code : "  + workEffortOriginal.code )
	   log.debug  ( "workEffortInstance.code : " + workEffortInstance.code )
	   log.debug  ( "taskInstance.properties : " + taskInstance.properties )
	   
		 if (!taskInstance.validate()) {
		     log.debug ( 'Tarea con Errores: ' + taskInstance.errors )
			 return fail(code:"Tarea con Errores"+ taskInstance.errors)
		
		 }
		   log.debug ('taskservice-validate: No tiene errores')
				 
		   if (!taskInstance.save( flush:true)) {
			   log.debug ( '!taskInstance.save: ' + taskInstance.errors )
//			   throw new RuntimeException("Fallo el grabado de taskInstance")
//			   return fail(code:"Fallo el grabado de taskInstance"+ taskInstance.errors)
			   throw new ValidationException("Errores de Validacion: ", taskInstance.errors)
		   }

		   log.debug ('taskservice-save: sale por la opcion de resultado exitoso')
		   result.taskInstance = taskInstance

	   //Primero tengo que hacer el cambio de Estado de workEffortOrigen y su userStory
	   // y despues crear el registro de relación
	   
	   def workEffortAssocInstance = new WorkEffortAssoc()
	   workEffortAssocInstance.id  =  TransactionService.serviceID('WEAI',session.user.toString() )

	   workEffortAssocInstance.workEffortIdFrom 	= userStoryOriginal.workEffort
	   workEffortAssocInstance.workEffortIdTo   	= taskInstance.workEffort
	   workEffortAssocInstance.workEffortAssocType	= WorkEffortAssocType.get( 'WORK_EFF_STORY_TASK' )
	   workEffortAssocInstance.sequenceNum			= 10
	   workEffortAssocInstance.fromDate				= new Date()
//	   workEffortAssocInstance.thruDate				=

	   workEffortAssocInstance.state   				=	1
	   workEffortAssocInstance.userCreated 			= session.user
	   workEffortAssocInstance.userUpdated 			= session.user
	   
 
	   if (!workEffortAssocInstance.save( flush:true)) {
		   log.error ( '!workEffortAssocInstance.save: ' + workEffortAssocInstance.errors )
//			   throw new RuntimeException("Fallo el grabado de taskInstance")
//			   return fail(code:"Fallo el grabado de taskInstance"+ taskInstance.errors)
		   throw new ValidationException("Errores de Validacion: ", workEffortAssocInstance.errors)
	   }
	  
	   log.error ( '!workEffortAssocInstance.save: ' + workEffortAssocInstance.errors )
	   log.debug ('taskservice-save: sale por la opcion de resultado exitoso')

	   result.workEffortAssocInstance = workEffortAssocInstance
   // Success.
   return result
   }
   
   
   def createFromWs_issue(String ws_issueId) {
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   def taskInstance  = new Task()
	   def workEffortInstance  
	   def ws_issueInstance  = Ws_issue.read(ws_issueId)
	   
	   log.debug ( 'ws_issueInstance.properties: ' + ws_issueInstance.properties )
	   //def workEffortOriginal = WorkEffort.read ( ws_issueInstance.workEffort.id  )


	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["taskInstance"] ]
		   log.error ( "Error TaskService: " + result.error )
		   return result
	   }

//	   log.debug ( 'Verifico ws_issueInstance' )
//	   if (ws_issueInstance == null) {
////		   notFound()
//		   log.debug ( 'Esfuerzo de Origen Inexistente' )
//		  return fail(code:"Esfuerzo de Origen Inexistente")
//	   }
	   log.debug ( 'Verifico ws_issueInstance' )
	   if (ws_issueInstance == null) {
		   //		   notFound()
					  log.error ( 'Ws_issue de Origen Inexistente' )
					 return fail(code:"Issue de Origen Inexistente")
				  
	   }
		 //Lo marco como modificación porque si entro luego a la creación de workEffort verificaríamos que es nuevo y lo pisa con estado 1
		 
		 //Creo el registro de workEffort
		 log.debug ( "***********Creo el workEffort*************")
		 workEffortInstance = new WorkEffort ()
		 log.debug ( "***********Creo y cargo el taskInstance*************")
		 taskInstance       = new Task (  )
		 	//
		 
	   
	   if (workEffortInstance){

		 //  def workEffortInstance = new WorkEffort()
//		   workEffortInstance.workEffortName = 'Generación por Tarea de usuario'
		   workEffortInstance.workEffortPurposeType  = WorkEffortPurposeType.read('WEPT_ANALYSIS_DESIGN')
		   workEffortInstance.workEffortType = WorkEffortType.read("TASK")
		   workEffortInstance.id  =  TransactionService.serviceID('TASK',session.user.toString() )
		   workEffortInstance.currentStatus= StatusItem.get ('WEPR_PLANNING')
		   workEffortInstance.moneyUom =  Uom.read ('ARS')
		   workEffortInstance.userCreated     = session.user // User.read ( session.user.id )
		   workEffortInstance.scopeEnum = Enumeration.read('WES_PUBLIC')
		   workEffortInstance.ticket  = workEffortInstance.id
		   workEffortInstance.code  = workEffortInstance.id
		   workEffortInstance.description = ws_issueInstance.description
		    //workEffortInstance.code = ws_issueInstance.code
		     workEffortInstance.actualCompletionDate= null
//		   workEffortInstance.actualMilliSeconds= 0
//		   workEffortInstance.actualSetupMillis= 0
		   workEffortInstance.actualStartDate = null
		   workEffortInstance.description = ws_issueInstance.description
		   workEffortInstance.estimatedCompletionDate= null
//		   workEffortInstance.estimatedMilliSeconds= 0
//		   workEffortInstance.estimatedSetupMillis= 0
		   workEffortInstance.estimatedStartDate=null
//		   workEffortInstance.percentComplete = 0
//		   workEffortInstance.totalMilliSecondsAllowed= 0
		   workEffortInstance.state           = 1
		   workEffortInstance.userUpdated     = session.user
		   
	   }
	   if (!workEffortInstance.validate()) {
		   log.error ( 'Esfuerzo con Errores: ' + workEffortInstance.errors )
		   return fail(code:"Esfuerzo con Errores"+ workEffortInstance.errors)
		
	   }else{
		   log.debug ('workEffortInstance-save: No tiene errores ')
		   if (!workEffortInstance.save( flush:true )) {
			   log.error ( '!workEffortInstance.save: ' + workEffortInstance.errors )
			    throw new ValidationException("Errores de Validacion : ", taskInstance.errors)
			}
	   }
	   if ( taskInstance ){
				   //Si grabé el workEffort es porque es nuevo...y tengo que actualizar la instancia de taskInstance
				   log.debug ('workEffortInstance.id: ' + workEffortInstance.id)
				   //Cargo los datos de task
				   //	   taskInstance.version                     =     ws_issueInstance.version
				   //	   taskInstance.date_created                =     ws_issueInstance.date_created
				   //	   taskInstance.date_deleted                =     ws_issueInstance.date_deleted
				  taskInstance.detail                      =     ws_issueInstance.detail
				  //taskInstance.last_updated               =     ws_issueInstance.last_updated
				  taskInstance.complexity                  =     ws_issueInstance.complexity
				  taskInstance.level                       =     ws_issueInstance.priority
				  taskInstance.urgency                     =     ws_issueInstance.urgency
				 // taskInstance.taskSituation	           =     ''
				 // taskInstance.delegated_to                =     ws_issueInstance.delegated_to
//				  taskInstance.dueDate                    =     ws_issueInstance.programmedDate
				  taskInstance.energy                      =     10
//				  taskInstance.follow_up_date              =     ws_issueInstance.follow_up_date
				  taskInstance.idOrigen                   =     ws_issueInstance.id
				  taskInstance.taskExcecutionType         =     TaskExecutionType.get ('ASAP')
//				  taskInstance.description                 =     ws_issueInstance.description
//				  taskInstance.projectTaxonomy        		=     ws_issueInstance.projectTaxonomy
//				  taskInstance.project                     =     ws_issueInstance.project
//				  taskInstance.status                  	   =     ws_issueInstance.status
//				  taskInstance.code                        =     workEffortInstance.code
				  log.debug ('pre id?')
				  taskInstance.workEffort      =  WorkEffort.get ( workEffortInstance.id )
				  taskInstance.id              =  workEffortInstance.id
				  taskInstance.userCreated     =  session.user
				  taskInstance.userUpdated     =  session.user
				  taskInstance.state           = 1
				   
				   
	   }
	 
	   log.debug  ( "workEffortInstance : " +workEffortInstance.properties )
	   log.debug  ( "ws_issueInstance.properties : " +ws_issueInstance.properties )
	   log.debug  ( "workEffortInstance.code : " +workEffortInstance.code )
	   log.debug  ( "taskInstance.properties : " +taskInstance.properties )
	   
		 if (!taskInstance.validate()) {
		     log.debug ( 'Tarea con Errores: ' + taskInstance.errors )
			 return fail(code:"Tarea con Errores"+ taskInstance.errors)
		
		 }
		   log.debug ('taskservice-validate: No tiene errores')
				 
		   if (!taskInstance.save( flush:true)) {
			   log.debug ( '!taskInstance.save: ' + taskInstance.errors )
//			   throw new RuntimeException("Fallo el grabado de taskInstance")
//			   return fail(code:"Fallo el grabado de taskInstance"+ taskInstance.errors)
			   throw new ValidationException("Errores de Validacion: ", taskInstance.errors)
		   }

		   log.debug ('taskservice-save: sale por la opcion de resultado exitoso')
		   result.taskInstance = taskInstance

//	   //Primero tengo que hacer el cambio de Estado de workEffortOrigen y su userStory
//	   // y despues crear el registro de relación 
//

		def wepaInstance = new WorkEffortPartyAssignment()
	   wepaInstance.id =  TransactionService.serviceID('wepa',session.user.toString() )
	   wepaInstance.party = Party.findByUser(session.user)
	   log.error ("user-party: " + wepaInstance.party	)
	   wepaInstance.roleType = RoleType.get('OWNER')
	   wepaInstance.fromDate = new Date()
	   wepaInstance.statusDateTime = new Date()
	   wepaInstance.description = 'Creación Inicial'
	   wepaInstance.detail= 'Creación Inicial'

	   wepaInstance.workEffort =taskInstance.workEffort

	   wepaInstance.delegateReasonEnum = Enumeration.get('WEDR_INITIAL')
	   wepaInstance.expectationEnum    =  Enumeration.get('WEE_FYI')


	   wepaInstance.availabilityStatus = StatusItem.get ('WEFA_AVAILABLE')
	   wepaInstance.status = StatusItem.get ('WOEA_ASSIGNED')


	   //Seguridad & Auditoria
	   wepaInstance.state   		= 1 // 0-Borrado 1-ok  2-modificado
	   wepaInstance.userCreated  = session.user
	   wepaInstance.userUpdated  = session.user
	   wepaInstance.dateCreated  = new Date()
	   wepaInstance.lastUpdated  = new Date()

	   if (!wepaInstance.validate()) {
		   log.error ( 'Esfuerzo con Errores: ' + wepaInstance.errors )
		   return fail(code:"Esfuerzo con Errores"+ wepaInstance.errors)

	   }else{
		   log.debug ('wepaInstance-save: No tiene errores ')
		   if (!wepaInstance.save( flush:true )) {
			   log.error ( '!wepaInstance.save: ' + wepaInstance.errors )
			   throw new ValidationException("Errores de Validacion : ", wepaInstance.errors)
		   }
	   }

	   
	   
	   //Cambio el estado de ws_issue para darlo como cerrado y no aparezca mas como activo.
	   ws_issueInstance.state				 = 0
	   ws_issueInstance.realizedDate		= new Date()
	   ws_issueInstance.statusClosed		= 1
	   
	   if (!ws_issueInstance.save( flush:true)) {
		   log.error ( '!ws_issueInstance.save: ' + ws_issueInstance.errors )
//			   throw new RuntimeException("Fallo el grabado de taskInstance")
//			   return fail(code:"Fallo el grabado de taskInstance"+ taskInstance.errors)
		   throw new ValidationException("Errores de Validacion: ", ws_issueInstance.errors)
	   }
   // Success.
   return result
   }


	def  ArrayList<GroovyRowResult> getTasks(params) {
		//Devuelvo todos los proyectos en los que intervengo

		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session

		// log.debug ('service.params: ' + params )
		log.debug 'service.params: ' + params.toString()

		def tasktype = WorkEffortType.get('TASK')

		def userPartyRoles = Party.findAllByUser( session.user )
		//
		def resultsWEPA = WorkEffortPartyAssignment.where {
			workEffort.workEffortType == tasktype &&  state > 0 &&  workEffort.state > 0
		}.list()

		def taskStatusParamsToArray = params.taskStatus

		log.debug ( 'taskService.params: ' +  params)
		log.debug  ('taskStatusParamsToArray: ' + taskStatusParamsToArray)

		StatusItem taskStatusList = []
		//def taskStatusList
//			   if ( !taskStatusParamsToArray  ){
//				   log.debug ( 'Entro a taskStatusParamsToArray Nulo')
//				   taskStatusParamsToArray =    [ 'TASK_PLANNING','TASK_IN_PROGRESS']
//			   }
		def taskStatusItemList
		if (! taskStatusParamsToArray  ){
			log.debug  ('taskStatusParamsToArray NULO : ')
			// taskStatusItemList = StatusItem.findAllByStatusType (StatusType.get('TASK_STATUS' ))
			taskStatusItemList = [ StatusItem.get('TASK_IN_PROGRESS'),  StatusItem.get('TASK_PLANNING')]
		}  else  {
			log.debug  ('taskStatusParamsToArray CON VALOR : ')
			//   taskStatusItemList = StatusItem.where { id in  taskStatusParamsToArray}
			taskStatusItemList = StatusItem.getAll ( taskStatusParamsToArray)


		}
		log.debug('taskStatusItemList.typeOf() :' + taskStatusItemList )
		log.debug ( 'Encontré los statusItem: ' + taskStatusItemList )

		log.debug ( 'POST StatusItem.where ')
		log.debug ( 'taskStatusParamsToArray: ' + taskStatusParamsToArray)
		log.debug ( 'taskStatusList: 1622'      + taskStatusList)
		log.debug ( 'taskStatusItemList: 1622' + taskStatusItemList)


		//Agrego los ultimos filtros si existen
		//Proyecto y fechas
		def pfechaDesde
		def pfechaHasta

		Calendar fechaHasta = Calendar.getInstance();
		Calendar fechaDesde = Calendar.getInstance();
		def projectInstance
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");


		if (params.fechaHasta){
			fechaHasta.setTime(sdf.parse(params.fechaHasta));// all done
			log.debug ('Calendar fechaHasta: ' + fechaHasta )
			pfechaHasta =  sdf.parse(params.fechaHasta);

		}

		if (params.fechaDesde){
			//			 fechaDesde = params.date('fechaDesde', 'dd/MM/yyyy')
			fechaDesde.setTime(sdf.parse( params.fechaDesde));// all done
			log.debug ('Calendar fechaDesde: ' + fechaDesde )
			pfechaDesde =  sdf.parse(params.fechaDesde);

		}
		if ( params.projectSelection){
			projectInstance = Project.get(params.projectSelection)

		}
		log.debug ( 'projectInstance: ' +  projectInstance)
		def textoDesde = " Desde: "+ fechaDesde.get(Calendar.DAY_OF_MONTH)+'/'+ ( fechaDesde.get(Calendar.MONTH)+1 )+'/'+ fechaDesde.get(Calendar.YEAR)
		def textoHasta = " Hasta: "+ fechaHasta.get(Calendar.DAY_OF_MONTH)+'/'+ (fechaHasta.get(Calendar.MONTH)+1 )+'/'+ fechaHasta.get(Calendar.YEAR)
		def proyecto= "Sin Datos"

		def leyendaAnterior =""

		if ( projectInstance ){
			leyendaAnterior = "Proyecto: " + projectInstance
		}else{
			leyendaAnterior = "Proyectos: Todos"
		}
		//leyendaAnterior =  leyendaAnterior +  textoDesde + textoHasta
		def leyendaAnterior2 = textoDesde + textoHasta

		fechaDesde.add(Calendar.DATE, -1);
		fechaHasta.add(Calendar.DATE, 1);



		def resultsWE = WorkEffort.where {
			// id in resultsWEPA.workEffort.id   &&
					and {
						if (projectInstance){
							eq("project",projectInstance)
						}
					}
			state > 0  &&
					workEffortType == tasktype    &&
					currentStatus in ( taskStatusItemList  ) &&
                    between('dueDate', fechaDesde.getTime(), fechaHasta.getTime())

        }.list()


		def results = Task.where {
			id in resultsWE.id


		}.list()

		log.debug ('userPartyRoles: ' + userPartyRoles)
		log.debug ('resultsWEPA: ' + resultsWEPA)
		log.debug ('results: ' + results)

//				results.message = leyendaAnterior2
//				log.debug ( results.message )
		return  results

	}

/**
 *
 * Functionality: Return all tasks owned by me
 * <p>
 * This method always returns immediately, whether or not the
 * image exists. When this applet attempts to draw the image on
 * the screen, the data will be loaded. The graphics primitives
 * that draw the image will incrementally paint on the screen.
 *
 * @param params
 * @return java.util.LinkedHashMap
 */

	def java.util.LinkedHashMap getMyTasks(params) {



		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session

		log.error('indexMyTasks.params: ' + params)
		def taskFilter = ['TASK_PLANNING', 'TASK_IN_PROGRESS']
		Calendar fechaHasta = Calendar.getInstance();
		fechaHasta.add(Calendar.DAY_OF_YEAR, 365);

		Calendar fechaDesde = Calendar.getInstance();
		fechaDesde.add(Calendar.DAY_OF_YEAR, -365);
		def projectSelection = Long.valueOf(0)
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		String fechaThru = fechaHasta.get(Calendar.DAY_OF_MONTH) + '/' + (fechaHasta.get(Calendar.MONTH) + 1) + '/' + fechaHasta.get(Calendar.YEAR)
		String fechaFrom = fechaDesde.get(Calendar.DAY_OF_MONTH) + '/' + (fechaDesde.get(Calendar.MONTH) + 1) + '/' + fechaDesde.get(Calendar.YEAR)

		Date pfechaHasta = sdf.parse(fechaThru);
		Date pfechaDesde = sdf.parse(fechaFrom);

		if (params.fechaHasta) {
			fechaHasta.setTime(sdf.parse(params.fechaHasta));// all done
			log.debug('Calendar fechaHasta: ' + fechaHasta)
			pfechaHasta = sdf.parse(params.fechaHasta);
			fechaThru = fechaHasta.get(Calendar.DAY_OF_MONTH) + '/' + (fechaHasta.get(Calendar.MONTH) + 1) + '/' + fechaHasta.get(Calendar.YEAR)

		}

		if (params.fechaDesde) {
//			 fechaDesde = params.date('fechaDesde', 'dd/MM/yyyy')
			fechaDesde.setTime(sdf.parse(params.fechaDesde));// all done
			log.error('Calendar fechaDesde: ' + fechaDesde)
			pfechaDesde = sdf.parse(params.fechaDesde);
			fechaFrom = fechaDesde.get(Calendar.DAY_OF_MONTH) + '/' + (fechaDesde.get(Calendar.MONTH) + 1) + '/' + fechaDesde.get(Calendar.YEAR)

		}


		def textoDesde = " Desde: " + fechaDesde.get(Calendar.DAY_OF_MONTH) + '/' + (fechaDesde.get(Calendar.MONTH) + 1) + '/' + fechaDesde.get(Calendar.YEAR)
		def textoHasta = " Hasta: " + fechaHasta.get(Calendar.DAY_OF_MONTH) + '/' + (fechaHasta.get(Calendar.MONTH) + 1) + '/' + fechaHasta.get(Calendar.YEAR)

		if (params.projectSelection) {
			projectSelection = params.projectSelection

		}
		/*
           INICIO - DEFINICION valores Filtro de Estado
           */

		def textoStatus = " - Estados: Aceptados, En Progreso, En Pausa, Planificación, En espera de otra Tarea] "
		def statusFilter
		if (params.itemStatusForm) {

			statusFilter = StatusItem.getAll(params.itemStatusForm)

		} else {

			statusFilter = StatusItem.getAll('TASK_IN_PROGRESS', 'TASK_ACCEPTED', 'TASK_PLANNING')

		}
		textoStatus = " - Estados: " + statusFilter
		/*
           FIN- DEFINICION valores Filtro de Estado
        */


		projectSelection = Project.get(projectSelection)

		def leyendaAnterior = ""

		if (projectSelection) {
			leyendaAnterior = "Proyecto: " + projectSelection
		} else {
			leyendaAnterior = "Proyectos: Todos"
		}

		leyendaAnterior = leyendaAnterior + textoStatus
		def leyendaAnterior2 = textoDesde + textoHasta

		//log.error ( 'fechaFrom: ' +fechaFrom )
		//log.error ( 'fechaThru: ' +fechaThru )
		params.fechaDesde = fechaFrom
		params.fechaHasta = fechaThru

		//log.error ( 'params.fechaDesde: ' + params.fechaDesde)
		//log.error ( 'params.fechaHasta: ' + params.fechaHasta)


		def tasktype = WorkEffortType.get('TASK')

		def userPartyRoles = Party.findAllByUser(session.user)
		//
		def resultsWEPA = WorkEffortPartyAssignment.where {
			workEffort.workEffortType == tasktype && state > 0 && workEffort.state > 0 && party in userPartyRoles
		}.list()

		def taskStatusParamsToArray

		taskStatusParamsToArray = params.itemStatusForm


		def projectInstance
		if (params.projectSelection) {
			projectInstance = Project.get(params.projectSelection)

		}


		def taskAssocType = WorkEffortAssocType.get('WORK_EFF_REQ_TASK')



		def weaCriteria = WorkEffortAssoc.createCriteria()

		def resultWEPATask = weaCriteria.list() {
			'workEffortAssocType' {
				eq('id', taskAssocType.id)

			}

			'workEffortIdTo' {
				'in'("id", resultsWEPA.workEffort.id)
				eq("workEffortType", tasktype)
				if (projectInstance) {
					eq("project", projectInstance)
				}

				gt('state', 0)
				if (statusFilter) {
					'in'('currentStatus', (statusFilter))
				}
				between('dueDate', fechaDesde.getTime(), fechaHasta.getTime())
			}
		}
		def results = Task.where {
			id in resultWEPATask.workEffortIdTo.id
//            and {
//                between('dateCreated', fechaDesde.getTime(), fechaHasta.getTime())
//                gt('state', 0)
//            }
		}.list()

		log.error('resultWEPATask: ' + resultWEPATask)
		log.error('resultWEPATask.size: ' + resultWEPATask.size())
		log.debug('resultsWEPA: ' + resultsWEPA)

		def result = [:]
		result.pfechaDesde = fechaFrom
		result.pfechaHasta = fechaThru
		result.statusFilter = statusFilter
		result.leyendaAnterior2 = leyendaAnterior2
		result.leyendaAnterior = leyendaAnterior
		result.projectTypeList = ProjectType.findAllByStateGreaterThan(0)
		result.itemStatusList = StatusItem.findAllByStatusType(StatusType.get('TASK_STATUS'))
		// result.taskInstanceList = Task.findAllByWorkEffort( resultsWEPA.workEffort )

		result.taskInstanceList = results

		result.planningTypeList = WorkEffortType.findAllByWorkEffortTypeRoot(WorkEffortType.get('STRATEGIC_PLANNING'))
		result.resultWEPATask = resultWEPATask

		log.error('taskInstanceList : ' + result.taskInstanceList  )
		log.error('taskInstanceList.size: ' + result.taskInstanceList.size())
		log.error('taskInstanceList : ' + result.taskInstanceList  )

		return result

	}

   def  ArrayList<GroovyRowResult> getMyBackLog() {
	   //Devuelvo todos los proyectos en los que intervengo
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
				   
	   /*
		* Esto Funciona perfecto, pero no logro sacar los distincts projects
		*/
	   
	    def projectType = WorkEffortType.get('PROJECT')
		log.debug ( 'projectType : ' +projectType)
		
	    def userPartyRoles = Party.findAllByUser( session.user )
		
		log.debug ('userPartyRoles: ' + userPartyRoles)
		
		def resultsWEPAProjects = WorkEffortPartyAssignment.where {
			workEffort.workEffortType == projectType &&  state > 0   && party in userPartyRoles
		}.list()

		log.debug ('resultsWEPAProjects: ' + resultsWEPAProjects)
		
			   def tasktype = WorkEffortType.get('TASK')
			   def taskStatusCompleted = StatusItem.get( 'WEPR_COMPLETE')
			   log.debug ('tasktype: ' + tasktype)
			   //  def results = WorkEffortPartyAssignment.where {
			   //	  workEffort.workEffortType == proyectos &&  state > 0   && party in userPartyRoles
			   //  }.list()
			   
			   //Tareas asignadas de los proyectos donde estoy asignado
			   
				  def resultsWEPAProjectsTasks = WorkEffortPartyAssignment.where {
				   workEffort.workEffortType == tasktype &&
				   workEffort.project in  resultsWEPAProjects.workEffort.project &&
				    state > 0   && party in userPartyRoles
			   }.list()
			   
			    log.debug ('resultsWEPAProjectsTasks: ' + resultsWEPAProjectsTasks)
				log.debug ('resultsWEPAProjectsTasks.workEffort: ' + resultsWEPAProjectsTasks.workEffort)
				log.debug ('taskStatusCompleted : ' + taskStatusCompleted)
				
				def results = Task.where {
				   workEffort.workEffortType == tasktype &&
				    state > 0  && 
				     workEffort.currentStatus != taskStatusCompleted 
				}.list()
				
				// &&
				//				   workEffort.project in  resultsWEPAProjects.workEffort.project &&
				//				   workEffort  { not { 'in' resultsWEPAProjectsTasks.workEffort  } }
				log.debug ('results: ' + results)
				
	   return  results
	   
   }
   
   
   @Transactional
   def updateTime(Task taskInstance) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	    
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["taskInstance"] ]
		   return result
	   }
	   
			 taskInstance.userUpdated          =   session.user
			 taskInstance.state     		   =  2
 
	   
	   if (!taskInstance.validate()) {
//		   log.debug ( 'TaskService.taskInstance.errors: ' +  taskInstance.errors )
			result.error = taskInstance.errors
			
			log.debug ( 'TaskService.taskInstance.errors: ' +  taskInstance.errors )
			log.debug ( 'TaskService. result.error : ' +  result.error )
			throw new RuntimeException ( result.error.toString() ) ;
	   }
	  
	   log.debug ( 'PRE--save ')
	 
	   
		if (!taskInstance.save( flush:true ) ) {
			log.error ( '!taskInstance.save: ' + taskInstance.errors )
			result.error ="Fallo el grabado de Tiempos: " +   taskInstance.errors
			
			//return (result)
			throw new RuntimeException(result)
		}
		
	   result.taskInstance = taskInstance
	   log.debug ( 'taskService.save(): - SUCCESS')
	   // Success.
	   return result
   }
   
}
