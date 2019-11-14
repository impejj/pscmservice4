package com.profesys.scientiam.services

import grails.gorm.transactions.Transactional
import org.grails.web.util.WebUtils

import groovy.util.logging.Log4j



@Transactional
class LinkService {

   def list(String ticketParam) {
        def result = [:]
        def fail = { Map m ->
            result.error = [ code: m.code, args: ["Links"] ]
            return result
        }
		
		def entryCriteria = Links.createCriteria()
		
		result.linksInstanceList  = entryCriteria.list {
			and{
			  eq("ticket",ticketParam)
			}
			    
	   
		  }
       // params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        //result.linksInstanceList  = Book.list(params)
        result.linksInstanceTotal = result.linksInstanceList.size()
 
			log.debug ('result.linksInstanceList : ' +result.linksInstanceList  )
			log.debug ('result.linksInstanceTotal : ' +result.linksInstanceTotal  )
//        if(!result.linksInstanceList || !result.linksInstanceTotal)
//            return fail(code:"default.list.failure")
 
        // Success.
        return result
    }
 
   
   @Transactional
   def save(Links linksInstance) {
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["Links"] ]
		   return result
	   }
	   log.debug ( 'linksInstance.properties: ' +linksInstance.properties)
	   if (linksInstance == null) {
//		   notFound()
		  return fail(code:"Link inexistente")
	   }
	   if (!linksInstance.hasErrors()) {
		   

		   linksInstance.save flush:true
	   }
	   
	   result.linksInstance = linksInstance
	   
	   // Success.
	   return result
	   }
   
 
   
}
