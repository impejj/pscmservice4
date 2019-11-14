package com.profesys.scientiam.services

import com.profesys.scientiam.pm.Project
import com.profesys.scientiam.pm.work.WorkEffort
import com.profesys.scientiam.pm.work.WorkEffortPartyAssignment
import grails.gorm.transactions.Transactional
import org.grails.web.util.WebUtils
import groovy.util.logging.Log4j


@Transactional
class WorkEffortPartyAssignmentService {

   def list(WorkEffort workEffort) {
        def result = [:]
        def fail = { Map m ->
            result.error = [ code: m.code, args: ["WorkEffortPartyAssignment"] ]
            return result
        }
		
		
		def entryCriteria = WorkEffortPartyAssignment.createCriteria()
		
		result.workEffortPartyAssignmentsInstanceList  = entryCriteria.list {
			and{
			  eq("workEffort",workEffort)
			  gt("state",0)
			}
			    
	   
		  }
		log.debug ('result.workEffortPartyAssignmentsInstanceList: ' + result.workEffortPartyAssignmentsInstanceList)
       // params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        //result.workEffortPartyAssignmentsInstanceList  = Book.list(params)
        result.workEffortPartyAssignmentsInstanceTotal = result.workEffortPartyAssignmentsInstanceList.size()
 
			log.debug ('result.workEffortPartyAssignmentsInstanceList : ' +result.workEffortPartyAssignmentsInstanceList  )
			log.debug ('result.workEffortPartyAssignmentsInstanceTotal : ' +result.workEffortPartyAssignmentsInstanceTotal  )
 
        // Success.
        return result
    }
 
   
   @Transactional
   def save( WorkEffortPartyAssignment workEffortPartyAssignmentsInstance) {
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["Links"] ]
		   return result
	   }
	   log.debug ( 'workEffortPartyAssignmentsInstance.properties: ' +workEffortPartyAssignmentsInstance.properties)
	   if (workEffortPartyAssignmentsInstance == null) {
//		   notFound()
		  return fail(code:"Link inexistente")
	   }
	   if (!workEffortPartyAssignmentsInstance.hasErrors()) {
		   

		   workEffortPartyAssignmentsInstance.save flush:true
	   }
	   
	   result.workEffortPartyAssignmentsInstance = workEffortPartyAssignmentsInstance
	   
	   // Success.
	   return result
	   }
   
 
   
}
