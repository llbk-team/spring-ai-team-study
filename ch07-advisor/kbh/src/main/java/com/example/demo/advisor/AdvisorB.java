package com.example.demo.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.core.Ordered;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdvisorB implements CallAdvisor{
  @Override
  public String getName() { // 이름
    return this.getClass().getSimpleName();
  }
  @Override
  public int getOrder() { // 우선 순위
    return Ordered.HIGHEST_PRECEDENCE + 2;
  }

  // 동기 방식의 전·후처리 작업
  @Override
  public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
    // 전처리
    log.info("[전처리]");
    ChatClientResponse chatClientResponse =callAdvisorChain.nextCall(chatClientRequest);
    // 후처리
    log.info("[후처리]");

    return chatClientResponse;
  }

}
