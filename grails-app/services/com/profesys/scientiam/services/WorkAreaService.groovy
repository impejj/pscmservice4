package com.profesys.scientiam.services

import com.profesys.scientiam.workspace.Ws_action;
import com.profesys.scientiam.workspace.Ws_issue
import com.profesys.scientiam.security.User
import grails.gorm.transactions.Transactional
import groovy.sql.Sql;
import org.grails.web.util.WebUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import groovy.util.logging.Log4j


@Transactional
 
class WorkAreaService {
	

    def serviceMethod() {

    }
	
	@Transactional
	def saveWs_action(Ws_action ws_actionInstance) {
		
		HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
		HttpSession session = request.session
		def result = [:]
		def fail = { Map m ->
			result.error = [ code: m.code, args: ["ws_actionInstance"] ]
			return result
		}
	 
		if (ws_actionInstance == null) {
			notFound()
			log.error ('Accion sin datos')
			return
		}

		if (ws_actionInstance.hasErrors()) {
			
			log.debug ( 'Errors: ' + ws_actionInstance.errors)
			respond ws_actionInstance.errors, view:'create'
			return
		}
 	
 
		 log.debug ( 'session.PRE-PERSON: ')
		 
		ws_actionInstance.state = 1
		ws_actionInstance.userCreated     = User.read (  session.user.id )
		ws_actionInstance.userUpdated     = User.read (  session.user.id )
		 
		 log.debug ( 'WorkAreaService.saveWs_action.ws_actionInstance.properties: ' +ws_actionInstance.properties)
		 log.debug ( 'WorkAreaService.saveWs_action.ws_actionInstance.id: ' +ws_actionInstance.id)
		 
		 def ws_issueInstance = Ws_issue.get(ws_actionInstance.idOrigen)
		  // Borro logicamente al ws_issue
		 if (ws_issueInstance){
			 
			 ws_issueInstance.dateDeleted =  ws_actionInstance.lastUpdated
			 ws_issueInstance.state       = 0
			 ws_issueInstance.userUpdated  =  ws_actionInstance.userUpdated
			 
			 
		 }
		 
		  
		   log.debug ( 'session.ws_issueInstance : ')
		  
		 result.ws_actionInstance = ws_actionInstance
		 
		 if (ws_issueInstance.hasErrors()) {
			 log.debug ( 'ws_issueInstance.groupSave.ws_issueInstance.errors: ' +  ws_issueInstance.errors )
			 
			  result.error = ws_issueInstance.errors
			  return result
		 }
		 if (ws_actionInstance.hasErrors()) {
			 log.debug ( 'ws_actionInstance.groupSave.ws_actionInstance.errors: ' +  ws_actionInstance.errors )
			 
			 result.error = ws_actionInstance.errors
			  return result
		 }
		  log.debug ( 'WorkAreaService.groupSave.grabacion Datos ' )
		  log.debug ( 'WorkAreaService.groupSave.Party.id: ' + ws_actionInstance.id )
//		  log.debug ( 'WorkAreaService.groupSave.PartyGroup.id: ' + personInstance.id )
		  
		  if ( ws_issueInstance.save(flush:true) ) {
			  if (ws_actionInstance.save(flush:true) ) {
				  // Success.
				  result.ws_actionInstance = ws_actionInstance
				  
			  }else{
				  ws_actionInstance.errors.each {
				  log.debug ( 'WorkAreaService.groupSave.ws_actionInstance.each.errors: ' +  it )
				   
				 
				
				  }
				  result.error = ws_actionInstance.errors
			  }
			  
		  } 
		  else{
			  ws_issueInstance.errors.each {
				   log.debug ( 'WorkAreaService.groupSave.ws_issueInstance.each.errors: ' +  it )
			
		  } 
			  result.error = ws_issueInstance.errors
	   }
	 
		  
		  
		return result
		 
		 
	 
	
		 
	}
	
	def getJournals(String cuit) {
		
				assert dataSource != null, "Datasource is null! No Good!!!"
				def sql = Sql.newInstance(dataSource)
				
				def query = """SELECT persona.apellido ,
			     persona.cantidad_credenciales ,
			     persona.cod_docu ,
			     persona.cod_pariente ,
			     persona.cuil ,
			     persona.cuil_titu ,
				 persona.documento,
			     persona.fechaalta ,
			     persona.fechabaja ,
			     persona.fecha_credencial ,
			     persona.fecha_nacimiento ,
			     persona.estado ,
			    persona.nacionalidad,
			   persona.calle,
				persona.numero,
			   persona.piso,
			    persona.departamento,
			    persona.cod_pos,
			    persona.localidad,
			    persona.provincia,
			    persona.telefono,    
			    persona.numeroBeneficiario
			     incapacidad ,
			     tipotitular ,
			      padron.fechaalta ,
			     padron.fechabaja ,
			     padron.estado ,
			     padron.regimen ,
			     padron.seccional ,
			     padron.cuil_titu ,
			     padron.transitorio ,
			     padron.ultimoaporte ,
			     padron.cuit,
				empresa.cuit,
			     empresa.fechaalta,
			    empresa.fechabaja,
			    empresa.descripcion,
			    empresa.direccion,
			    empresa.localidad,
			    empresa.numero,
			    empresa.seccional as empresaSeccional,
			    empresa.provincia_as,
			      empresa.codigoPostal,
			    empresa.telefono,
			    empresa.empresaid, 
			    empresa.provincia
			FROM padron left join empresa on padron.empresaid = empresa.empresaid, persona 
			where padron.cuil_titu = persona.cuil_titu
			and  persona.cuil = ?  """ // USE YOUR ID
				 try {
				 def row = sql.firstRow(query, cuit)
				 log.debug (query)
				 return row
				
				} catch(Exception e) {
				log.error "Exception PadronService.getPadron() ${query} - ${e}"
				}
				//log.debug("Username - ${apellido} apellido - ")
				return null
			}
	
	
}










	