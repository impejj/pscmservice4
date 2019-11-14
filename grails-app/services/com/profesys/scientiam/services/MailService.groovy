package com.profesys.scientiam.services

import grails.gorm.transactions.Transactional

import javax.*

import javax.websocket.Session

import groovy.util.logging.Log4j



@Transactional
class MailService {

    def serviceMethod() {

    }
	
	
	//This function fetches emails from your Gmail account
	//and takes your username and password as parameters​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​
	    
		def getEmails(String username, String password) {
			log.info "Fetching emails from Gmail"
	 
			   //Set a bunch of standard properties required to connect to Gmail
			   def host = "imap.gmail.com"
			   def port = "993"        
			   Properties props = new Properties()
			   props.setProperty("mail.store.protocol", "imap")
			   props.setProperty("mail.imap.host", host)
			   props.setProperty("mail.imap.port", port)            
			   props.setProperty("mail.imap.socketFactory.fallback", "false");
			   props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

			   //Connect to Gmail now
			   def session = Session.getDefaultInstance(props, null)
			   def store = session.getStore("imap")
			   store.connect(host, username, password)
			   log.info "Connected to gmail"

			   //Get INBOX folder in Gmail
			   def inbox = store.getFolder("INBOX")
			   
			   //If you only want to read the contents of the folder then connect in  READ_ONLY mode
			   //   inbox.open(Folder.READ_ONLY)
			
			       //If you want to modify the contents of the folder i.e. Mark them READ or MOVE them then use READ_WRITE mode 
			   inbox.open(Folder.READ_WRITE)
			
			   log.info "Opened inbox"

			   
			   // To directly fetch emails without any filtering​​​​​​​​​​​​​​​​​​​​​​ use
			   //   def emails = inbox.getMessages()​​​​

			   	//To fetch filtered messages, for e.g. fetching only Unread messages​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​: 
//			   	Flags seen = new Flags(Flags.Flag.SEEN);
//
//				   //The false parameter will allow us to filter for emails which are not Seen
//				   FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
				   def emails = inbox.search(unseenFlagTerm);
//				   log.info "Messages received"
    
			   //If you want to set the emails received as READ you can set the SEEN flag to true
			   //Otherwise when you read the contents of a email, that email is automatically marked read
			   //in case you had opened the inbox folder in READ_WRITE mode 
			   
		//	   inbox.setFlags(messages, new Flags(Flags.Flag.SEEN), true) ​​​
			   return emails 
		}
		
	 	def getEmailText(email) {
	
		def stringContent = ""
	
		try {
							  //Get content from email. This content is Multipart in case of
			//email sent with Gmail but can be of type String also. I have shown
					   //a sample email sent from Gmail at bottom. You can also check this
			//for yourself by going to Show Original option inside your Gmail's email
	
			def content = email.getContent()
			
//			if (content instanceof Multipart) {
//	
//				log.info "Email content type: Multipart"
//				Multipart mp = (Multipart)content
//	
//				for (int i = 0; i < mp.getCount(); i++) {
//	
//				 def bodyPart = (BodyPart) mp.getBodyPart(i)
//				   def contentType = bodyPart.getContentType().toLowerCase()
//				   log.info "Content type for bodypart is: " + contentType
//				   
//				   //A email can have different content type for
//				   //images, plain text, html, etc.
//				   //To extract html content from email I can check
//				   //for content type text/html
//				   
//				   ​if (contentType.contains("text/html")) {
//					   stringContent+= ((String) mp.getBodyPart(i).content)
//	
//					  //For html spaces i.e. &nbsp; character,
//					  // it gets encoded to \u00a0 in UTF-8
//					  //So we will have to replace it back to &nbsp;
//					  //if we want to show it in html properly​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​​
//					  
//					  stringContent = stringContent.replaceAll("[\u00a0]", "&nbsp;")
//									   }
//							  
//				}
//			}
			
			if (content instanceof String) {
				log.info "Email content type: String"
				stringContent = (String)content
			}
	
		} catch (Exception e) {
			log.error "Could not parse email content. Error:" + e
		}
	
		return stringContent
	}
	
	//Get subject of email
	def getEmailSubject(email) {
		return email.getSubject();
	}
	//	Deleting emails from inbox in GMAIL is not straightforward
	//	We first have to move these emails to trash folder
	//	Then we can set their DELETED flag to true
	//	Also we have configure GMAIL imap settings as shown in the end
	def deleteEmails(emails, inbox, store) {
		
		//Get trash folder from Gmail
//		Folder trash = store.getFolder("[Gmail]/Trash");
		
		//Move emails from inbox folder to trash folder
		inbox.copyMessages(emails, trash);
		
		//Now open Trash folder with read write permission
		trash.open(Folder.READ_WRITE)
		
		//Get all emails inside trash folder
		def toDeleteEmails = trash.getMessages()
	
		//Now set their DELETED flag to true
		toDeleteEmails*.setFlag(Flags.Flag.DELETED, true)
	
		//Now close this folder with expunge paramter set to true
		trash.close(true)
	}
	
	//Getting from email address
	def getEmailFromAddress(email) {
		log.info "Email from addresses are: " + email.getFrom().toString()
		return email.getFrom()[0].toString()
	}

	def sendEmailSSL(String username, String password,String[] destinyBoxes) {
		log.info "Preparing mail to be send"

		//Set a bunch of standard properties required to connect to Gmail
//		def host = "imap.gmail.com"
//		def port = "993"
//		Properties props = new Properties()
//		props.setProperty("mail.store.protocol", "imap")
//		props.setProperty("mail.imap.host", host)
//		props.setProperty("mail.imap.port", port)
//		props.setProperty("mail.imap.socketFactory.fallback", "false");
//		props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

		//Connect to Gmail now
//		def session = Session.getDefaultInstance(props, null)
//		def store = session.getStore("imap")
//		store.connect(host, username, password)
//		log.info "Connected to gmail"
//
//		Properties props = new Properties();
//		props.put("mail.smtp.host", "smtp.gmail.com");
//		props.put("mail.smtp.socketFactory.port", "465");
//		props.put("mail.smtp.socketFactory.class",
//				"javax.net.ssl.SSLSocketFactory");
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.port", "465");

//		Session session = Session.getDefaultInstance(props,
//				new javax.mail.Authenticator() {
//					protected PasswordAuthentication getPasswordAuthentication() {
//						return new PasswordAuthentication("username", "password");
//					}
//				});

//		try {
//
//			Message message = new MimeMessage(session);
//			message.setFrom(new InternetAddress("from@no-spam.com"));
//			message.setRecipients(Message.RecipientType.TO,
//					InternetAddress.parse("to@no-spam.com"));
//			message.setSubject("Testing Subject");
//			message.setText("Dear Mail Crawler," +
//					"\n\n No spam to my email, please!");
//
//			Transport.send(message);
//
//			System.out.println("Done");
//
//		} catch (MessagingException e) {
//			throw new RuntimeException(e);
//		}

	}

}
