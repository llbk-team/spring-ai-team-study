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
  // 필드
  private int order;
  private int maxCharLength = 300;
  public static final String MAX_CHAR_LENGTH = "maxCharLength";

  // 생성자
  public MaxCharLengthAdvisor(int order) {
    this.order = order;
  }

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public int getOrder() {
    return order;
  }

  @Override
  public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
    // 전처리
    // 변경된 ChatClientRequest
    ChatClientRequest mutatedRequest = augmentPrompt(chatClientRequest);
    
    ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(mutatedRequest);

    // 후처리(X)

    return chatClientResponse;
  }

  private ChatClientRequest augmentPrompt(ChatClientRequest chatClientRequest) {
    // 보강 내용
    String userText = this.maxCharLength + "자 이내로 답변해주세요.";
    Integer maxCharLength = (Integer) chatClientRequest.context().get(MAX_CHAR_LENGTH);
    // 공유 데이터에 값이 있으면 그 값으로 사용
    if(maxCharLength != null) {
      userText = maxCharLength + "자 이내로 답변해주세요.";
    }

    String finalUserText = userText;

    Prompt prevPrompt = chatClientRequest.prompt();
    Prompt newPrompt = prevPrompt.augmentUserMessage(userMessage -> {
      return UserMessage.builder()
        .text(userMessage.getText() + "\n" + finalUserText)
        .build();
    });

    ChatClientRequest newChatClientRequest = chatClientRequest.mutate()
      .prompt(newPrompt)
      .build();

    return newChatClientRequest;
  }
}
