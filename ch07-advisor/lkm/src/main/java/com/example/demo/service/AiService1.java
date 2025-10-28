package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.demo.advisor.AdvisorA;
import com.example.demo.advisor.AdvisorB;
import com.example.demo.advisor.AdvisorC;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiService1 {
  private ChatClient chatClient;

  // 기본 Advisor로 추가하기
  public AiService1(ChatClient.Builder chatClienBuilder) {
    this.chatClient = chatClienBuilder
      .defaultAdvisors(new AdvisorA(), new AdvisorB())
      .build();
  }

  // 모델 호출 및 응답
  public String advisorChain1(String question) {
    String answer = chatClient.prompt()
      .user(question)
      .call()
      .content();

    return answer;
  }

  // AdvisorC를 ChatClient에 추가하여 모델 호출 및 응답
  public String advisorChain2(String question) {
    String answer = chatClient.prompt()
      .user(question)
      .advisors(new AdvisorC())
      .call()
      .content();

    return answer;
  }
}
