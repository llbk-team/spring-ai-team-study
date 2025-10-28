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
  public AiService1(ChatClient.Builder chatClientBuilder){
    this.chatClient = chatClientBuilder
      .defaultAdvisors(new AdvisorA(), new AdvisorB()) // 공통 부분 전역 처리
      .build();
  }

  public String advisorChain1(String question){
    String answer = chatClient.prompt()
      .user(question)
      .call()
      .content();

    return answer;
  }

  public String advisorChain2(String question){
    String answer = chatClient.prompt()
      .user(question)
      .advisors(new AdvisorC()) // ChatClient에 추가
      .call()
      .content();

    return answer;
  }
}
