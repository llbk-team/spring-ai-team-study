package com.example.demo.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.core.Ordered;

import lombok.extern.slf4j.Slf4j;

//이거는 스프링 빈이 아님
@Slf4j
public class AdvisorC implements CallAdvisor{@Override
  public String getName() {
    return this.getClass().getSimpleName();
   
  }

@Override
public int getOrder() {
  return Ordered.HIGHEST_PRECEDENCE+3; //우선순위 젤 높은거 다음번
}

@Override
public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
  //ChatClientRequest chatClientRequest A가 추가한 내용 B가 추가한 내용이 들어가있다.
  //callAdvisorChain 현재 advisor를 실행하고 chain을 통해서 넘어간다.
  
  //전처리
  log.info("[전처리]");
  ChatClientResponse chatClientResponse=callAdvisorChain.nextCall(chatClientRequest);
  //후처리
  log.info("[후처리]");
  //..
  return chatClientResponse;
}
  

  

}
