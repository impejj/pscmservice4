package com.profesys.scientiam.services

import java.util.ArrayList;

import com.profesys.scientiam.pm.issue.Issue
import com.profesys.scientiam.pm.work.Task;
import com.profesys.scientiam.pm.work.WorkEffort
import com.profesys.scientiam.pm.work.WorkEffortPurposeType
import com.profesys.scientiam.pm.work.WorkEffortType
import com.profesys.scientiam.pm.work.WorkEffortPartyAssignment
import com.profesys.scientiam.workspace.Ws_issue
import com.profesys.scientiam.security.User
import com.profesys.scientiam.configuration.uom.Uom
import com.profesys.scientiam.configuration.Enumeration
import com.profesys.scientiam.configuration.StatusItem
import com.profesys.scientiam.erp.party.Party

import grails.gorm.transactions.Transactional
import groovy.sql.GroovyRowResult;

import org.grails.web.util.WebUtils

 


import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import groovy.util.logging.Log4j



@Transactional
class IssueService {

   def TransactionService
   def WorkEffortService
   
   
   
   def listByUserStory(WorkEffort workEffort) {
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["Issue"] ]
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
		   log.debug ('result.issueInstanceList : ' + result.workEffortAssocInstanceList  )
		   log.debug ('result.issueInstanceTotal : ' +result.workEffortAssocInstanceTotal  )
//        if(!result.noteInstanceList || !result.noteInstanceTotal)
//            return fail(code:"default.list.failure")

	   // Success.
	   return result
   }
   
   
   
  @Transactional
  def saveIssue( Issue issueInstance) {
	  
	  HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	  HttpSession session = request.session
	  log.debug ( '*****-INGRESO A saveIssue - *******: ' )
	  
	  def result = [:]
	  def fail = { Map m ->
		  result.error = [ code: m.code, args: ["issueInstance"] ]
		  return result
	  }
	  def workEffortInstance = issueInstance.workEffort
	  if (workEffortInstance == null) {
//		   notFound()
		 return fail(code:"Esfuerzo de Tarea con Valores Nulos")
	  }
	  if (issueInstance == null) {
		  //		   notFound()
					return fail(code:" Tarea con Valores Nulos")
				 }
		  //
	  log.debug ( ' ***************PRE-WorkEffortService.save'  )
	  workEffortInstance.workEffortType = WorkEffortType.read("ISSUE")

	  
	  
//	   workEffortInstance.workEffortPurposeType  = WorkEffortPurposeType.read('WEPT_ANALYSIS_DESIGN')
//	   workEffortInstance.code 			 = issueInstance.code
	  if (!workEffortInstance.id) {
		  workEffortInstance.actualCompletionDate= null
		  workEffortInstance.actualStartDate = null
		  workEffortInstance.estimatedCompletionDate= null
		  workEffortInstance.estimatedStartDate=null
//		  if (! workEffortInstance.scopeEnum){
			  workEffortInstance.scopeEnum = Enumeration.read('WES_PUBLIC')
//		  }
		  //workEffortInstance.percentCompleted = 0
//		  if (! workEffortInstance.currentStatus){
			  workEffortInstance.currentStatus= StatusItem.get ('ISSUE_OPEN')
			  
//		  }
		  workEffortInstance.moneyUom =  Uom.read ('ARS')
	  }

	  
	  result = WorkEffortService.save  (  workEffortInstance )
	  log.debug ('issueService.saveissue().WorkEffortService.save.properties='+result.properties)
	  log.debug ('issueService.saveissue().WorkEffortService.save.result='+result)
	  log.debug ('issueService.saveissue().WorkEffortService.save.result='+result.error)
	  
	  if (!result.workEffortInstance) {
		  log.debug ( 'FALLO LA VALIDACION!  -->!workEffortInstance.save.result: ' + result )
		  throw new RuntimeException(result.error.toString())
//		   return  ( result )
		}else{
			log.debug ('result.workEffortInstance.id: ' + result.workEffortInstance.id )
			issueInstance.userUpdated          =   session.user
			issueInstance.state     		   =  2
			log.debug ('result-pre. issueInstance.properties  : ' +  issueInstance.properties   )
			if (!issueInstance.id){
				//Es un registro nuevo y tengo que insertar los valores de id y ticket
				
				issueInstance.userCreated       = session.user
				issueInstance.id      		   = result.workEffortInstance.id
				issueInstance.workEffort 	   = result.workEffortInstance
				issueInstance.state             = 1
				//issueInstance.status 			= null
				
				log.debug ('result. issueInstance.id          : ' +  issueInstance.id           )
				log.debug ('result. issueInstance.properties  : ' +  issueInstance.properties   )
				log.debug ('result. issueInstance.workEffort  : ' +  issueInstance.workEffort.properties  )
			}
		}
	
	  
		log.debug ( 'PRE--issueInstance.hasErrors: ')
	  
	  if (!issueInstance.validate()) {
//		   log.debug ( 'IssueService.issueInstance.errors: ' +  issueInstance.errors )
		   result.error = issueInstance.errors
		   
		   log.debug ( 'IssueService.issueInstance.errors: ' +  issueInstance.errors )
		   log.debug ( 'IssueService. result.error : ' +  result.error )
		   throw new RuntimeException ( result.error.toString() ) ;
	  }
	 
	  log.debug ( 'PRE--save ')
	
	  
	   if (!issueInstance.save( flush:true ) ) {
		   log.error ( '!issueInstance.save: ' + issueInstance.errors )
		   result.error ="Fallo el grabado de Tareas: " +   issueInstance.errors
		   
		   //return (result)
		   throw new RuntimeException(result)
	   }
	   
	  result.issueInstance = issueInstance
	  log.debug ( 'issueService.save(): - SUCCESS')
	  // Success.
	  return result
	  }
   
   
   
   def list(String ticketParam) {
        def result = [:]
        def fail = { Map m ->
            result.error = [ code: m.code, args: ["Issue"] ]
            return result
        }
		
		def entryCriteria = Issue.createCriteria()
		
		result.issueInstanceList  = entryCriteria.list {
			and{
			  eq("ticket",ticketParam)
			}
			    
	   
		  }
       // params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        //result.issueInstanceList  = Book.list(params)
        result.issueInstanceTotal = result.issueInstanceList.size()
 
			log.debug ('result.issueInstanceList : ' +result.issueInstanceList  )
			log.debug ('result.issueInstanceTotal : ' +result.issueInstanceTotal  )
//        if(!result.issueInstanceList || !result.issueInstanceTotal)
//            return fail(code:"default.list.failure")
 
        // Success.
        return result
    }
 
   
 
   @Transactional
   def save(Issue issueInstance ) {
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["Issue"] ]
		   return result
	   }
	   log.debug ( 'issueInstance.properties: ' +issueInstance.properties)
	   if (issueInstance == null) {
//		   notFound()
		  return fail(code:"Nota inexistente")
	   }
	   if (!issueInstance.hasErrors()) {
		   

		   issueInstance.save flush:true
	   }
	   
	   result.issueInstance = issueInstance
	   
	   // Success.
	   return result
	   }
   
   def  ArrayList<GroovyRowResult> getIssueByStatus(params) {
	   //Devuelvo todos los proyectos en los que intervengo
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
	   log.debug ('service.params: ' + params )
	   
	   def issuetype = WorkEffortType.get('ISSUE')
 
	   def userPartyRoles = Party.findAllByUser( session.user )
	   //
		  def resultsWEPA = WorkEffortPartyAssignment.where {
		   workEffort.workEffortType == issuetype &&  state > 0 &&  workEffort.state > 0  && party in userPartyRoles
	   }.list()
	   
	  def issueStatusParamsToArray = params.issueStatus
	  
	  log.debug ( 'issueService.params: ' +  params)
	  log.debug  ('issueStatusParamsToArray: ' + issueStatusParamsToArray)
			  
			  StatusItem issueStatusList = []
			  
			  def issueStatusItemList
			   if (! issueStatusParamsToArray  ){
				   log.debug  ('issueStatusParamsToArray NULO : ')
				  // issueStatusItemList = StatusItem.findAllByStatusType (StatusType.get('TASK_STATUS' ))
				   issueStatusItemList = [ StatusItem.get('TASK_IN_PROGRESS'),  StatusItem.get('TASK_PLANNING')]
					}  else  {
				   log.debug  ('issueStatusParamsToArray CON VALOR : ')
					//   issueStatusItemList = StatusItem.where { id in  issueStatusParamsToArray}
						 issueStatusItemList = StatusItem.getAll ( issueStatusParamsToArray)
					
				   
				   }
			   log.debug('issueStatusItemList.typeOf() :' + issueStatusItemList )
			    log.debug ( 'Encontré los statusItem: ' + issueStatusItemList )
				   
			   log.debug ( 'POST StatusItem.where ')
			   log.debug ( 'issueStatusParamsToArray: ' + issueStatusParamsToArray)
			   log.debug ( 'issueStatusList: '      + issueStatusList )
			   log.debug ( 'issueStatusItemList: ' + issueStatusItemList)

				def resultsWE = WorkEffort.where {
					 	  state > 0   &&
							workEffortType == issuetype    &&
						   currentStatus in ( issueStatusItemList  )
					   }.list()
						
				//
					   log.debug ('resultsWE: ' + resultsWE)
			   def results = Issue.where {
				   id in resultsWE.id
				}.list()
									
				log.debug ('results: ' + results)
				
	   return  results
	   
   }
   
}
