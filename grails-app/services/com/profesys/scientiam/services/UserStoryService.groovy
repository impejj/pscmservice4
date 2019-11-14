package com.profesys.scientiam.services

import com.profesys.scientiam.configuration.Enumeration
import com.profesys.scientiam.configuration.StatusItem
import com.profesys.scientiam.configuration.StatusType
import com.profesys.scientiam.configuration.uom.Uom
import com.profesys.scientiam.erp.party.Party
import com.profesys.scientiam.pm.Project
import com.profesys.scientiam.pm.userStory.UserStory
import com.profesys.scientiam.pm.work.*
import com.profesys.scientiam.workspace.Ws_issue
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.sql.GroovyRowResult
import org.grails.web.util.WebUtils

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.text.SimpleDateFormat
import groovy.util.logging.Log4j


@Transactional(readOnly = false)
class UserStoryService {

	def TransactionService
    def WorkEffortService


	def listByUserStory(WorkEffort workEffort) {
		def result = [:]
		def fail = { Map m ->
			result.error = [ code: m.code, args: ["UserStory"] ]
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
		
		
			log.error ('workEffort:' +workEffort.properties)
			log.error ('workEffort.id:' +workEffort.id)
			log.error ('result.workEffortAssocInstanceList : ' + result.workEffortAssocInstanceList  )
			log.error ('result.workEffortAssocInstanceTotal : ' +result.workEffortAssocInstanceTotal  )
//        if(!result.noteInstanceList || !result.noteInstanceTotal)
//            return fail(code:"default.list.failure")
 
		// Success.
		return result
	}



	def  java.util.LinkedHashMap getMyUserStories(params) {
		//Devuelvo todos los proyectos en los que intervengo

		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session

		String proyectoId =  params.projectSelection
		log.error ( 'Tengo id de proyecto?:' + params )
		log.error ( 'Tengo proyectoId?:' + proyectoId )
		log.error ( 'Tengo proyectoId?:' + params.projectSelection )


		String projectViewId = params.id
		if (projectViewId){
			proyectoId = projectViewId

		}
		if ( params.projectSelection){
			proyectoId =  params.projectSelection

		}
		/*
        INICIO - DEFINICION las Variables fecha
         */
		Calendar fechaHasta = Calendar.getInstance();
		fechaHasta.add(Calendar.DAY_OF_YEAR, 365);

		Calendar fechaDesde = Calendar.getInstance();
		fechaDesde.add(Calendar.DAY_OF_YEAR, -365);
		def projectSelection = Long.valueOf(0)
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		String fechaThru =  fechaHasta.get(Calendar.DAY_OF_MONTH )+'/'+ ( fechaHasta.get(Calendar.MONTH)+1 )+'/'+ fechaHasta.get(Calendar.YEAR)
		String fechaFrom =  fechaDesde.get(Calendar.DAY_OF_MONTH )+'/'+ ( fechaDesde.get(Calendar.MONTH)+1 )+'/'+ fechaDesde.get(Calendar.YEAR)

		Date pfechaHasta =   sdf.parse(fechaThru);
		Date pfechaDesde =   sdf.parse(fechaFrom) ;

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
		def textoDesde = " Fecha Desde: "+ fechaDesde.get(Calendar.DAY_OF_MONTH)+'/'+ ( fechaDesde.get(Calendar.MONTH)+1 )+'/'+ fechaDesde.get(Calendar.YEAR)
		def textoHasta = " Hasta: "+ fechaHasta.get(Calendar.DAY_OF_MONTH)+'/'+ (fechaHasta.get(Calendar.MONTH)+1 )+'/'+ fechaHasta.get(Calendar.YEAR)
		params.fechaDesde = fechaFrom
		params.fechaHasta = fechaThru
		/*
        FIN - DEFINICION las Variables fecha

        */

		/*
           INICIO - DEFINICION valores Filtro de Estado
           */

		// def textoStatus = " - Estados:   En Progreso, En Planificación ] "
		def statusFilter
		if (params.itemStatusForm){

			statusFilter =  StatusItem.getAll ( params.itemStatusForm )

		}else{

			statusFilter =  StatusItem.getAll ( 'USSS_IN_PROGRESS','USSS_PLANNING')

		}
		  def textoStatus = " - Estados: " +  statusFilter


		log.error ('textoStatus: ' +textoStatus)
		//


		def projectInstance = Project.get ( proyectoId)
		/*
		FIN- DEFINICION valores Filtro de Estado
		*/
		def textoProyecto
		if ( projectInstance ){
			textoProyecto = "- Proyecto: " + projectInstance
		}else{
			textoProyecto = " - Proyectos: Todos"
		}

		def leyendaAnterior = textoDesde + textoHasta + textoStatus + textoProyecto

		def entryCriteria = UserStory.createCriteria()



		def userStoryInstanceList

		userStoryInstanceList = entryCriteria.list {
			workEffort {
				if(projectInstance){ eq("project", projectInstance )}
				gt("state", 0)
				if (statusFilter)
				{  "in" ("currentStatus", statusFilter  )	}
			}
			and {
				gt("state", 0)

			}


		}
		log.debug(' projectInstance: ' + projectInstance)
		log.debug(' userStoryInstanceList: ' + userStoryInstanceList)

		log.debug ( 'fechaFrom: ' + fechaFrom)
		log.debug ( 'fechaThru: ' + fechaThru)

		def result = [:]
		result.pfechaDesde = fechaFrom
		result.pfechaHasta = fechaThru


		result.statusFilter = statusFilter
//		result.projectTypeFilter = projectTypeFilter
//		result.planningTypeFilter =planningTypeFilter
		result.itemStatusList =  StatusItem.findAllByStatusType(StatusType.get('USER_STORY_STATUS'))

		// result.leyendaAnterior2 = leyendaAnterior2
		result.leyendaAnterior = leyendaAnterior
//		result.projectTypeList = ProjectType.findAllByStateGreaterThan(0)
//		result.projectStatusList = StatusItem.findAllByStatusType(StatusType.get('WE_PROJECT_STATUS'))
//		result.planningTypeList	 =  WorkEffortType.findAllByWorkEffortTypeRoot(WorkEffortType.get('STRATEGIC_PLANNING'))
		result.userStoryInstanceList = userStoryInstanceList
		if ( projectInstance ) { result.projectInstanceId = projectInstance.id
			}



		return  result

	}


   @Transactional
   def saveUserStory(WorkEffort workEffortInstance, UserStory userStoryInstance) {

	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   log.debug ( '*****-INGRESO A saveUserStory - *******: ' )

	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["userStoryInstance"] ]
		   return result
	   }
	   if (workEffortInstance == null) {
//		   notFound()
		  return fail(code:"Esfuerzo de Tarea con Valores Nulos")
	   }
	   if (userStoryInstance == null) {
		   //		   notFound()
					 return fail(code:" Tarea con Valores Nulos")
				  }
		   //
	   log.debug ( ' ***************PRE-WorkEffortService.save'  )
	   workEffortInstance.workEffortType = WorkEffortType.read("USER_STORY")



	    workEffortInstance.workEffortPurposeType  = WorkEffortPurposeType.read('WEPT_ANALYSIS_DESIGN')
//	   workEffortInstance.code 			 = userStoryInstance.code
	   workEffortInstance.actualCompletionDate= null
	   workEffortInstance.actualStartDate = null
	   workEffortInstance.scopeEnum = Enumeration.read('WES_PUBLIC')
	   workEffortInstance.estimatedCompletionDate= null
	   workEffortInstance.estimatedStartDate=null
	   workEffortInstance.percentCompleted = 0
	   workEffortInstance.currentStatus= StatusItem.get ('USER_STORY_PLANNING')
	   workEffortInstance.moneyUom =  Uom.read ('ARS')



	   result = WorkEffortService.save  (  workEffortInstance )
	   log.debug ('userStoryService.saveuserStory().WorkEffortService.save.properties='+result.properties)
	   log.debug ('userStoryService.saveuserStory().WorkEffortService.save.result='+result)
	   log.debug ('userStoryService.saveuserStory().WorkEffortService.save.result='+result.error)

	   if (!result.workEffortInstance) {
		   log.debug ( 'FALLO LA VALIDACION!  -->!workEffortInstance.save.result: ' + result )
		   throw new RuntimeException(result.error.toString())
//		   return  ( result )
		 }else{
			 log.debug ('result.workEffortInstance.id: ' + result.workEffortInstance.id )
			 userStoryInstance.userUpdated          =   session.user
			 userStoryInstance.state     		   =  2
			 log.debug ('result-pre. userStoryInstance.properties  : ' +  userStoryInstance.properties   )
		     if (!userStoryInstance.id){
				 //Es un registro nuevo y tengo que insertar los valores de id y ticket

		    	 userStoryInstance.userCreated     = session.user
		    	 userStoryInstance.id      		   = result.workEffortInstance.id
				 userStoryInstance.workEffort 	   = result.workEffortInstance
				 userStoryInstance.state             = 1
				 //userStoryInstance.status 			= null

				 log.debug ('result. userStoryInstance.id          : ' +  userStoryInstance.id           )
				 log.debug ('result. userStoryInstance.properties  : ' +  userStoryInstance.properties   )
				 log.debug ('result. userStoryInstance.workEffort  : ' +  userStoryInstance.workEffort.properties  )
			 }
		 }


	     log.debug ( 'PRE--userStoryInstance.hasErrors: ')

	   if (!userStoryInstance.validate()) {
//		   log.debug ( 'UserStoryService.userStoryInstance.errors: ' +  userStoryInstance.errors )
		    result.error = userStoryInstance.errors

			log.debug ( 'UserStoryService.userStoryInstance.errors: ' +  userStoryInstance.errors )
			log.debug ( 'UserStoryService. result.error : ' +  result.error )
			throw new RuntimeException ( result.error.toString() ) ;
	   }

	   log.debug ( 'PRE--save ')


		if (!userStoryInstance.save( flush:true ) ) {
			log.error ( '!userStoryInstance.save: ' + userStoryInstance.errors )
			result.error ="Fallo el grabado de Tareas: " +   userStoryInstance.errors

			//return (result)
			throw new RuntimeException(result)
		}

	   result.userStoryInstance = userStoryInstance
	   log.debug ( 'userStoryService.save(): - SUCCESS')
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
   def save(UserStory userStoryInstance) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   log.debug ( '*****-INGRESO A saveUserStory - *******: ' )

	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["userStoryInstance"] ]
		   return result
	   }
	   def workEffortInstance = userStoryInstance.workEffort
	   if (workEffortInstance == null) {
//		   notFound()
		  return fail(code:"Esfuerzo de Tarea con Valores Nulos")
	   }
	   log.debug ('userStoryInstance.workEffort.percentCompleted: ' + userStoryInstance.workEffort.percentCompleted )
	   if (userStoryInstance == null) {
		   //		   notFound()
					 return fail(code:" Tarea con Valores Nulos")
				  }
		   //

	   /*
	    * Cago valores especificos del workEffort
	    */
	   /*Tengo
         que verificar que no venga con un valor pre-existente del formulario porque estoy forzando
         USER_STORY y ahora tengo muchas opciones
       */
		if (! workEffortInstance.workEffortType ){
			workEffortInstance.workEffortType = WorkEffortType.read("USER_STORY")

		}
	   /*
         Verificar el currentStatus porque esta dando problemas
       */
	   if (! workEffortInstance.currentStatus ){
		   workEffortInstance.currentStatus = WorkEffortType.read(StatusItem.get("USSS%") )

	   }

	   log.error ('userStoryService.saveuserStory().WorkEffortService.save.properties='+ result.properties)
		workEffortInstance.clearErrors()
	   result = WorkEffortService.save  (  workEffortInstance )
	   log.debug ('userStoryService.saveuserStory().WorkEffortService.save.properties='+ result.properties)
	   log.debug ('userStoryService.saveuserStory().WorkEffortService.save.result='    + result)
	   log.debug ('userStoryService.saveuserStory().WorkEffortService.save.result='    + result.error)

	   if (!result.workEffortInstance) {
		   log.debug ( 'FALLO LA VALIDACION!  -->!workEffortInstance.save.result: ' + result )
		   throw new RuntimeException(result.error.toString())
//		   return  ( result )
		 }else{
			 log.debug ('result.workEffortInstance.id: ' + result.workEffortInstance.id )
			 userStoryInstance.userUpdated          =   session.user
			 userStoryInstance.state     		   =  2
			 log.debug ('result-pre. userStoryInstance.properties  : ' +  userStoryInstance.properties   )
		     if (!userStoryInstance.id){
				 //Es un registro nuevo y tengo que insertar los valores de id y ticket

		    	 userStoryInstance.userCreated       = session.user
		    	 userStoryInstance.id      		   = result.workEffortInstance.id
				 userStoryInstance.workEffort 	   = result.workEffortInstance
				 userStoryInstance.state             = 1


				 log.debug ('result. userStoryInstance.id          : ' +  userStoryInstance.id           )
				 log.debug ('result. userStoryInstance.properties  : ' +  userStoryInstance.properties   )
				 log.debug ('result. userStoryInstance.workEffort  : ' +  userStoryInstance.workEffort.properties  )
			 }
		 }


	     log.debug ( 'PRE--userStoryInstance.hasErrors: ')

	   if (!userStoryInstance.validate()) {
//		   log.debug ( 'UserStoryService.userStoryInstance.errors: ' +  userStoryInstance.errors )
		    result.error = userStoryInstance.errors

			log.debug ( 'UserStoryService.userStoryInstance.errors: ' +  userStoryInstance.errors )
			log.debug ( 'UserStoryService. result.error : ' +  result.error )
			throw new RuntimeException ( result.error.toString() ) ;
	   }

	   log.debug ( 'PRE--save ')


		if (!userStoryInstance.save( flush:true ) ) {
			log.error ( '!userStoryInstance.save: ' + userStoryInstance.errors )
			result.error ="Fallo el grabado de Tareas: " +   userStoryInstance.errors

			//return (result)
			throw new RuntimeException(result)
		}

	   result.userStoryInstance = userStoryInstance
	   log.debug ( 'userStoryService.save(): - SUCCESS')
	   // Success.
	   return result
   }


   @Transactional
   def convert(String userStoryId) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   def userStoryInstance  = new UserStory()
	   def workEffortInstance
	   def userStoryOriginal  = UserStory.read(userStoryId)

	   log.debug ( 'userStoryOriginal.properties: ' + userStoryOriginal.properties )
	   def workEffortOriginal = WorkEffort.read ( userStoryOriginal.workEffort.id  )


	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["userStoryInstance"] ]
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
		 log.debug ( "***********Creo y cargo el userStoryInstance*************")
		 userStoryInstance       = new UserStory (  )
		 	//


	   if (workEffortInstance){

		 //  def workEffortInstance = new WorkEffort()
//		   workEffortInstance.workEffortName = 'Generación por Tarea de usuario'
		   workEffortInstance.workEffortPurposeType  = WorkEffortPurposeType.read('WEPT_ANALYSIS_DESIGN')
		   workEffortInstance.workEffortType = WorkEffortType.read("USER_STORY")
		   workEffortInstance.id  =  TransactionService.serviceID('USER_STORY',session.user.toString() )
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
			    throw new ValidationException("Errores de Validacion : ", userStoryInstance.errors)
			}
	   }
	   if ( userStoryInstance ){
				   //Si grabé el workEffort es porque es nuevo...y tengo que actualizar la instancia de userStoryInstance
				   log.debug ('workEffortInstance.id: ' + workEffortInstance.id)
				   //Cargo los datos de userStory
				   //	   userStoryInstance.version                     =     userStoryOriginal.version
				   //	   userStoryInstance.date_created                =     userStoryOriginal.date_created
				   //	   userStoryInstance.date_deleted                =     userStoryOriginal.date_deleted
				  userStoryInstance.detail                      =     userStoryOriginal.detail

				  //userStoryInstance.last_updated               =     userStoryOriginal.last_updated

				  userStoryInstance.complexity                  =     userStoryOriginal.complexity
				  userStoryInstance.level                       =     userStoryOriginal.level
				  userStoryInstance.urgency                     =     userStoryOriginal.urgency
				 // userStoryInstance.userStorySituation	           =     ''
				 // userStoryInstance.delegated_to                =     userStoryOriginal.delegated_to
				 // userStoryInstance.dueDate                    =     userStoryOriginal.dueDate
				  userStoryInstance.energy                      =     userStoryOriginal.energy
//				  userStoryInstance.follow_up_date              =     userStoryOriginal.follow_up_date
				  userStoryInstance.idOrigen                   =     userStoryOriginal.id
				  // userStoryInstance.userStoryExcecutionType         =     UserStoryExecutionType.get ('ASAP')
//				  userStoryInstance.projectTaxonomy        		=     userStoryOriginal.projectTaxonomy
//				  userStoryInstance.description                 =     userStoryOriginal.description
//				  userStoryInstance.project                     =     userStoryOriginal.project
//				  userStoryInstance.status                  	   =     userStoryOriginal.status
//				  userStoryInstance.code                        =     userStoryOriginal.code
				  log.debug ('pre id?')
				   userStoryInstance.workEffort      =  WorkEffort.get ( workEffortInstance.id )
				   userStoryInstance.id              =  workEffortInstance.id
				     userStoryInstance.userCreated     =  session.user
				   userStoryInstance.userUpdated     =  session.user
				   userStoryInstance.state           = 1


	   }

//	   log.debug  ( "workEffortInstance : " +workEffortInstance.properties )
//	   log.debug  ( "workEffortOriginal.properties : " +workEffortOriginal.properties )
//	   log.debug  ( "workEffortOriginal.code : " +workEffortOriginal.code )
//	   log.debug  ( "workEffortInstance.code : " +workEffortInstance.code )
//	   log.debug  ( "userStoryInstance.properties : " +userStoryInstance.properties )

		 if (!userStoryInstance.validate()) {
		     log.debug ( 'Tarea con Errores: ' + userStoryInstance.errors )
			 return fail(code:"Tarea con Errores"+ userStoryInstance.errors)

		 }
		   log.debug ('userStoryservice-validate: No tiene errores')

		   if (!userStoryInstance.save( flush:true)) {
			   log.debug ( '!userStoryInstance.save: ' + userStoryInstance.errors )
//			   throw new RuntimeException("Fallo el grabado de userStoryInstance")
//			   return fail(code:"Fallo el grabado de userStoryInstance"+ userStoryInstance.errors)
			   throw new ValidationException("Errores de Validacion: ", userStoryInstance.errors)
		   }

		   log.debug ('userStoryservice-save: sale por la opcion de resultado exitoso')
		   result.userStoryInstance = userStoryInstance

	   //Primero tengo que hacer el cambio de Estado de workEffortOrigen y su userStory
	   // y despues crear el registro de relación

	   def workEffortAssocInstance = new WorkEffortAssoc()
	   workEffortAssocInstance.id  =  TransactionService.serviceID('WEAI',session.user.toString() )

	   workEffortAssocInstance.workEffortIdFrom 	= userStoryOriginal.workEffort
	   workEffortAssocInstance.workEffortIdTo   	= userStoryInstance.workEffort
	   workEffortAssocInstance.workEffortAssocType	= WorkEffortAssocType.get( 'WORK_EFF_STORY_USER_STORY' )
	   workEffortAssocInstance.sequenceNum			= 10
	   workEffortAssocInstance.fromDate				= new Date()
//	   workEffortAssocInstance.thruDate				=

	   workEffortAssocInstance.state   				=	1
	   workEffortAssocInstance.userCreated 			= session.user
	   workEffortAssocInstance.userUpdated 			= session.user


	   if (!workEffortAssocInstance.save( flush:true)) {
		   log.debug ( '!workEffortAssocInstance.save: ' + workEffortAssocInstance.errors )
//			   throw new RuntimeException("Fallo el grabado de userStoryInstance")
//			   return fail(code:"Fallo el grabado de userStoryInstance"+ userStoryInstance.errors)
		   throw new ValidationException("Errores de Validacion: ", workEffortAssocInstance.errors)
	   }

	   log.debug ( '!workEffortAssocInstance.save: ' + workEffortAssocInstance.errors )
	   log.debug ('userStoryservice-save: sale por la opcion de resultado exitoso')
	   result.workEffortAssocInstance = workEffortAssocInstance
   // Success.
   return result
   }

   def createFromWs_issue(String ws_issueId) {

	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   def userStoryInstance  = new UserStory()
	   def workEffortInstance
	   def ws_issueInstance  = Ws_issue.read(ws_issueId)

	   log.debug ( 'ws_issueInstance.properties: ' + ws_issueInstance.properties )
	   //def workEffortOriginal = WorkEffort.read ( ws_issueInstance.workEffort.id  )


	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["userStoryInstance"] ]
		   return result
	   }


	   log.debug ( 'Verifico ws_issueInstance' )
	   if (ws_issueInstance == null) {
		   //		   notFound()
					  log.debug ( 'Ws_issue de Origen Inexistente' )
					 return fail(code:"Issue de Origen Inexistente")

	   }
		 //Lo marco como modificación porque si entro luego a la creación de workEffort verificaríamos que es nuevo y lo pisa con estado 1

		 //Creo el registro de workEffort
		 log.debug ( "***********Creo el workEffort*************")
		 workEffortInstance = new WorkEffort ()
		 log.debug ( "***********Creo y cargo el userStoryInstance*************")
		 userStoryInstance       = new UserStory (  )
		 	//


	   if (workEffortInstance){

		 //  def workEffortInstance = new WorkEffort()
//		   workEffortInstance.workEffortName = 'Generación por Tarea de usuario'
		   workEffortInstance.workEffortPurposeType  = WorkEffortPurposeType.read('WEPT_ANALYSIS_DESIGN')
		   workEffortInstance.workEffortType = WorkEffortType.read("USER_STORY")
		   workEffortInstance.id  =  TransactionService.serviceID('USER_STORY',session.user.toString() )
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
//		   if ( userStoryInstance ) {
//			   workEffortInstance.dueDate = ws_issueInstance.workEffort.dueDate
//		   }
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
		   log.debug ( 'Esfuerzo con Errores: ' + workEffortInstance.errors )
		   return fail(code:"Esfuerzo con Errores"+ workEffortInstance.errors)

	   }else{
		   log.debug ('workEffortInstance-save: No tiene errores ')
		   if (!workEffortInstance.save( flush:true )) {
			   log.debug ( '!workEffortInstance.save: ' + workEffortInstance.errors )
			    throw new ValidationException("Errores de Validacion : ", userStoryInstance.errors)
			}
	   }
	   if ( userStoryInstance ){
				   //Si grabé el workEffort es porque es nuevo...y tengo que actualizar la instancia de userStoryInstance
				   log.debug ('workEffortInstance.id: ' + workEffortInstance.id)
				   //Cargo los datos de userStory
				   //	   userStoryInstance.version                     =     ws_issueInstance.version
				   //	   userStoryInstance.date_created                =     ws_issueInstance.date_created
				   //	   userStoryInstance.date_deleted                =     ws_issueInstance.date_deleted
				  userStoryInstance.detail                      =     ws_issueInstance.detail

				  //userStoryInstance.last_updated               =     ws_issueInstance.last_updated
		          userStoryInstance.relativePoints				= 0
				  userStoryInstance.complexity                  =     ws_issueInstance.complexity
				  userStoryInstance.level                       =     ws_issueInstance.priority
				  userStoryInstance.urgency                     =     ws_issueInstance.urgency
				 // userStoryInstance.userStorySituation	           =     ''
				 // userStoryInstance.delegated_to                =     ws_issueInstance.delegated_to
 				  userStoryInstance.energy                      =     10
//				  userStoryInstance.follow_up_date              =     ws_issueInstance.follow_up_date
				  userStoryInstance.idOrigen                   =     ws_issueInstance.id
				 // userStoryInstance.userStoryExcecutionType         =     UserStoryExecutionType.get ('ASAP')
//				  userStoryInstance.description                 =     ws_issueInstance.description
//				  userStoryInstance.projectTaxonomy        		=     ws_issueInstance.projectTaxonomy
//				  userStoryInstance.project                     =     ws_issueInstance.project
//				  userStoryInstance.status                  	   =     ws_issueInstance.status
//				  userStoryInstance.code                        =     workEffortInstance.code
				  log.debug ('pre id?')
				   userStoryInstance.workEffort      =  WorkEffort.get ( workEffortInstance.id )
				   userStoryInstance.id              =  workEffortInstance.id
				     userStoryInstance.userCreated     =  session.user
				   userStoryInstance.userUpdated     =  session.user
				   userStoryInstance.state           = 1


	   }

	   log.debug  ( "workEffortInstance : " +workEffortInstance.properties )
	   log.debug  ( "ws_issueInstance.properties : " +ws_issueInstance.properties )
	   log.debug  ( "workEffortInstance.code : " +workEffortInstance.code )
	   log.debug  ( "userStoryInstance.properties : " +userStoryInstance.properties )

		 if (!userStoryInstance.validate()) {
		     log.debug ( 'Tarea con Errores: ' + userStoryInstance.errors )
			 return fail(code:"Tarea con Errores"+ userStoryInstance.errors)

		 }
		   log.debug ('userStoryservice-validate: No tiene errores')

		   if (!userStoryInstance.save( flush:true)) {
			   log.debug ( '!userStoryInstance.save: ' + userStoryInstance.errors )
//			   throw new RuntimeException("Fallo el grabado de userStoryInstance")
//			   return fail(code:"Fallo el grabado de userStoryInstance"+ userStoryInstance.errors)
			   throw new ValidationException("Errores de Validacion: ", userStoryInstance.errors)
		   }

		   log.debug ('userStoryservice-save: sale por la opcion de resultado exitoso')
		   result.userStoryInstance = userStoryInstance

//	   //Primero tengo que hacer el cambio de Estado de workEffortOrigen y su userStory
//	   // y despues crear el registro de relación
//


	   //Cambio el estado de ws_issue para darlo como cerrado y no aparezca mas como activo.
	   ws_issueInstance.state				 = 0
	   ws_issueInstance.realizedDate		= new Date()
	   ws_issueInstance.statusClosed		= 1

	   if (!ws_issueInstance.save( flush:true)) {
		   log.debug ( '!ws_issueInstance.save: ' + ws_issueInstance.errors )
//			   throw new RuntimeException("Fallo el grabado de userStoryInstance")
//			   return fail(code:"Fallo el grabado de userStoryInstance"+ userStoryInstance.errors)
		   throw new ValidationException("Errores de Validacion: ", ws_issueInstance.errors)
	   }
   // Success.
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
		
			   def userStorytype = WorkEffortType.get('USER_STORY')
			   def userStoryStatusCompleted = StatusItem.get( 'WEPR_COMPLETE')
			   log.debug ('userStorytype: ' + userStorytype)
			   //  def results = WorkEffortPartyAssignment.where {
			   //	  workEffort.workEffortType == proyectos &&  state > 0   && party in userPartyRoles
			   //  }.list()

			   //Tareas asignadas de los proyectos donde estoy asignado

				  def resultsWEPAProjectsUserStorys = WorkEffortPartyAssignment.where {
				   workEffort.workEffortType == userStorytype &&
				   workEffort.project in  resultsWEPAProjects.workEffort.project &&
				    state > 0   && party in userPartyRoles
			   }.list()
			   
			    log.debug ('resultsWEPAProjectsUserStorys: ' + resultsWEPAProjectsUserStorys)
				log.debug ('resultsWEPAProjectsUserStorys.workEffort: ' + resultsWEPAProjectsUserStorys.workEffort)
				log.debug ('userStoryStatusCompleted : ' + userStoryStatusCompleted)
				
				def results = UserStory.where {
				   workEffort.workEffortType == userStorytype &&
				    state > 0  &&
				     workEffort.currentStatus != userStoryStatusCompleted
				}.list()
				
				// &&
				//				   workEffort.project in  resultsWEPAProjects.workEffort.project &&
				//				   workEffort  { not { 'in' resultsWEPAProjectsUserStorys.workEffort  } }
				log.debug ('results: ' + results)
				
	   return  results
	   
   }
   
   
   @Transactional
   def updateTime(UserStory userStoryInstance) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	    
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["userStoryInstance"] ]
		   return result
	   }
	   
			 userStoryInstance.userUpdated          =   session.user
			 userStoryInstance.state     		   =  2
 
	   
	   if (!userStoryInstance.validate()) {
//		   log.debug ( 'UserStoryService.userStoryInstance.errors: ' +  userStoryInstance.errors )
			result.error = userStoryInstance.errors
			
			log.debug ( 'UserStoryService.userStoryInstance.errors: ' +  userStoryInstance.errors )
			log.debug ( 'UserStoryService. result.error : ' +  result.error )
			throw new RuntimeException ( result.error.toString() ) ;
	   }
	  
	   log.debug ( 'PRE--save ')
	 
	   
		if (!userStoryInstance.save( flush:true ) ) {
			log.error ( '!userStoryInstance.save: ' + userStoryInstance.errors )
			result.error ="Fallo el grabado de Tiempos: " +   userStoryInstance.errors
			
			//return (result)
			throw new RuntimeException(result)
		}
		
	   result.userStoryInstance = userStoryInstance
	   log.debug ( 'userStoryService.save(): - SUCCESS')
	   // Success.
	   return result
   }

	def getWorkEffortTypeRequirement() {
		def result = [:]
		def fail = { Map m ->
			result.error = [ code: m.code, args: ["workEffortInstance"] ]
			return result
		}

		log.error ('result: ' + result )

		def reqType =  WorkEffortType.get('CAPTURE_REQUIREMENT')

		log.error ('reqType: ' + reqType )

		def entryCriteria = WorkEffortType.createCriteria()

		result.workEffortTypeListInstance  = entryCriteria.list {
			and{
				'in' ("workEffortTypeRoot",reqType)
				gt("state",0)
			}


		}

		// Success.
		return result
	}


	def  ArrayList<GroovyRowResult> getActiveSprintsByProject(String projectId) {
		//Devuelvo todos los sprints Activos ( en planificacion o ejecucion )

		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session

		log.error ('service.params: ' + projectId )


		def workEffortIterationList  = WorkEffortType.where {
			workEffortTypeRoot == WorkEffortType.get('ITERATION')

			state > 0


		}.list()
		log.error ('workEffortIterationList: '+ workEffortIterationList)

		def statusItemIterationList  = StatusItem.where {
			id in [ 'ITERATION_PLANNING' ,  'ITERATION_IN_PROGRESS' ]
		}.list()

		log.error ('statusItemIterationList: '+ statusItemIterationList)

		def entrySprintCriteria = WorkEffort.createCriteria()
		def activeSprintList = entrySprintCriteria.list {
			and {
				'in' ("workEffortType",
						workEffortIterationList
				)
				'in' ("currentStatus",

						statusItemIterationList
				)

			}

			eq (  'project' , Project.get (projectId) )
			gt("state", 0)

		}


		log.error ('activeSprintList: '+ activeSprintList)
//		log.error ('partyRoleList.party: '+ partyRoleList.party.id)

		//def partyInstanceList= Party.findAllById(partyRoleList.party)

//
//		def partyInstanceList = Party.where {
//			id in partyRoleList.party.id
//			state > 0
//		}


		//return  partyInstanceList.list()
		return activeSprintList
	}

	def listUserStoryBySprint(WorkEffort workEffort) {
		def result = [:]
		def fail = { Map m ->
			result.error = [code: m.code, args: ["UserStory"]]
			return result
		}

		//Dependiendo que el pedido venga por sprint o por requerimiento
		//varia el tipo de consulta que hacemos


//
		def workEffortAssocTypeInstance = WorkEffortAssocType.get('WORK_EFF_SPRINT')
		def entryCriteria = WorkEffortAssoc.createCriteria()

		result.workEffortAssocInstanceList  = entryCriteria.list {

			and{

				if (workEffort.workEffortType.workEffortTypeRoot == WorkEffortType.get('ITERATION')) {

					eq("workEffortIdTo",workEffort)

				} else {

					eq("workEffortIdFrom",workEffort)


				}
				eq("workEffortAssocType",workEffortAssocTypeInstance )
				gt("state",0)
			}


		}

		// params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
		//result.noteInstanceList  = Book.list(params)
		result.workEffortAssocInstanceTotal = result.workEffortAssocInstanceList.size()


		log.error ('workEffort:' + workEffort.properties)
		log.error ('workEffort.id:' +workEffort.id)
		log.error ('workEffortTypeInstance :' + workEffortAssocTypeInstance)
		log.error ('result.workEffortAssocInstanceList : ' + result.workEffortAssocInstanceList  )
		log.error ('result.workEffortAssocInstanceTotal : ' +result.workEffortAssocInstanceTotal  )
//        if(!result.noteInstanceList || !result.noteInstanceTotal)
//            return fail(code:"default.list.failure")

		// Success.
		return result
	}


	def listTasksByUserStory(WorkEffort workEffort) {
		def result = [:]
		def fail = { Map m ->
			result.error = [code: m.code, args: ["UserStory"]]
			return result
		}

		//Dependiendo que el pedido venga por sprint o por requerimiento
		//varia el tipo de consulta que hacemos


//
		def workEffortAssocTypeInstance = WorkEffortAssocType.get('WORK_EFF_REQ_TASK')
		def entryCriteria = WorkEffortAssoc.createCriteria()

		def workEffortAssocInstanceList  = entryCriteria.list {

			workEffortIdTo {
				gt("state",0)
			}
			and{
				eq("workEffortIdFrom",workEffort)
				eq("workEffortAssocType",workEffortAssocTypeInstance )
				gt("state",0)
			}


		}

		// params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
		//result.noteInstanceList  = Book.list(params)
//		result.workEffortAssocInstanceTotal = result.workEffortAssocInstanceList.size()
		def workEffortToList = workEffortAssocInstanceList.workEffortIdTo

		log.error ("workEffortToList: "+workEffortToList )

		if ( workEffortToList) {
			result.taskInstanceList = Task.where {

				workEffort in workEffortToList

			}.list()

		}
		// Success.
		return result
	}

}
