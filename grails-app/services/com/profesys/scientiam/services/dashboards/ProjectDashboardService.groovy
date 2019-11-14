package com.profesys.scientiam.services.dashboards

import com.profesys.scientiam.pm.work.WorkEffort
import com.profesys.scientiam.security.User
import com.profesys.scientiam.configuration.uom.Uom
import com.profesys.scientiam.configuration.Enumeration
import com.profesys.scientiam.configuration.StatusItem
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.grails.web.util.WebUtils
//import org.grails.gorm.hibernate.connections.*
       // HibernateConnectionSourceFactory

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import groovy.util.logging.Log4j



@Transactional
class ProjectDashboardService {

   def TransactionService
   // HibernateConnectionSourceFactory sessionFactory;
    def grailsApplication = Holders.getGrailsApplication()
    def sessionFactory = grailsApplication?.mainContext?.sessionFactory

    def listPersons(){
        String query = "select distinct username from person";
        def personList = sessionFactory.getCurrentSession().createSQLQuery(query).list();

        log.error ('personList: ' + personList)
        return personList;
    }

   def listTaskByProject(String ticketParam) {
        def result = [:]
        def fail = { Map m ->
            result.error = [ code: m.code, args: ["Story"] ]
            return result
        }
		
		//def entryCriteria = Project.createCriteria()
		
		result.storyInstanceList  = entryCriteria.list {
			and{
			  eq("ticket",ticketParam)
			}
			    
	   
		  }
       // params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        //result.storyInstanceList  = Book.list(params)
        result.storyInstanceTotal = result.storyInstanceList.size()
 
			log.debug ('result.storyInstanceList : ' +result.storyInstanceList  )
			log.debug ('result.storyInstanceTotal : ' +result.storyInstanceTotal  )
//        if(!result.storyInstanceList || !result.storyInstanceTotal)
//            return fail(code:"default.list.failure")
 
        // Success.
        return result
    }

    def getMyPKIProductivityDashboard(){

        def sum = WorkEffort.executeQuery("select sum(m.price) from ME as me join me.m as m where me.e = :e", [e: givenE])
        println sum


        def result = [:]
        result.horasProgramadas = 200
        result.horasEjecutadas  = 130

        return  result


    }
   
}
