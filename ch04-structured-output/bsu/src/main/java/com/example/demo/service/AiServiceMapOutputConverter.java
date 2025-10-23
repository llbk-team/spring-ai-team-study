package com.example.demo.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AiServiceMapOutputConverter {
    private ChatClient chatClient;

    public AiServiceMapOutputConverter(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Map<String, Object> mapOutputConverterLowLevel(String hotel) {
        MapOutputConverter mapOutputConverter = new MapOutputConverter();

        PromptTemplate promptTemplate = new PromptTemplate(
                "호텔 {hotel}에 대한 정보를 알려주세요. {format}");

        Prompt prompt = promptTemplate.create(Map.of("hotel", hotel, "format", mapOutputConverter.getFormat()));

        String json = chatClient.prompt(prompt)
                .call()
                .content();

        Map<String, Object> hotelInfo = mapOutputConverter.convert(json);

        return hotelInfo;
    }

    public Map<String, Object> mapOutputConverterHighLevel(String hotel) {
        Map<String, Object> hotelInfo = chatClient.prompt()
                .user("호텔 %s에 대한 정보를 알려주세요.".formatted(hotel))
                .call()
                .entity(new MapOutputConverter());

        return hotelInfo;
    }
}
