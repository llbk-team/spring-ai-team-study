package com.example.demo.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MaxCharLengthAdvisor implements CallAdvisor{
  
  private int order;
  private int maxCharLength =300;
  //private static final String MAX_CHAR_LENGTH = "maxCharLength";
  public static final String MAX_CHAR_LENGTH = "maxCharLength";


  public MaxCharLengthAdvisor(int order){
    this.order=order;
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
    //전처리(o)
    ChatClientRequest mutatedRequest = argumentPrompt(chatClientRequest);//chatClientRequest는 변경이 안되니까 보강된 새로운 거를 만들기 위해서 추가함
    
    ChatClientResponse chatClientResponse=callAdvisorChain.nextCall(mutatedRequest);
    
    //후처리(x)

    return chatClientResponse;
  }

  private ChatClientRequest argumentPrompt(ChatClientRequest chatClientRequest){
    String userText = this.maxCharLength+"자 이내로 답변해주세요.";
    Integer maxCharLength = (Integer) chatClientRequest.context().get(MAX_CHAR_LENGTH);

    //Integer maxCharLength = (Integer) request.context().get(MAX_CHAR_LENGH);
    if(maxCharLength!=null){
      userText = maxCharLength+"자 이내로 답변해주세요.";
    }
    //변수에 저장 
    String finalUserText = userText;

    //프롬프트 만들기
    Prompt prevPrompt= chatClientRequest.prompt();
    Prompt newPrompt = prevPrompt.augmentUserMessage(userMessage->{
      return UserMessage.builder()
      .text(userMessage.getText()+"\n"+finalUserText)
      .build();
    });

    //chatClientRequest 안에 들어있는 프롬프트를 뉴 프롬프트로 변경하겠다.
    ChatClientRequest newChatClientRequest = chatClientRequest.mutate()
    .prompt(newPrompt)  
    .build();

    return newChatClientRequest;


  }



}
