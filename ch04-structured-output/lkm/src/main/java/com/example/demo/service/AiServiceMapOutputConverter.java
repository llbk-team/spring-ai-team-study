package com.example.demo.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiServiceMapOutputConverter {
  private ChatClient chatClient;

  public AiServiceMapOutputConverter(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  // 저수준
  public Map<String, Object> mapOutputConverterLowLevel(String hotel) {
    // MapOutputConverter 생성
    MapOutputConverter mapOutputConverter = new MapOutputConverter();

    // 프롬프트 템플릿 생성
    PromptTemplate promptTemplate = PromptTemplate.builder()
      .template("호텔 {hotel}에 대한 정보를 다음 형식으로 알려주세요.\n{format}")
      .build();

    // 프롬프트 생성 & JSON으로 변환
    String json = chatClient.prompt()
      .user(promptTemplate.render(Map.of(
        "hotel", hotel,
        "format", mapOutputConverter.getFormat()
      )))
      .call()
      .content();

    // 자바 객체로 변환
    Map<String, Object> map = mapOutputConverter.convert(json);

    return map;
  }

  // 고수준
  public Map<String, Object> mapOutputConverterHighLevel(String hotel) {
    Map<String, Object> map = chatClient.prompt()
      .user("호텔 %s에 대한 정보를 알려주세요.".formatted(hotel))
      .call()
      .entity(new MapOutputConverter());

    return map;
  }
}
