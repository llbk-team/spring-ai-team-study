package com.example.demo.service;

import java.util.List;
import java.util.Objects;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiServiceStepBackPrompt {
private ChatClient chatClient;

  public AiServiceStepBackPrompt(ChatClient.Builder chatclientBuilder) {
    this.chatClient = chatclientBuilder.build();
  }

  public String stepBackPrompt(String question) throws Exception{
    String questions = chatClient.prompt()
      .user("""
          사용자 질문을 처리하기 Step-Back 프롬프트 기법을 사용하려고 한다.
          사용자 질문을 단계별 질문들로 재구성해 주세요.
          맨 마지막 질문은 사용자 질문과 일치해야 합니다.
          단계별 질문을 항목으로 하는 JSON 배열로 출력해 주세요.
          예시: ["...", "...", "...","..."]
          사용자 질문: %s
          """.formatted(question))
      .call()
      .content();
    // ["...", "...", "...","..."] 추출하기
    String json = questions.substring(
      questions.indexOf("["),
      questions.indexOf("]")+1
    );

    // List<String> 변환하기
    ObjectMapper objectMapper = new ObjectMapper();
    List<String> listQuestion = objectMapper.readValue(
      json, new TypeReference<List<String>>() {});
    
    // 단계별 질문에 대한 답변을 얻고 다음 단게 질문에 포함시키기
    String[] answerArray = new String[listQuestion.size()];
    for(int i=0; i<listQuestion.size(); i++){
      String stepQuestion = listQuestion.get(i);
      String stepAnswer = getStepAnswer(stepQuestion, answerArray);
      answerArray[i] = stepAnswer;
    }
    String finalAnswer = answerArray[answerArray.length-1];
    return finalAnswer;
  }
  public String getStepAnswer(String question, String[] answerArray){
    // 이전 답변글을 모두 context로 누적
    String context = "";
    for(String answer : answerArray){
      context +=Objects.requireNonNullElse(answer, "");
    }
    String answer = chatClient.prompt()
      .user("""
          다음 question에 대한 답변을 context를 기반으로 답변해 주세요.
          context가 없다면 인터넷을 서칭해서 답변을 해주세요.
          question: %s
          context: %s
          """.formatted(question, context))
      .call()
      .content();
      return answer;
  }
}
