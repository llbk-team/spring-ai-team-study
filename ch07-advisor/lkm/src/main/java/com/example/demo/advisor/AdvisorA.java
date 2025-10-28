package com.example.demo.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.core.Ordered;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdvisorA implements CallAdvisor {
  
  // Advisor 이름 얻기
  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  // Advisor의 실행 순서
  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE - 1;
  }

  // ChatClientRequest 가로채고, 다음 CallAdvisor를 실행시키기
  @Override
  public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
    // 전처리
    log.info("[여기는 전처리 코드]");

    // 이 코드를 기준으로 전/후처리 나누기
    // 다음 CallAdvisor를 실행시키기
    ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

    // 후처리
    log.info("[여기는 후처리 코드]");

    return chatClientResponse;
  }

}
