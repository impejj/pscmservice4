package com.profesys.scientiam.services

import com.profesys.scientiam.configuration.StatusItem
import com.profesys.scientiam.resource.DataResource
import com.profesys.scientiam.resource.DataResourceType
import com.profesys.scientiam.resource.Reference
import com.profesys.scientiam.resource.ReferenceItem
import com.profesys.scientiam.resource.ReferenceType
import com.profesys.scientiam.workspace.Ws_issue

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.sql.GroovyRowResult
import org.grails.web.util.WebUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.text.SimpleDateFormat
import groovy.util.logging.Log4j



@Transactional
class ReferenceService {
	
	def TransactionService

   def list(String ticketParam) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
        def result = [:]
        def fail = { Map m ->
            result.error = [ code: m.code, args: ["Ws_issue"] ]
            return result
        }
		
		def entryCriteria = Ws_issue.createCriteria()
		
		result.referenceInstanceList  = entryCriteria.list {
			and{
			  eq("ticket",ticketParam)
			  gt("state", 0)
			 
			}
			    
	   
		  }
       // params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        //result.referenceInstanceList  = Book.list(params)
        result.referenceInstanceTotal = result.referenceInstanceList.size()
 
			log.debug ('result.referenceInstanceList : ' +result.referenceInstanceList  )
			log.debug ('result.referenceInstanceTotal : ' +result.referenceInstanceTotal  )
//        if(!result.referenceInstanceList || !result.referenceInstanceTotal)
//            return fail(code:"default.list.failure")

        // Success.
        return result
    }


	def  getMyReferences() {

		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session

		def result = [:]


		def resultReferenceList = Reference.where {
			userCreated in session.user &&
					state > 0

		}.list(sort:"dataResource.description" )


//		def entryCriteria = Reference.createCriteria()
//
//		def resultReferenceItems  = entryCriteria.list {
//			and{
//				&&   party in userPartyRoles
//				eq("reference",referenceInstance)
//				gt("state", 0)
//
//			}
//		}
		//result.resultReferenceList = results

		return resultReferenceList

	}





	def  listReferenceItems(Reference referenceInstance) {

		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session

		def result = [:]
		def entryCriteria = ReferenceItem.createCriteria()

		def resultReferenceItems  = entryCriteria.list {
			and{
				eq("reference",referenceInstance)
				gt("state", 0)

			}
		}
		result.resultReferenceItemsList = resultReferenceItems
		return resultReferenceItems

	}
 
   
   @Transactional
   def save(Reference referenceInstance) {
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["Ws_issue"] ]
		   return result
	   }
	   log.debug ( 'referenceInstance.properties: ' +referenceInstance.properties)
	   if (referenceInstance == null) {
//		   notFound()
		  return fail(code:"Nota inexistente")
	   }
	   
	   if (!referenceInstance.id) {
		   //		   notFound()
		 //  referenceInstance.id   	     =  TransactionService.serviceID('WSISSUE',session.user.toString() )
		   referenceInstance.id   	     =  TransactionService.getUIID()
				  }
				  
	   if (!referenceInstance.hasErrors()) {
		   

		   referenceInstance.save flush:true
	   }
	   
	   result.referenceInstance = referenceInstance
	   
	   // Success.
	   return result
	   }

	@Transactional
	def  java.util.LinkedHashMap saveQuickReference(params) {

		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session

		def result = [:]

		def dataResourceInstance = new DataResource()
		def referenceInstance = new Reference(params)

		referenceInstance.dataResource = dataResourceInstance
		referenceInstance.referenceType = ReferenceType.get ( params.referenceType)


		if (referenceInstance == null) {
			result.error= "No existe instancia de Referencia"
			return result
		}

		referenceInstance.dataResource.id = TransactionService.serviceID('REFE',session.user.toString() )

		referenceInstance.dataResource.state = 1
		referenceInstance.dataResource.userCreated = session.user
		referenceInstance.dataResource.userUpdated = session.user
		referenceInstance.dataResource.dataResourceType= DataResourceType.get('REFERENCE')
		 referenceInstance.dataResource.isPublic='N'
		referenceInstance.dataResource.statusItem = StatusItem.get('DARE_HOLD')
		referenceInstance.dataResource.ticket = referenceInstance.dataResource.id

		if (!referenceInstance.dataResource.save(flush:true)) {

			result.error= referenceInstance.dataResource.errors

			return result

		}


		referenceInstance.id  = referenceInstance.dataResource.id

		referenceInstance.state = 1
		referenceInstance.userCreated = session.user
		referenceInstance.userUpdated = session.user

		if (referenceInstance.hasErrors()) {

			result.error= referenceInstance.errors

			return result
		}

		referenceInstance.save flush:true


 		result.referenceInstance = referenceInstance
//		result.pfechaHasta = fechaThru
//		result.statusFilter = statusFilter
//		result.leyendaAnterior2 = leyendaAnterior2
//		result.leyendaAnterior = leyendaAnterior
//		result.projectTypeList = ProjectType.findAllByStateGreaterThan(0)
//		result.itemStatusList =  StatusItem.findAllByStatusType(StatusType.get('TASK_STATUS'))
//		result.taskInstanceList = results
//		result.planningTypeList	 =  WorkEffortType.findAllByWorkEffortTypeRoot(WorkEffortType.get('STRATEGIC_PLANNING'))

		return  result
	}

	def  java.util.LinkedHashMap  getMyTasks(params) {
		//Devuelvo todos los proyectos en los que intervengo

		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session

		log.error ( 'indexMyTasks.params: ' + params)
		def taskFilter = ['TASK_PLANNING', 'TASK_IN_PROGRESS']
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
		def textoDesde = " Desde: "+ fechaDesde.get(Calendar.DAY_OF_MONTH)+'/'+ ( fechaDesde.get(Calendar.MONTH)+1 )+'/'+ fechaDesde.get(Calendar.YEAR)
		def textoHasta = " Hasta: "+ fechaHasta.get(Calendar.DAY_OF_MONTH)+'/'+ (fechaHasta.get(Calendar.MONTH)+1 )+'/'+ fechaHasta.get(Calendar.YEAR)

//		if ( params.projectSelection){
//			projectSelection =  params.projectSelection
//
//		}
//		/*
//           INICIO - DEFINICION valores Filtro de Estado
//           */
//
//		def textoStatus = " - Estados: Aceptados, En Progreso, En Pausa, Planificación, En espera de otra Tarea] "
//		def statusFilter
//		if (params.itemStatusForm){
//
//			statusFilter =  StatusItem.getAll ( params.itemStatusForm )
//
//		}else{
//
//			statusFilter =  StatusItem.getAll ( 'TASK_IN_PROGRESS','TASK_ACCEPTED','TASK_PLANNING')
//
//		}
//		textoStatus = " - Estados: " +  statusFilter
//		/*
//           FIN- DEFINICION valores Filtro de Estado
//        */
//
//
//		projectSelection= Project.get ( projectSelection )
//
//		def leyendaAnterior =""
//
//		if ( projectSelection ){
//			leyendaAnterior = "Proyecto: " + projectSelection
//		}else{
//			leyendaAnterior = "Proyectos: Todos"
//		}
//
//		leyendaAnterior =  leyendaAnterior +   textoStatus
//		def leyendaAnterior2 = textoDesde + textoHasta
//
//		//log.error ( 'fechaFrom: ' +fechaFrom )
//		//log.error ( 'fechaThru: ' +fechaThru )
//		params.fechaDesde = fechaFrom
//		params.fechaHasta = fechaThru
//
//		//log.error ( 'params.fechaDesde: ' + params.fechaDesde)
//		//log.error ( 'params.fechaHasta: ' + params.fechaHasta)
//
//
//		def tasktype = WorkEffortType.get('TASK')
//
//		def userPartyRoles = Party.findAllByUser( session.user )
//		//
//		def resultsWEPA = WorkEffortPartyAssignment.where {
//			workEffort.workEffortType == tasktype &&  state > 0 &&  workEffort.state > 0  && party in userPartyRoles
//		}.list()
//
//		def taskStatusParamsToArray
//
//		taskStatusParamsToArray = params.itemStatusForm
//
//
//		def projectInstance
//		if ( params.projectSelection){
//			projectInstance = Project.get(params.projectSelection)
//
//		}
//
//		def resultsWE = WorkEffort.where {
//			id in resultsWEPA.workEffort.id   &&
//					and {
//						if (projectInstance){
//							eq("project",projectInstance)
//						}
//					}
//			state > 0 && workEffortType == tasktype
//			if (statusFilter)
//			{  currentStatus in ( statusFilter  )	}
//
//		}.list()
//
//
//		def results = Task.where {
//			id in resultsWE.id
//			and {
//				between('dateCreated', fechaDesde.getTime(), fechaHasta.getTime())
//				gt('state', 0)
//			}
//		}.list()
//
//		log.debug ('userPartyRoles: ' + userPartyRoles)
//		log.debug ('resultsWEPA: ' + resultsWEPA)
//		log.debug ('results: ' + results)
//
		def result = [:]
//		result.pfechaDesde = fechaFrom
//		result.pfechaHasta = fechaThru
//		result.statusFilter = statusFilter
//		result.leyendaAnterior2 = leyendaAnterior2
//		result.leyendaAnterior = leyendaAnterior
//		result.projectTypeList = ProjectType.findAllByStateGreaterThan(0)
//		result.itemStatusList =  StatusItem.findAllByStatusType(StatusType.get('TASK_STATUS'))
//		result.taskInstanceList = results
//		result.planningTypeList	 =  WorkEffortType.findAllByWorkEffortTypeRoot(WorkEffortType.get('STRATEGIC_PLANNING'))

		return  result

	}
   
}
