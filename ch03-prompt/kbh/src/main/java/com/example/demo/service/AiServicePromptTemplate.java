package com.example.demo.service;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AiServicePromptTemplate {
  private ChatClient chatClient;
  private PromptTemplate systemTemplate = SystemPromptTemplate.builder()
      .template("""
              답변을 생성할 때 HTML와 CSS를 사용해서 파란 글자로 출력하세요.
              <span> 태그 안에 들어갈 내용만 출력하세요.
          """)
      .build();
  private PromptTemplate userTemplate = PromptTemplate.builder()
      .template("""
            다음 한국어 문장을 {language}로 번역해 주세요.
            문장: {statement}
          """)
      .build();

  public AiServicePromptTemplate(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  public Flux<String> PromptTemplate1(String statement, String language) {
    Prompt prompt = userTemplate.create(
        Map.of("statement", statement, "language", language));
    Flux<String> fluxString = chatClient.prompt(prompt)
        .stream()
        .content();
    return fluxString;
  }

  public Flux<String> PromptTemplate2(String statement, String language) {
    Flux<String> fluxString = chatClient.prompt()
        .messages(
            systemTemplate.createMessage(),
            userTemplate.createMessage(Map.of("statement", statement, "language", language)))
        .stream()
        .content();
    return fluxString;
  }

  public Flux<String> PromptTemplate3(String statement, String language) {
    Flux<String> fluxString = chatClient.prompt()
        .system(systemTemplate.render())
        .user(userTemplate.render(Map.of("statement", statement, "language", language)))
        .stream()
        .content();
    return fluxString;
  }

  public Flux<String> PromptTemplate4(String statement, String language) {
    String systemText = """
        답변을 생성할 때 HTML와 CSS를 사용해서 파란 글자로 출력하세요.
        <span> 태그 안에 들어갈 내용만 출력하세요.
        """;

    String userText = """
        다음 한국어 문장을 %s로 번역해 주세요.
        문장: %s
        """.formatted(language, statement);
        
    Flux<String> fluxString = chatClient.prompt()
        .system(systemText)
        .user(userText)
        .stream()
        .content();
    return fluxString;
  }

}
