package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ReviewClassification;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiServiceSystemMessage {
  private ChatClient chatClient;

  public AiServiceSystemMessage(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  public ReviewClassification classifyReview(String review) {
    ReviewClassification result = chatClient.prompt()
    // 시스템 메시지 전달
    .system("""
        영화 리뷰를 [POSITIVE, NEUTRAL, NEGATIVE] 중에서 하나로 분류하고,
        유효한 JSON을 반환하세요.
      """)
      // 사용자 메시지로 형식 전달
      .user("%s".formatted(review))
      // 대화 옵션 전달
      .options(ChatOptions.builder()
        .temperature(0.0)
        .build()
      )
      // 응답
      .call()
      // JSON + 자바 객체로 변환
      .entity(ReviewClassification.class);

    return result;
  }
}
