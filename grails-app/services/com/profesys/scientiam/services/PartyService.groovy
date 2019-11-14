package com.profesys.scientiam.services

import com.profesys.scientiam.configuration.data.DataSource
import com.profesys.scientiam.erp.party.Party
import com.profesys.scientiam.erp.party.PartyRole
import com.profesys.scientiam.erp.party.PartyType
import com.profesys.scientiam.erp.party.PartyGroup
import com.profesys.scientiam.erp.party.Person
import com.profesys.scientiam.pm.Project
import com.profesys.scientiam.pm.work.Task
import com.profesys.scientiam.pm.work.WorkEffort
import com.profesys.scientiam.pm.work.WorkEffortPartyAssignment
import com.profesys.scientiam.pm.work.WorkEffortType
import com.profesys.scientiam.security.User
import com.profesys.scientiam.configuration.StatusItem
import com.profesys.scientiam.configuration.uom.Uom
import groovy.sql.GroovyRowResult
import org.grails.web.util.WebUtils

import grails.gorm.transactions.Transactional

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.text.SimpleDateFormat
import groovy.util.logging.Log4j



@Transactional
class PartyService {
	
	def TransactionService
 

 
   
   @Transactional
   def save(Person personForm) {
	   // Success.
	   return result
    }
	  
   @Transactional
   def savePerson(Person personInstance) {
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   log.debug ( '*****-INGRESO A savePerson - *******: ' )
	   log.debug ( 'personInstance*: '+ personInstance)
	   log.debug ( 'personInstance*properties: '+ personInstance.properties)
	   
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["personForm"] ]
		   return result
	   }
	   if (personInstance == null) {
//		   notFound()
		  return fail(code:"Persona con Valores Nulos")
	   }
	   log.debug ( 'session.PRE-PERSON: ')
	   log.debug ( 'PartyService.savePerson.personInstance.id: '         + personInstance.id)
	   
	   personInstance.state           = 2
	   personInstance.userUpdated     = User.read (  session.user.id )
	   if (!personInstance.id ){
		   personInstance.id  			  = TransactionService.serviceID('PERSON',session.user.toString() )
	       personInstance.state           = 1
	       personInstance.userCreated     = User.read ( session.user.id )
	 
	   }
	   
		//Genero un registro de Party
		//con valores por defecto
	   log.debug ( 'session.PREPARTY ')
	   def partyInstance =  personInstance.party 
	    
	   if (!partyInstance){
		   log.debug (' !partyInstance' )
		   
		   partyInstance = new Party()
		   partyInstance.id              = personInstance.id
		   partyInstance.state           = 1
		   partyInstance.userCreated     = personInstance.userCreated
		   partyInstance.ticket 		 = partyInstance.id 
		  
		   
	   }else{
	   		log.debug (' else (!partyInstance) ')
		    log.debug (' def partyInstance = Party.get(personInstance.party): ' +  partyInstance)
	   		partyInstance.state           = 2
	   
	   }
	   def apellido  =  personInstance.lastName ?: ' '
	   def nombre  =  personInstance.firstName ?: ' '
	   def segundoNombre   =  personInstance.middleName ?: ' '
	   def localDescription 				= apellido + ', ' +nombre + ' ' + segundoNombre
		log.debug ('localDescription: '+ localDescription)
		partyInstance.description          	= localDescription
		partyInstance.isUnread            	 = 'N'
		partyInstance.partyType           	 = PartyType.read('PERSON')
		partyInstance.status              	 =  StatusItem.read ('PARTY_ENABLED')
		partyInstance.dataSourceA          	 =  DataSource.read('USER_ENTRY')
		partyInstance.preferredCurrencyUom 	 =  Uom.read('ARS')
		partyInstance.userUpdated     		 = personInstance.userUpdated
		log.debug ( 'session.POSPARTY : ')
		
	   result.personInstance = personInstance
	   
	   if (partyInstance.hasErrors()) {
		   log.debug ( 'PartyService.savePerson.partyInstance.errors: ' +  partyInstance.errors )
		   
			result.error = partyInstance.errors
			return result
	   }
	  
		log.debug ( 'PartyService.savePerson.grabacion Datos ' )
		log.debug ( 'PartyService.savePerson.Party.id: '      + partyInstance.id )
		log.debug ( 'PartyService.savePerson.PartyGroup.id: ' + personInstance.id )
		
		if (!partyInstance.save(flush:true) ) {
			partyInstance.errors.each {
				 log.debug ( 'PartyService.savePerson.partyInstance.each.errors: ' +  it )
		  
			}
		}
		
		personInstance.party = partyInstance
		if (!personInstance.validate(['maritalStatus','gender','lastName','firstName'])) {
			log.debug ( 'PartyService.savePerson.personInstance.errors: ' +  personInstance.errors )
			
			result.error = personInstance.errors
			 return result
		}
		if (!personInstance.save(flush:true) ) {
			personInstance.errors.each {
				 log.debug ( 'PartyService.savePerson.personInstance.each.errors: ' +  it )
		  
			}
		}
		
	   // Success.
	   return result
	}
   
   
   
   @Transactional
   def saveGroup(PartyGroup partyGroupInstance,PartyType partyType) {
	  
	   HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
	   HttpSession session = request.session
	   log.debug ( '*****-INGRESO A groupSave - *******: ' )
	   log.debug ( 'partyGroupForm*: '+ partyGroupInstance )
	   log.debug ( 'partyGroupForm*.properties: '+ partyGroupInstance.properties)
	   def result = [:]
	   def fail = { Map m ->
		   result.error = [ code: m.code, args: ["partyGroupInstance"] ]
		   return result
	   }
	   if (partyGroupInstance == null) {
//		   notFound()
		  return fail(code:"Grupo con Valores Nulos")
	   }
	   log.debug ( 'session.partyGroupForm.id : ' + partyGroupInstance.id )
	   log.debug ( 'session.PRE-PARTYGROUP: ')
	   
	   partyGroupInstance.state           = 2
	   partyGroupInstance.userUpdated     = User.read (  session.user.id )
	   if (!partyGroupInstance.id ){
		   partyGroupInstance.id  = TransactionService.serviceID('PARTYGROUP',session.user.toString() )
		   partyGroupInstance.state           = 1
		   partyGroupInstance.userCreated     = User.read ( session.user.id )
	 
	   }
	   def partyInstance =  partyGroupInstance.party
	   
	  if (!partyInstance){
		    partyInstance 			     = new Party()
		   partyInstance.id 		     = partyGroupInstance.id 
		   partyInstance.state           = 1
		   partyInstance.userCreated     = partyGroupInstance.userCreated
		   partyInstance.ticket 		 = partyGroupInstance.id
		   
	   }else{
	   
			   partyInstance.state           = 2
	   
	   }

	   partyInstance.userUpdated     = partyGroupInstance.userUpdated
	   	
		//Genero un registro de Party 
		//con valores por defecto
	    log.debug ( 'session.PREPARTY : ')
		partyInstance.description = partyGroupInstance.groupName
		partyInstance.isUnread    = 'N'
		partyInstance.partyType   =  partyType
		partyInstance.status      =  StatusItem.read ('PARTY_ENABLED')
		// partyInstance.dataSource =  DataSource.read('USER_ENTRY')
		partyInstance.preferredCurrencyUom = Uom.read('ARS')
		log.debug ( 'session.POSTPARTY   ')
		
	   result.partyGroupInstance = partyGroupInstance
	   
	   if (partyInstance.hasErrors()) {
		   log.debug ( 'PartyService.groupSave.partyInstance.errors: ' +  partyInstance.errors )
		    result.error = partyInstance.errors
		    return result
	   }
	  
		if (!partyInstance.save(flush:true) ) {
			partyInstance.errors.each {
				 log.debug ( 'PartyService.groupSave.partyInstance.each.errors: ' +  it )
			}
		}
		partyGroupInstance.party = partyInstance
		if (!partyGroupInstance.validate()) {
			log.debug ( 'PartyService.groupSave.partyGroupInstance.errors: ' +  partyGroupInstance.errors )
			result.error = partyInstance.errors
			 return result
		}
		 log.debug ( 'PartyService.groupSave.grabacion Datos ' )
		 log.debug ( 'PartyService.groupSave.Party.id: '      + partyInstance.id )
		 log.debug ( 'PartyService.groupSave.PartyGroup.id: ' + partyGroupInstance.id )
		 
		if (!partyGroupInstance.save(flush:true) ) {
			partyGroupInstance.errors.each {
				 log.debug ( 'PartyService.groupSave.partyGroupInstance.each.errors: ' +  it )
		  
			}
		}
		
	   // Success.
	   return result
	   }



	def  ArrayList<GroovyRowResult> getOrganizationControlParties(params) {
		//Devuelvo todos los proyectos en los que intervengo

		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session

		log.debug ('service.params: ' + params )

		def entryRTCriteria = PartyRole.createCriteria()
		def partyRoleList = entryRTCriteria.list {
			and {

				eq ("roleType.id", "ORGANIZATION_CONTROL")
				gt("state", 0)

			}

		}
		log.error ('partyRoleList: '+ partyRoleList)
		log.error ('partyRoleList.party: '+ partyRoleList.party.id)

		//def partyInstanceList= Party.findAllById(partyRoleList.party)


		def partyInstanceList = Party.where {
			id in partyRoleList.party.id
			state > 0
		}


		return  partyInstanceList.list()

	}
   
}
