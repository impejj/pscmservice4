package com.profesys.scientiam.services

import grails.gorm.transactions.Transactional
import groovy.util.logging.Log4j



@Transactional
class ResourceService {

   def list(String ticketParam) {
        def result = [:]
        def fail = { Map m ->
            result.error = [ code: m.code, args: ["Resource"] ]
            return result
        }
		
		def entryCriteria = Resource.createCriteria()
		
		result.resourceInstanceList  = entryCriteria.list {
			and{
			  eq("ticket",ticketParam)
			  gt("status",0)
			}
			    
	   
		  }
       // params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        //result.resourceInstanceList  = Book.list(params)
        result.resourceInstanceTotal = result.resourceInstanceList.size()
 
			log.debug ('result.resourceInstanceList : ' +result.resourceInstanceList  )
			log.debug ('result.resourceInstanceTotal : ' +result.resourceInstanceTotal  )
 
        // Success.
        return result
    }
 
   
   @Transactional
   def save(Resource resourceInstance) {
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["Resource"] ]
		   return result
	   }
	   log.debug ( 'resourceInstance.properties: ' +resourceInstance.properties)
	   if (resourceInstance == null) {
//		   notFound()
		  return fail(code:"Resource inexistente")
	   }
	   if (!resourceInstance.hasErrors()) {
		   

		   resourceInstance.save flush:true
	   }
	   
	   result.resourceInstance = resourceInstance
	   
	   // Success.
	   return result
	   }
   
 
   
}
