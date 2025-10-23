package com.example.demo.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiServiceMultiMessages {
  private ChatClient chatClient;
  public AiServiceMultiMessages(ChatClient.Builder chatClientBuilder){
    this.chatClient = chatClientBuilder.build();
  }

  public String multiMessages(String question, List <Message> chatMemory){
    // 시스템 메시지 생성
    SystemMessage systemMessage = SystemMessage.builder()
      .text("""
          당신은 AI 비서입니다.
          제공되는 지난 대화 내용을 보고 우선적으로 답변해주세요.
          """)
      .build();

    // 시스템 메시지는 제일 첫 메시지로 딱 한번만 저장
    if(chatMemory.size() == 0){
      chatMemory.add(systemMessage);
    }

    //대화 메시지 저장
    UserMessage userMessage = UserMessage.builder()
      .text(question)
      .build();
    chatMemory.add(userMessage);

    // LLM에게 요청하고 응답받기
    ChatResponse chatResponse = chatClient.prompt()
      .messages(chatMemory)
      .user(question)
      .call()
      .chatResponse(); // 대화 기록, 전체 응답 정보까지 받을때는 chatResponse()를 사용함
    
    // 응답 메시지 저장
    AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
    chatMemory.add(assistantMessage);

    return assistantMessage.getText();
  }

}
