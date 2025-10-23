package com.example.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.AiServiceMapOutputConverter;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/ai")
@Slf4j
public class AiControllerMapOutputConverter {
  @Autowired
  private AiServiceMapOutputConverter aiService;

  @PostMapping(
    value = "/map-output-converter",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Object> postMethodName(@RequestParam("hotel") String hotel) {
    Map<String, Object> map = aiService.mapOutputConverterLowLevel(hotel);      
    // Map<String, Object> map = aiService.mapOutputConverterHighLevel(hotel);      
    return map;
  }
  
}
