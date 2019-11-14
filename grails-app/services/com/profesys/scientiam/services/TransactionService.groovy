package com.profesys.scientiam.services

import grails.gorm.transactions.Transactional

import java.security.SecureRandom
import java.text.SimpleDateFormat
import groovy.util.logging.Log4j


@Transactional
class TransactionService {

    def serviceMethod() {

    }
	
	String serviceTicket(String domain, String user){
		//Date now = new Date()
		if (!user) {
			user="AUTO"
			
		}
		
		//SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
		Calendar calendar =  Calendar.getInstance();
	 
		int year       = calendar.get(Calendar.YEAR);
		int month      = calendar.get(Calendar.MONTH); // Jan = 0, dec = 11
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		int dayOfWeek  = calendar.get(Calendar.DAY_OF_WEEK);
		int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
		int weekOfMonth= calendar.get(Calendar.WEEK_OF_MONTH);
	 
		int hour       = calendar.get(Calendar.HOUR);        // 12 hour clock
		int hourOfDay  = calendar.get(Calendar.HOUR_OF_DAY); // 24 hour clock
		int minute     = calendar.get(Calendar.MINUTE);
		int second     = calendar.get(Calendar.SECOND);
		int millisecond= calendar.get(Calendar.MILLISECOND);
//		Integer year = now.year + 1900 + now.getYear()
//		Integer month = now.month + 1
//		Integer day = now.getAt(Calendar.DAY_OF_MONTH) // inconsistent!
//		Integer hour = now.hours
//		Integer minute = now.minutes
//		Integer seconds = now.seconds
		
		def instante = 	 String.format("%04d", year) +
		String.format("%02d", month) +
		String.format("%02d", dayOfMonth) +
		String.format("%02d", hourOfDay) +
		String.format("%02d", minute)+
		String.format("%02d", second)+
		String.format("%04d", millisecond)
		
		
		def ticket = instante + domain + user
	//	println ( 'ticket: ' + ticket )
		log.debug ( 'ticket: ' + ticket )
		
		return ticket;
		
	}
	
	String serviceID(String domainPrefix, String user){
		//Date now = new Date()
		if (!user) {
			user="AUTO"
			
		}
		
		//SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
		Calendar calendar =  Calendar.getInstance();
	 
		int year       = calendar.get(Calendar.YEAR);
		int month      = calendar.get(Calendar.MONTH); // Jan = 0, dec = 11
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		int dayOfWeek  = calendar.get(Calendar.DAY_OF_WEEK);
		int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
		int weekOfMonth= calendar.get(Calendar.WEEK_OF_MONTH);
	 
		int hour       = calendar.get(Calendar.HOUR);        // 12 hour clock
		int hourOfDay  = calendar.get(Calendar.HOUR_OF_DAY); // 24 hour clock
		int minute     = calendar.get(Calendar.MINUTE);
		int second     = calendar.get(Calendar.SECOND);
		int millisecond= calendar.get(Calendar.MILLISECOND);
//		Integer year = now.year + 1900 + now.getYear()
//		Integer month = now.month + 1
//		Integer day = now.getAt(Calendar.DAY_OF_MONTH) // inconsistent!
//		Integer hour = now.hours
//		Integer minute = now.minutes
//		Integer seconds = now.seconds
		
		def instante = 	 String.format("%02d", year) +
		String.format("%02d", month) +
		String.format("%02d", dayOfMonth) +
		String.format("%02d", hourOfDay) +
		String.format("%02d", minute)+
		String.format("%02d", second)+
		String.format("%04d", millisecond)
		
		log.debug ( '.instante: ' + instante)
		log.debug ( '.instante.toInteger(): ' + instante)
		def sufix = new BigInteger(instante).toString(16)
		log.debug ( '.sufix: ' + sufix)
	//	def sufix = Integer.toHexString(instante.toInteger())
		def id = domainPrefix + sufix  
		
		log.debug ( 'Transaction.service.id: ' + id )
		
		return id;
		
	}
	
	String getCode(){
		//Date now = new Date()
	 
		
		//SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
		Calendar calendar =  Calendar.getInstance();
	 
		int year       = calendar.get(Calendar.YEAR);
		int month      = calendar.get(Calendar.MONTH); // Jan = 0, dec = 11
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		int dayOfWeek  = calendar.get(Calendar.DAY_OF_WEEK);
		int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
		int weekOfMonth= calendar.get(Calendar.WEEK_OF_MONTH);
	 
		int hour       = calendar.get(Calendar.HOUR);        // 12 hour clock
		int hourOfDay  = calendar.get(Calendar.HOUR_OF_DAY); // 24 hour clock
		int minute     = calendar.get(Calendar.MINUTE);
		int second     = calendar.get(Calendar.SECOND);
		int millisecond= calendar.get(Calendar.MILLISECOND);
		
		def instante = 	 String.format("%04d", year) +
		String.format("%02d", month) +
		String.format("%02d", dayOfMonth) +
		String.format("%02d", hourOfDay) +
		String.format("%02d", minute)+
		String.format("%02d", second)+
		String.format("%04d", millisecond)
		
		
		def code = instante  
	 
		log.debug ( 'code: ' + code )
		
		return code;
		
	}
	String getUIID( ){


		return  UUID.randomUUID().toString()

	}
	String getUIID32( ){

		//UNHEX(CONCAT(SUBSTR(work_effort_id, 15, 4),SUBSTR(work_effort_id, 10, 4),SUBSTR(work_effort_id, 1, 8),SUBSTR(work_effort_id, 20, 4),SUBSTR(work_effort_id, 25)))
		String idUuid32 = getUIID()

		 idUuid32 = idUuid32.substring(14,18) + idUuid32.substring(9,13) +  idUuid32.substring(0,8) +  idUuid32.substring(19,23)  +  idUuid32.substring(24)
		log.error ('idUuid32: ' + idUuid32)

		return  idUuid32
		
	}

}



