package com.demo.jms.p2p_request_reply;


import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class JMSAsyncReceiver implements MessageListener {

    private Connection connection;
    private Session session;
    private Queue queue;


    public JMSAsyncReceiver() {
        try {
            connection = new ActiveMQConnectionFactory("tcp://localhost:61616").createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            queue = session.createQueue("REQUEST.Q");
            MessageConsumer receiver = session.createConsumer(queue);
            connection.start();
            receiver.setMessageListener(this);
            System.out.println("Waiting on messages");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void onMessage(Message message) {
        try {
            TextMessage inputMessage = (TextMessage)message;
            String street = inputMessage.getStringProperty("Street");
            System.out.println("Street = " + street);
            System.out.println("City: " + inputMessage.getText());

            //Send confirmation back
            TextMessage replyMessage = session.createTextMessage();
            replyMessage.setText("Accepted");
            replyMessage.setJMSCorrelationID(inputMessage.getJMSMessageID());
            MessageProducer producer = session.createProducer(inputMessage.getJMSReplyTo());
            producer.send(replyMessage);
            System.out.println("Waiting for address change");

        } catch (Exception up) {
            up.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        new Thread() {
            public void run() {
                new JMSAsyncReceiver();
            }}.start();
    }

}

