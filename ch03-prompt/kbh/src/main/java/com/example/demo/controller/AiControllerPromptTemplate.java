package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.AiServicePromptTemplate;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@Slf4j
public class AiControllerPromptTemplate {
  @Autowired
  private AiServicePromptTemplate aiService;

  @PostMapping(value = "/prompt-template", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_NDJSON_VALUE //
  )
  public Flux<String> promptTemplate(
      @RequestParam("statement") String statement,
      @RequestParam("language") String language) {
    // Flux<String> answer = aiService.PromptTemplate1(statement, language);
    // Flux<String> answer = aiService.PromptTemplate2(statement, language);
    // Flux<String> answer = aiService.PromptTemplate3(statement, language);
    Flux<String> answer = aiService.PromptTemplate4(statement, language);
    return answer;

  }
}
