package com.profesys.scientiam.services

import grails.gorm.transactions.Transactional
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import com.profesys.scientiam.workspace.Ws_issue
import org.grails.web.util.WebUtils
import groovy.util.logging.Log4j


@Transactional
class Ws_issueService {
	
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
		
		result.ws_issueInstanceList  = entryCriteria.list {
			and{
			  eq("ticket",ticketParam)
			  gt("state", 0)
					isNull('reference')
			}
			    
	   
		  }
       // params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        //result.ws_issueInstanceList  = Book.list(params)
        result.ws_issueInstanceTotal = result.ws_issueInstanceList.size()
 
			log.debug ('result.ws_issueInstanceList : ' +result.ws_issueInstanceList  )
			log.debug ('result.ws_issueInstanceTotal : ' +result.ws_issueInstanceTotal  )
//        if(!result.ws_issueInstanceList || !result.ws_issueInstanceTotal)
//            return fail(code:"default.list.failure")
 
        // Success.
        return result
    }
 
   
   @Transactional
   def save(Ws_issue ws_issueInstance) {
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["Ws_issue"] ]
		   return result
	   }
	   log.debug ( 'ws_issueInstance.properties: ' +ws_issueInstance.properties)
	   if (ws_issueInstance == null) {
//		   notFound()
		  return fail(code:"Nota inexistente")
	   }
	   
	   if (!ws_issueInstance.id) {
		   //		   notFound()
		 //  ws_issueInstance.id   	     =  TransactionService.serviceID('WSISSUE',session.user.toString() )
		   ws_issueInstance.id   	     =  TransactionService.getUIID()
				  }
				  
	   if (!ws_issueInstance.hasErrors()) {
		   

		   ws_issueInstance.save flush:true
	   }
	   
	   result.ws_issueInstance = ws_issueInstance
	   
	   // Success.
	   return result
	   }
   
 
   
}
