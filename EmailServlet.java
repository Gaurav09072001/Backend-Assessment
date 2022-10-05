package com.akqa.aem.training.aem201.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.jcr.Node;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Properties;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Session;
import javax.mail.Transport;

/**
 * Servlet that perform add to cart operations
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */

@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="cq:Page",
        methods=HttpConstants.METHOD_POST,
        selectors = "form.add",
        extensions="json")
@ServiceDescription("Cart Servlet")
public class EmailServlet extends SlingAllMethodsServlet {

    @SlingObject
    private Resource currentResource;

    private static final String BASE_PATH = "/content/experience-fragments/aem201/us/en/site/mail/master/jcr:content/root/text";
    public String emailText="";//storing email structure from node
     private static final long serialVersionUID = 1L;
     final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doPost(final SlingHttpServletRequest req,
                          final SlingHttpServletResponse resp) throws ServletException, IOException {

        String fromEmail = "from@example.com"; //Mail of a Sender
        String toEmail = "to@example.com";     //Mail of receiver

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String subject = req.getParameter("subject");
        String subjectDesc = req.getParameter("subject-desc");

        final Resource resource = req.getResource();
        javax.jcr.Session session = resource.getResourceResolver().adaptTo(javax.jcr.Session.class);
        try{
            if (Objects.nonNull(session) && session.nodeExists(BASE_PATH)) {  //Replacing Structure data with fetch data
                final Node node = session.getNode(BASE_PATH);
                emailText=node.getProperty("text").getString();
                emailText=emailText.replace("&lt;contactform.subject&gt;",subject);
                emailText=emailText.replace("&lt;contactform.customerName&gt;",name);
                emailText=emailText.replace("&lt;contactform.customerEmail&gt;",email);
                emailText=emailText.replace("&lt;contactform.requestDescription&gt;",subjectDesc);
            }
        }catch(Exception e){
            log(e.getMessage());
        }

       if(sendMail(fromEmail,toEmail,subject, emailText)){
           resp.getWriter().write("{'Email': 'Success'}");
       }
    }

    //Send Mails
    private boolean sendMail(String fromEmail,String toEmail ,String subject ,String emailText) {

        Properties properties = new Properties();

        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.mailtrap.io");
        properties.put("mail.smtp.port", "25");
        properties.put("mail.smtp.ssl.trust", "smtp.mailtrap.io");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("00dd3a6812d851", "39ee35b345467b");
            }
        });
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(emailText,"text/html");
            Transport.send(message);
            return true;
        }
        catch(MessagingException e){
            e.printStackTrace();
        }
        return true;
    }
}

