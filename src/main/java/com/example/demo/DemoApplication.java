package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

/*configuracion de websockets*/
@Configuration
@EnableWebSocketMessageBroker
class WebSocketMessageBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/broadcast");
    }
}

@Controller
class ThymeleafController {
    @RequestMapping("/index")
    public String index(Model model) {
        return "index";
    }
}

@Controller
@Slf4j
class WebSocketBroadcastController {
    @Autowired
    KafkaConsumer kafkaConsumer;

    @MessageMapping("/broadcast")
    @SendTo("/topic/messages")
    public void send(SimpMessageHeaderAccessor headerAccessor, String str) {
        if (str.equals("{\"from\":\"server\",\"text\":\"connected to server\"}")) {
            // Obtén la sesión a partir del encabezado del mensaje
            String sessionId = headerAccessor.getSessionId();
            log.info("session id: " + sessionId);
            kafkaConsumer.consumeMessages("demo-topic", sessionId);
        }
    }
}

@Slf4j
@Component
class KafkaConsumer {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    //Listar los topicos que correspondan a un regex
    // Contador de ingestas durante el día
    //host izquierdo front derecha
    //aparece y desaparece
    //Inicializar los elementos de debajo en la cantidad ingestada
    // un origen destino

    public void consumeMessages(String topic, String sessionId) {

        ReceiverOptions<Integer, String> receiverOptions = obtainProperties(topic, sessionId);

        Flux<ReceiverRecord<Integer, String>> kafkaFlux = KafkaReceiver.create(receiverOptions).receive();
        kafkaFlux.subscribe(record -> {
            ReceiverOffset offset = record.receiverOffset();
            log.info("Received message: topic-partition={} offset={}  key={} value={}",
                    offset.topicPartition(),
                    offset.offset(),
                    record.key(),
                    record.value());
            messagingTemplate.convertAndSend("/topic/broadcast", String.format("session id %s value %s", sessionId, record.value()));
            offset.acknowledge();
        });
    }

    private ReceiverOptions<Integer, String> obtainProperties(String topic, String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "sample-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ReceiverOptions<Integer, String> receiverOptions = ReceiverOptions.create(props);

        return receiverOptions.subscription(Collections.singleton(topic))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));
    }
}


