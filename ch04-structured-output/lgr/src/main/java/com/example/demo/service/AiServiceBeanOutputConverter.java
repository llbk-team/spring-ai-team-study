package com.example.demo.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Hotel;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiServiceBeanOutputConverter {
  private ChatClient chatClient;

  public AiServiceBeanOutputConverter(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  // dto Hotel
  public Hotel beanOutputConverterLowLevel(String city) {

    BeanOutputConverter<Hotel> converter = new BeanOutputConverter<>(Hotel.class); // 객체를 생성할때 hotel.class를 매개값으로
                                                                                   // 전달해야한다. dto를 분석하고 출력 ㅣㅈ침을 만들기 위해서

    // 프롬프트 템플릿 생성
    PromptTemplate promptTemplate = PromptTemplate.builder()
        .template("""
            {city}에서 유명한 호텔 목록 5개를 다음 형식으로 출력하세요.
            형식:{format}
            """)
        .build();

    String json = chatClient.prompt()
        .user(promptTemplate.render(Map.of("city", city, "format", converter.getFormat())))
        .call()
        .content();// content 넣으면 string

    Hotel hotel = converter.convert(json);
    // 바로 호텔이 리턴된다.
    return hotel;
  }

  public Hotel beanOutputConverterHighLevel(String city) {
     

        Hotel hotel = chatClient.prompt()
          .user("%s에서 유명한 호텔 목록 5개를 다음으로 출력하세요. ".formatted(city))
          .call()
          .entity(Hotel.class);

        return hotel;
  }

}
