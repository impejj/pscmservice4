package com.profesys.scientiam.services

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import com.profesys.scientiam.crm.product.Product
import com.profesys.scientiam.pm.requirement.RequirementStatus
import com.profesys.scientiam.pm.requirement.RequirementType

import grails.gorm.transactions.Transactional
import groovy.sql.GroovyRowResult;
import groovy.util.logging.Log4j



@Transactional
class ProductService {

   def list(String ticketParam) {
        def result = [:]
        def fail = { Map m ->
            result.error = [ code: m.code, args: ["Product"] ]
            return result
        }
		
		def entryCriteria = Product.createCriteria()
		
		result.productInstanceList  = entryCriteria.list {
			and{
			  eq("ticket",ticketParam)
			}
			    
	   
		  }
       // params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        //result.productInstanceList  = Book.list(params)
        result.productInstanceTotal = result.productInstanceList.size()
 
			log.debug ('result.productInstanceList : ' +result.productInstanceList  )
			log.debug ('result.productInstanceTotal : ' +result.productInstanceTotal  )
//        if(!result.productInstanceList || !result.productInstanceTotal)
//            return fail(code:"default.list.failure")
 
        // Success.
        return result
    }
 
   
   @Transactional
   def save(Product productInstance) {
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["Product"] ]
		   return result
	   }
	   log.debug ( 'productInstance.properties: ' +productInstance.properties)
	   if (productInstance == null) {
//		   notFound()
		  return fail(code:"Link inexistente")
	   }
	   if (!productInstance.hasErrors()) {
		   

		   productInstance.save flush:true
	   }
	   
	   result.productInstance = productInstance
	   
	   // Success.
	   return result
	   }
   
   def  ArrayList<GroovyRowResult> getSoftwareProducts(params) {
	   //Devuelvo todos los proyectos en los que intervengo
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
	   log.debug ('service.params: ' + params )
	   
	   def requirementType = RequirementType.get('PROJECT')
 
	   def userPartyRoles = Party.findAllByUser( session.user )
	   //
		  def resultsWEPA = WorkEffortPartyAssignment.where {
		   workEffort.workEffortType == requirementType &&  state > 0 &&  workEffort.state > 0  && party in userPartyRoles
	   }.list()
	   
	  def requirementStatusParamsToArray = params.requirementStatus
	  
	  log.debug ( 'requirementService.params: ' +  params)
	  log.debug  ('requirementStatusParamsToArray: ' + requirementStatusParamsToArray)
			  
			 RequirementStatus requirementStatusList = []
			  
			  def requirementStatusItemList
			   if (! requirementStatusParamsToArray  ){
				   log.debug  ('requirementStatusParamsToArray NULO : ')
				  // requirementStatusItemList = StatusItem.findAllByStatusType (StatusType.get('TASK_STATUS' ))
				   requirementStatusItemList = [ StatusItem.get('WEPR_IN_PROGRESS'),  StatusItem.get('WEPR_PLANNING')]
					}  else  {
				   log.debug  ('requirementStatusParamsToArray CON VALOR : ')
					//   requirementStatusItemList = StatusItem.where { id in  requirementStatusParamsToArray}
						 requirementStatusItemList = StatusItem.getAll ( requirementStatusParamsToArray)
					
				   
				   }
			   log.debug('requirementStatusItemList.typeOf() :' + requirementStatusItemList )
				log.debug ( 'Encontré los statusItem: ' + requirementStatusItemList )
				   
			   log.debug ( 'POST StatusItem.where ')

				def resultsWE = WorkEffort.where {
						   state > 0    &&
							 workEffortType == requirementtype    &&
							currentStatus in ( requirementStatusItemList  )
					   }.list()
						
				//
					   def idsArray
					   if  (!resultsWE){
						   idsArray = '';
					   }else{
					   
						   idsArray = resultsWE.id
					   }
					   
			  log.debug ('resultsWE: ' + resultsWE)
			   def results = Project.where {
				   id in idsArray
				}.list()
									
				log.debug ('results: ' + results)
				
	   return  results
	   
   }
   
}
