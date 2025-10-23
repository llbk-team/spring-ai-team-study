package com.example.demo.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Hotel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiServiceBeanOutputConverter {
  private ChatClient chatClient;
  public AiServiceBeanOutputConverter(ChatClient.Builder chatClientBuilder){
    this.chatClient = chatClientBuilder.build();
  }

  public Hotel beanOutputConverterLowLevel(String city){
    // 구조화된 출력 변환기 생성
    BeanOutputConverter<Hotel> converter = new BeanOutputConverter<>(Hotel.class);
    
    // 프롬프트 템플릿 생성
    PromptTemplate promptTemplate = PromptTemplate.builder()
    .template("""
      {city}에서 유명한 호텔 목록 5개를 호출하세요.
      형식: {format}
      """)
      .build();
      
      String json = chatClient.prompt()
      .user(promptTemplate.render(Map.of("city", city, "format", converter.getFormat())))
      .call()
      .content();
      
      Hotel hotel = converter.convert(json);
      
      return hotel;
    }
    
    public Hotel beanOutputConverterHighLevel(String city){
      Hotel hotel = chatClient.prompt()
        .user("%s에서 유명한 호텔 목록 5개를 출력하시오.".formatted(city))
        .call()
        .entity(Hotel.class);

      return hotel;
    }
}
