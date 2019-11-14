package com.profesys.scientiam.services

import com.profesys.scientiam.configuration.StatusItem
import com.profesys.scientiam.erp.party.Party
import com.profesys.scientiam.pm.Project
import com.profesys.scientiam.pm.work.Task
import com.profesys.scientiam.pm.work.WorkEffortPartyAssignment
import com.profesys.scientiam.pm.work.WorkEffortType
import grails.gorm.transactions.Transactional
import groovy.sql.GroovyRowResult

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import org.grails.web.util.WebUtils
import com.profesys.scientiam.security.User
import com.profesys.scientiam.pm.work.WorkEffort
import com.profesys.scientiam.pm.work.TimeEntry

import java.text.SimpleDateFormat
import groovy.util.logging.Log4j


@Transactional
class TimeEntryService {

	def TransactionService

	def  ArrayList<GroovyRowResult> index(params){


		def result = [:]
		def fail = { Map m ->
			result.error = [ code: m.code, args: ["TimeEntry"] ]
			return result
		}

		//Devuelvo todos los proyectos en los que intervengo

		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session

		log.error ('TimeEntryService.params: ' + params )



	 	def timeEntryStatusParamsToArray = params.itemStatusForm

		def     timeEntryStatusItemList

		//Por ahora esto no aplica lo pongo como prevencion
		//2017-06-13
		if ( timeEntryStatusParamsToArray  ){
			timeEntryStatusItemList = StatusItem.getAll ( timeEntryStatusParamsToArray )


		}

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

//		def textoDesde = " Desde: "+ fechaDesde.get(Calendar.DAY_OF_MONTH)+'/'+ ( fechaDesde.get(Calendar.MONTH)+1 )+'/'+ fechaDesde.get(Calendar.YEAR)
//		def textoHasta = " Hasta: "+ fechaHasta.get(Calendar.DAY_OF_MONTH)+'/'+ (fechaHasta.get(Calendar.MONTH)+1 )+'/'+ fechaHasta.get(Calendar.YEAR)
//		def proyecto= "Sin Datos"
//
//		def leyendaAnterior =""
//
//		if ( projectInstance ){
//			leyendaAnterior = "Proyecto: " + projectInstance
//		}else{
//			leyendaAnterior = "Proyectos: Todos"
//		}
//		//leyendaAnterior =  leyendaAnterior +  textoDesde + textoHasta
//		def leyendaAnterior2 = textoDesde + textoHasta



		def results  = TimeEntry.where {
			and {
				gt("state",0)
				eq("userCreated",session.user)
				between('fromDate', fechaDesde.getTime() , fechaHasta.getTime())
			}

		}.list()

 		log.error ('results timeEntry.where: ' + results)

		return  results
	}
	
   def list(WorkEffort workEffortInstance) {

	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["TimeEntry"] ]
		   return result
	   }

		def entryCriteria = TimeEntry.createCriteria()
		
		result.timeEntryInstanceList  = entryCriteria.list {
			and{
			  eq("workEffort",workEffortInstance)
			  gt("state",0)
			}
			    
	   
		  }
     

        result.timeEntryInstanceTotal = result.timeEntryInstanceList.size()
 
			log.debug ('result.timeEntryInstanceList : ' + result.timeEntryInstanceList  )
			log.debug ('result.timeEntryInstanceTotal : ' +result.timeEntryInstanceTotal  )
//        if(!result.timeEntryInstanceList || !result.timeEntryInstanceTotal)
//            return fail(code:"default.list.failure")
 
        // Success.
        return result
    }
 
   
   @Transactional
   def save(TimeEntry timeEntryInstance) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
 
	   
	 
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["TimeEntry"] ]
		   return result
	   }
	   log.debug ( 'timeEntryInstance.properties: ' +timeEntryInstance.properties)
	   if (timeEntryInstance == null) {
//		   notFound()
		   log.error('*****-Error--+**** if (timeEntryInstance == null)')
		  return fail(code:"Registro de Entrada de tiempos inexistente")
	   }
	   
	   
	   timeEntryInstance.state           = 2
	   timeEntryInstance.userUpdated     = User.read (  session.user.id )
	   
	   log.debug ('timeEntryInstance.id: ' + timeEntryInstance.id )
	   if (!timeEntryInstance.id ){
		   log.debug ('NO TIENE timeEntryInstance.id: '  )
		   timeEntryInstance.id  			  = TransactionService.serviceID('TIME',session.user.toString() )
		   timeEntryInstance.state           = 1
		   timeEntryInstance.userCreated     = User.read ( session.user.id )
	 
	   }
	   log.debug ('POST transactionService timeEntryInstance.id: ' + timeEntryInstance.id )
	   
	   if (!timeEntryInstance.validate()) {
		   log.error ( 'TimeEntryService.savetimeEntryInstance.timeEntryInstance.errors: ' +  timeEntryInstance.errors )
		   
			result.error = timeEntryInstance.errors
			return result
	   }
	  
	   
	   //Grabo la actualización de horas en la tarea puntual
	   timeEntryInstance.workEffort.actualUnits +=   timeEntryInstance.minutes
 		log.error ('TimeEntry.service  timeEntryInstance.workEffort.actualUnits: ' + timeEntryInstance.workEffort.actualUnits)
	   log.error ('TimeEntry.service  timeEntryInstance.minutes: ' +  timeEntryInstance.minutes)

			   	   	   if (!timeEntryInstance.save(flush:true) ) {
				        timeEntryInstance.errors.each {
						log.debug ( 'TimeEntryService.saveTimeEntry.timeEntryInstance.each.errors: ' +  it )
				 
				   }
			   }


		 
	   
	   result.timeEntryInstance = timeEntryInstance
	   
	   // Success.
	   return result
	   }
   
 
   
}
