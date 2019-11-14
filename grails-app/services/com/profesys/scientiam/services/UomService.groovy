package com.profesys.scientiam.services

import groovy.util.logging.Log4j

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import com.profesys.scientiam.erp.party.Party
import com.profesys.scientiam.erp.party.PartyCurrency; 
import com.profesys.scientiam.configuration.uom.Uom;
import com.profesys.scientiam.configuration.uom.UomType;

import grails.gorm.transactions.Transactional
import groovy.sql.GroovyRowResult;



class UomService {

    def serviceMethod() {

    }
	@Transactional
	def  ArrayList<GroovyRowResult> getPartyCurrencies(Party partyInstance) {
	   //Devuelvo todos los proyectos en los que intervengo 
	   
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   
 
	   			
	   /*
	    * Esto Funciona perfecto, pero no logro sacar los distincts projects
	    */
			   def currencies = PartyCurrency.findAllByParty(partyInstance)
		 
			   log.debug ( 'UomService.getPartyCurrencies.currencies: ' + currencies)
			   //  def results = WorkEffortPartyAssignment.where {
			   //	  workEffort.workEffortType == proyectos &&  state > 0   && party in userPartyRoles
			   //  }.list()
				def uomTypeInstance = UomType.get('CURRENCY_MEASURE')
			   
			      def resultsCurrencies = Uom.where {
			 	  uomType == uomTypeInstance    && id in currencies
			   }.list()
			   
			   
			   
//			   def results = Uom.where {
//				   id in  resultsWEPA.workEffort.id 
//				}.list( )
			   
//				log.debug ('userPartyRoles: ' + userPartyRoles)
//				log.debug ('resultsWEPA: ' + resultsWEPA)
//				log.debug ('resultsWEPA.workEffort.id: ' + resultsWEPA.workEffort.id)
//				log.debug ('resultsWEPA.workEffort.description: ' + resultsWEPA.workEffort.description)
				// log.debug (   results)


	   return  results
	   
   }
}
