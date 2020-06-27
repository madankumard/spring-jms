package mk.springframework.springjms.sender;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import mk.springframework.springjms.config.JmsConfig;
import mk.springframework.springjms.model.HelloWorldMessage;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class HelloSender {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 2000)
    public void sendMessage(){
        //System.out.println("Im sending a message");

        HelloWorldMessage message = HelloWorldMessage.builder()
                .id(UUID.randomUUID())
                .message("Hello World!")
                .build();

        jmsTemplate.convertAndSend(JmsConfig.MY_QUEUE, message);
        //System.out.println("Message Sent!");
    }

    @Scheduled(fixedRate = 2000)
    public void sendAndReceiveMessage() throws JMSException {
        HelloWorldMessage message = HelloWorldMessage
                .builder()
                .id(UUID.randomUUID())
                .message("Hello")
                .build();

        Message receivedMsg = jmsTemplate.sendAndReceive(JmsConfig.MY_SEND_RCV_QUEUE,
                new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        Message helloMessage = null;
                        try{
                            helloMessage = session.createTextMessage(objectMapper.writeValueAsString(message));
                            helloMessage.setStringProperty("_type", "mk.springframework.springjms.model.HelloWorldMessage");

                            System.out.println("Sending hello");
                            return helloMessage;
                        }catch (JsonProcessingException e) {
                            throw new JMSException("boom");
                        }

                    }
                });

        System.out.println(receivedMsg.getBody(String.class));
    }
}
