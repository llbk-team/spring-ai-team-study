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
  public AiServiceMapOutputConverter(ChatClient.Builder chatClientBuilder){
    this.chatClient = chatClientBuilder.build();
  }

  public Map<String, Object> mapOutputConverterLowLevel(String hotel){
    // 구조화된 출력 변환기 생성
    MapOutputConverter mapOutputConverter = new  MapOutputConverter();

    // 프롬프트 템플릿 생성
    PromptTemplate promptTemplate = PromptTemplate.builder()
      .template("호텔 {hotel}에 대해 정보를 다음 형식으로 알려주세요.\n {format}")
      .build();

    // LLM의 JSON 출력 얻기
    String json = chatClient.prompt()
      .user(promptTemplate.render(Map.of(
        "hotel", hotel,
        "format", mapOutputConverter.getFormat()
      )))
      .call()
      .content();

    // 변환
    Map<String, Object> map = mapOutputConverter.convert(json);

    return map;
  }

  public Map<String, Object> mapOutputConverterHighLevel(String hotel){
    Map<String, Object> map = chatClient.prompt()
      .user("호텔 %s에 대해 정보를 알려주세요.".formatted(hotel))
      .call()
      .entity(new MapOutputConverter());
    return map;
  }

}
