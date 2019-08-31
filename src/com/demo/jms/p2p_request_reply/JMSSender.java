package com.demo.jms.p2p_request_reply;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;


public class JMSSender {

    private Session session;
    private Queue requestQueue;
    private Queue responseQueue;
    private Connection connection;

    public JMSSender() {
        try {
            Properties env = new Properties();
            env.put(Context.SECURITY_PRINCIPAL, "system");
            env.put(Context.SECURITY_CREDENTIALS, "manager");
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            env.put(Context.PROVIDER_URL, "tcp://localhost:61616");
            env.put("queue.REQUEST.Q", "REQUEST.Q");
            env.put("queue.RESPONSE.Q", "RESPONSE.Q");


            InitialContext ctx = new InitialContext(env);
            this.requestQueue = (Queue) ctx.lookup("REQUEST.Q");
            this.responseQueue = (Queue) ctx.lookup("RESPONSE.Q");

            ConnectionFactory connectionFactory = ((ConnectionFactory) ctx.lookup("ConnectionFactory"));
            connection = connectionFactory.createConnection();
            connection.start();

        } catch ( JMSException | NamingException e){
             e.printStackTrace();
        }
    }

      public void sendRequest(String message){
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer sender = session.createProducer(this.requestQueue);
            TextMessage msg = session.createTextMessage(message);
            msg.setStringProperty("Street", "121 oak park");
            msg.setJMSReplyTo(this.responseQueue);
            sender.send(msg);
            System.out.println("Message sent");

            //Receive reply back
            String filter = "JMSCorrelationID= '" + msg.getJMSMessageID() + "'";
            MessageConsumer  consumer  = session.createConsumer(responseQueue, filter);
            TextMessage textMessage = (TextMessage) consumer.receive(50000);
            if(textMessage == null){
                System.out.println("NO response");
            }else{
                System.out.println("Message accepted or rejected: " + textMessage.getText());
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        JMSSender jmsSender = new JMSSender();
        jmsSender.sendRequest("Kansas city");
    }

}
