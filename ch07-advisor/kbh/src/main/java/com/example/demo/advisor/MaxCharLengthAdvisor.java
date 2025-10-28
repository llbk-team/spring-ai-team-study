package com.example.demo.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MaxCharLengthAdvisor implements CallAdvisor {
  private int order;
  private int maxCharLength = 300; // 문자 수 제한
  public static final String MAX_CHAR_LENGTH = "maxCharLength"; // 공유 객체의 키 상수

  public MaxCharLengthAdvisor(int order){ // 고정 값이 아닌 생성자로 받음
    this.order = order;
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }
  @Override
  public int getOrder() {
    return order;
  }

  @Override
  public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
    // 전처리(있음)
    // 최대 문자수 제한이 반영된 새로운 ChatClientRequest를 얻음
    ChatClientRequest mutatedRequest = augmentPrompt(chatClientRequest);

    ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(mutatedRequest);
    // 후처리(없음)
    return chatClientResponse;
  }

  private ChatClientRequest augmentPrompt(ChatClientRequest chatClientRequest){
    String userText = this.maxCharLength + "자 이내로 답변해주세요.";
    Integer maxCharLength = (Integer) chatClientRequest.context().get(MAX_CHAR_LENGTH);
    if(maxCharLength != null){
      userText = maxCharLength + "자 이내로 답변해주세요.";
    }

    String finalUserText = userText;

    // 이전 프롬프트 읽기
    Prompt prevPrompt = chatClientRequest.prompt();

    // UserMessage만 보강된 새로운 프롬프트 생성
    Prompt newPrompt = prevPrompt.augmentUserMessage(userMessage -> 
    UserMessage.builder()
    // 람다식 안에서는 로컬 변수가 final이어야 하므로, 별도 final 변수(finalUserText)에 담아 사용함
      .text(userMessage.getText() + "\n" + finalUserText)
      .build()
    );

    // 변경된 ChatClientRequest 생성
    ChatClientRequest newChatClientRequest = chatClientRequest.mutate()
      .prompt(newPrompt)
      .build();

    return newChatClientRequest;
  }
  
}
