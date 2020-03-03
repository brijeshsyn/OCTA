package com.wma.framework.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.wma.framework.common.ConfigProvider;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

/**
 * This class helps to connect to the outlook application and read emails
 *
 * @author singhb
 *
 */
public class OutlookUtility {
	private static Logger log = Logger.getLogger(OutlookUtility.class);

	private String folder;
	private ExchangeService service;
	private static Integer NUMBER_EMAILS_FETCH = 20;
	private final String webmailURI = "https://webmail.aqrcapital.com/ews/Exchange.asmx";

	/**
	 * Constructor to initialize the object to access email contents
	 * 
	 * @param userName
	 * @param password
	 */
	public OutlookUtility(String userName, String password) {
		try {
			service = new ExchangeService(ExchangeVersion.Exchange2010_SP1);
			service.setUrl(new URI(webmailURI));

			ExchangeCredentials credentials = new WebCredentials(userName, password, "aqr.com");
			service.setCredentials(credentials);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set hte folder from which you need to read emails 
	 * @param folderPath Folder path should be like <b>Inbox\foldeer\folder1_1</b>
	 */
	public void setFolder(String folderName) {
		this.folder = folderName;
	}

	/**
	 * Set the folder from Inbox from which you need to read emails
	 * 
	 * @param foldername
	 *            Foldr Name should be like <b>FD-Apps</b>
	 * @throws Exception
	 */
	private FolderId getFolderId() throws Exception {
		FolderId folderid = new FolderId(WellKnownFolderName.Inbox);

		FolderView fview = new FolderView(1);
		fview.setPropertySet(new PropertySet(FolderSchema.DisplayName, FolderSchema.Id));

		Folder msgf = Folder.bind(service, WellKnownFolderName.Inbox);
		FindFoldersResults res = msgf
				.findFolders(new SearchFilter.ContainsSubstring(FolderSchema.DisplayName, folder), fview);
		List<Folder> f1 = res.getFolders();
		Folder fdApps = f1.get(0);
		folderid = fdApps.getId();

		return folderid;
	}

	private Document getEmailContents(String subject, String sender) {
		Document doc = null;
		try {
			FolderView fview = new FolderView(1);
			fview.setPropertySet(new PropertySet(FolderSchema.DisplayName, FolderSchema.Id));

			FindItemsResults<Item> results = service.findItems(getFolderId(), new ItemView(NUMBER_EMAILS_FETCH));
			for(Item item : results) {
				// To check that the email is not older than the execution start 
				//time 
				if (item.getDateTimeCreated().compareTo(ConfigProvider.getInstance().getExecutionStartedAt()) < 0)
					return null;

				Map<String, String> messageData = readEmailItem(item.getId());
				String strSubject = messageData.get("subject").toString();
				String strSender = messageData.get("senderName").toString();
				if (strSubject.contains(subject) && strSender.contains(sender)) { 
					doc = Jsoup.parse(messageData.get("emailBody"));
					break;
				}
			}
		} catch(Exception e) {
			log.error(e);
		}
		return doc;
	}

	/**
	 * To get the body of the Email for the given Subject and sender in the form of String 
	 * @param subject
	 * @param sender
	 * @return Return the email contents/body
	 */
	public String getEmailBody(String subject, String sender) {
		Document doc = getEmailContents(subject, sender);
		if (doc != null)
			return doc.text();
		else
			return null;
	}

	/**
	 * To get the body of the email for given subject and sender, containing the mentioned value 
	 * @param subject
	 * @param sender
	 * @param value 
	 * @return returns the body of the email which contains the given subject and the mentioned value in it
	 */
	public String getEmailBodyHaving(String subject, String sender, String value) {
		String body = "";
		try {
			FolderView fview = new FolderView(1);
			fview.setPropertySet(new PropertySet(FolderSchema.DisplayName, FolderSchema.Id));

			FindItemsResults<Item> results = service.findItems(getFolderId(), new ItemView(NUMBER_EMAILS_FETCH));
			int i = 1;
			for (Item item : results) {
				// To check that the email is not older than the execution start 
				// time
				if (item.getDateTimeCreated().compareTo(ConfigProvider.getInstance().getExecutionStartedAt()) < 0)
					return null;

				Map<String, String> messageData = readEmailItem(item.getId());
				log.info("\nEmails #" + (i++) + ":");
				String strSubject = messageData.get("subject").toString();
				String strSender = messageData.get("senderName").toString();
				if(strSubject.contains(subject) && strSender.contains(sender)) {
					body = Jsoup.parse(messageData.get("emailBody")).text();
					if(body.contains(value))
						break;
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return body;
	}

	/**
	 * Returns the Hyperlinks/URLs from the email for given subject and sender
	 * 
	 * @param subject
	 * @param sender
	 * @return Returns list of hyperlinks
	 */
	public List<String> getLinksFromEmailBody(String subject, String sender) {
		List<String> links = new ArrayList<>();
		Document doc = getEmailContents(subject, sender);
		if (doc == null) {
			log.error("Email reading failed.");
			return links;
		}
		Elements aHrefs = doc.select("a[href]");
		for (Element link : aHrefs)
			links.add(link.attr("abs:href"));

		return links;
	}

	public void dispose() {
		service.close();
	}

	/**  
	 * Reading one email at a time. Using Item ID of the email. Creating a 
	 * message data map as a return value.
	 * @throws Exception 
	 */
	private Map<String, String> readEmailItem(ItemId itemId) throws Exception {
		Item itm = Item.bind(service, itemId, PropertySet.FirstClassProperties);
		EmailMessage emailMessage = EmailMessage.bind(service, itm.getId());
		Map<String, String> messageData = new HashMap<>();
		messageData.put("emailItemId", emailMessage.getId().toString());
		messageData.put("subject", emailMessage.getSubject().toString());
		messageData.put("fromAddress", emailMessage.getFrom().getAddress().toString());
		messageData.put("senderName", emailMessage.getSender().getName().toString());
		Date dateTimeCreated = emailMessage.getDateTimeCreated();
		messageData.put("SendDate", dateTimeCreated.toString());
		Date dateTimeReceived = emailMessage.getDateTimeReceived();
				messageData.put("ReceivedDate", dateTimeReceived.toString());
		messageData.put("Size", emailMessage.getSize() + "");
		messageData.put("emailBody", emailMessage.getBody().toString());
		
		return messageData;
	}


	private List<Attachment> getEmailAttachments(ItemId itemid) {
		try {
			Item itm = Item.bind(service, itemid, PropertySet.FirstClassProperties);
			EmailMessage emailMessage = EmailMessage.bind(service, itm.getId());
			return emailMessage.getAttachments().getItems();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * To fetch the names of the attached files in the email
	 * @param subject
	 * @param sender 
	 * @return
	 */
	public List<String> getEmailAttachments(String subject, String sender) {
		List<String> attachments = new ArrayList<>();
		try {
			FolderView fview = new FolderView(1);
			fview.setPropertySet(new PropertySet(FolderSchema.DisplayName, FolderSchema.Id));

			FindItemsResults<Item> results = service.findItems(getFolderId(), new ItemView(NUMBER_EMAILS_FETCH));
			for(Item item : results) {
				// To check that the email is not older than the execution start 
				// time
				if(item.getDateTimeCreated().compareTo(ConfigProvider.getInstance().getExecutionStartedAt()) < 0)
					break;
				Map<String, String> messageData = readEmailItem(item.getId());
				String strSubject = messageData.get("subject");
				String strSender = messageData.get("senderName");		
				if(strSubject.contains(subject) && strSender.contains(sender)) {
					for(Attachment attachment : getEmailAttachments(item.getId()))
						attachments.add(attachment.getName());
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return attachments;
	}

	public void sendEmail(String to, String subject, String body) {
		try
		{
			EmailMessage message = new EmailMessage(service);
			EmailAddress fromEmailAddress = new EmailAddress(ConfigProvider.getInstance().getUserName());
			message.setFrom(fromEmailAddress);
			message.getToRecipients().add(to);
			message.setSubject(subject);
			message.setBody(new MessageBody(body));
			message.sendAndSaveCopy();
			log.info("Successfully sent");
		}catch (Exception e)
		{
			log.error(""+e);
		}
	}

	public void sendEmail(String[] to, String subject, String body, String attachments) {
		try{

			EmailMessage message = new EmailMessage(service);
			for(String t : to)
				message.getToRecipients().add(t);
			message.setSubject(subject);
			message.setBody(new MessageBody(body));
			message.getAttachments().addFileAttachment(attachments);
			message.sendAndSaveCopy();
			log.info("Successfully sent");
		} catch (Exception e)
		{
			log.error(""+e);
		}
	}

	private List<EmailMessage> getListOfEmailForSubject(String subject, String sender) {
		List<EmailMessage> emails = new ArrayList<>();
		try {
			FolderView fview = new FolderView(1);
			fview.setPropertySet(new PropertySet(FolderSchema.DisplayName, FolderSchema.Id));
			
			FindItemsResults<Item> results = service.findItems(getFolderId(), new ItemView(NUMBER_EMAILS_FETCH));
			for(Item item : results) {
			// To check that the email is not older than the execution start 
		    // time
			if (item.getDateTimeCreated().compareTo(ConfigProvider.getInstance().getExecutionStartedAt()) < 0)
				break;
			
			Map<String, String> messageData = readEmailItem(item.getId());
			String strSubject = messageData.get("subject").toString();
			String strSender = messageData.get("senderName").toString();
			if(strSubject.contains(subject) && strSender.contains(sender))
				emails.add(getEmailMessage(item.getId()));
		   }
		} catch(Exception e) {
			log.error(e);
		}
		return emails;
	}

	private EmailMessage getEmailMessage(ItemId itemid) {
		EmailMessage emailMesaage = null;
		try {
			Item itm = Item.bind(service, itemid, PropertySet.FirstClassProperties);
			emailMesaage = EmailMessage.bind(service, itm.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return emailMesaage;
	}
	
	public int getCountOfEmailsForSubject(String subject, String sender) {
		return getListOfEmailForSubject(subject, sender).size();
	}
}
