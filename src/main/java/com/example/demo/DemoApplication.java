package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.HandshakeHandler;

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
	public String saludar(Model model) {
		return "index";
	}
}
@Controller
class WebSocketBroadcastController {

	@GetMapping("/stomp-broadcast")
	public String getWebSocketBroadcast() {
		return "stomp-broadcast";
	}

	@MessageMapping("/broadcast")
	@SendTo("/topic/messages")
	public ChatMessage send(ChatMessage chatMessage) throws Exception {
		return new ChatMessage(chatMessage.getFrom(), chatMessage.getText(), "ALL");
	}
}


 class ChatMessage {

	private String from;
	private String text;
	private String recipient;
	private String time;

	public ChatMessage() {

	}

	public ChatMessage(String from, String text, String recipient) {
		this.from = from;
		this.text = text;
		this.recipient = recipient;
	}

	 public String getFrom() {
		 return from;
	 }

	 public void setFrom(String from) {
		 this.from = from;
	 }

	 public String getText() {
		 return text;
	 }

	 public void setText(String text) {
		 this.text = text;
	 }

	 public String getRecipient() {
		 return recipient;
	 }

	 public void setRecipient(String recipient) {
		 this.recipient = recipient;
	 }

	 public String getTime() {
		 return time;
	 }

	 public void setTime(String time) {
		 this.time = time;
	 }
 }