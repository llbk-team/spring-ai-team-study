package com.example.demo.service;



import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AiService {
  private ChatClient chatClient;
  public AiService(ChatClient.Builder chatClientBuilder){
    this.chatClient = chatClientBuilder.build();
  }

  public Flux<String> imageAnalysis(String question, String contentType, byte[] bytes){
    SystemMessage systemMessage = SystemMessage.builder()
      .text("""
          당신은 이미지 분석가이다.
          사용자의 질문에 맞게 이미지를 분석하고 답변을 한국어로 해라.
          """)
      .build();
    
    // ByteArrayResource -> 실제 파일이 아닌 메모리 상의 byte 데이터를 파일 형태처럼 다루게 해주는 객체
    Resource resource = new ByteArrayResource(bytes);

    // 미디어는 사용자 메시지, AI 메시지에 포함하므로 따로 생성
    Media media = Media.builder()
      .mimeType(MimeType.valueOf(contentType))
      .data(resource)
      .build();
    
    UserMessage userMessage = UserMessage.builder()
      .text(question)
      .media(media)
      .build();

    Flux<String> fluxString = chatClient.prompt()
      .messages(systemMessage, userMessage)
      .stream()
      .content();

      return fluxString;
  }

}
