package com.profesys.scientiam.services

import grails.gorm.transactions.Transactional
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import org.grails.web.util.WebUtils
import com.profesys.scientiam.security.User

import groovy.util.logging.Log4j



@Transactional
class NoteService {

	def TransactionService
	
   def list(String ticketParam) {
        def result = [:]
        def fail = { Map m ->
            result.error = [ code: m.code, args: ["Note"] ]
            return result
        }
		
		def entryCriteria = Note.createCriteria()
		
		result.noteInstanceList  = entryCriteria.list {
			and{
			  eq("ticket",ticketParam)
			}
			    
	   
		  }
       // params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        //result.noteInstanceList  = Book.list(params)
        result.noteInstanceTotal = result.noteInstanceList.size()
 
			log.debug ('result.noteInstanceList : ' + result.noteInstanceList  )
			log.debug ('result.noteInstanceTotal : ' +result.noteInstanceTotal  )
//        if(!result.noteInstanceList || !result.noteInstanceTotal)
//            return fail(code:"default.list.failure")
 
        // Success.
        return result
    }
 
   
   @Transactional
   def save(Note noteInstance) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session

	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["Note"] ]
		   return result
	   }
	   log.debug ( 'noteInstance.properties: ' +noteInstance.properties)
	   if (noteInstance == null) {
//		   notFound()
		  return fail(code:"Nota inexistente")
	   }
	   
	   noteInstance.state           = 2
	   noteInstance.userUpdated     = User.read (  session.user.id )
	   
	   log.debug ('noteInstance.id: ' + noteInstance.id )
	   if (!noteInstance.id ){
		   log.debug ('NO TIENE noteInstance.id: '  )
		   noteInstance.id  			  = TransactionService.serviceID('NOTE',session.user.toString() )
		   noteInstance.state           = 1
		   noteInstance.userCreated     = User.read ( session.user.id )
	 
	   }
	   log.debug ('POST transactionService noteInstance.id: ' + noteInstance.id )
	   
	   if (!noteInstance.validate()) {
		   log.debug ( 'NoteService.savenoteInstance.noteInstance.errors: ' +  noteInstance.errors )
		   
			result.error = noteInstance.errors
			return result
	   }
	  
	   
	   if (!noteInstance.save(flush:true) ) {
		   noteInstance.errors.each {
				log.debug ( 'NoteService.saveNote.noteInstance.each.errors: ' +  it )
		 
		   }
	   }

		 
	   
	   result.noteInstance = noteInstance
	   
	   // Success.
	   return result
	   }
   
 
   
}
