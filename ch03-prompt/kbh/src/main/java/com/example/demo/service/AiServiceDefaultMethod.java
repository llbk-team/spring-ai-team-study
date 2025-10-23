package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AiServiceDefaultMethod {
  private ChatClient chatClient;
  public AiServiceDefaultMethod(ChatClient.Builder chatClientBuilder){
    // 디폴트 메시지 생성
    this.chatClient = chatClientBuilder
      .defaultSystem("적절한 감탄사, 웃음을 넣어서 친절하게 대화를 해주세요.")
      .defaultOptions(ChatOptions.builder()
        .temperature(1.0)
        .maxTokens(300)
        .build()
      )
      .build();
  }

  public Flux<String> defaultMethod(String question){
    Flux<String> fluxString = chatClient.prompt()
      .user(question)
      .stream()
      .content();
    return fluxString;
  }
}
