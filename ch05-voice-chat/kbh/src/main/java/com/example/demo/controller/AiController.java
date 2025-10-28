package com.example.demo.controller;

import java.io.OutputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.AiService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import org.springframework.web.bind.annotation.RequestBody;





@RestController
@RequestMapping("/ai")
@Slf4j
public class AiController {
  @Autowired
  private AiService aiService;

  @PostMapping(
    value = "/stt",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
    produces = MediaType.TEXT_PLAIN_VALUE
    )
  public String stt(@RequestParam("speech") MultipartFile speech) throws Exception{
    String originalFileName = speech.getOriginalFilename();
    byte[] bytes = speech.getBytes();

    String text = aiService.stt(originalFileName, bytes);
    return text;
  }

  @PostMapping(
    value = "/tts",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
  public byte[] tts(@RequestParam("text") String text) throws Exception{
    byte[] bytes = aiService.tts(text);
    return bytes;
  }

  @PostMapping(
    value = "/chat-text",
    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
    )
  public Map<String, String> postMethodName(@RequestParam("question") String question) {
    Map<String, String> response = aiService.chatText(question);

    return response;
  }

  @PostMapping(
    value = "/chat-voice-stt-llm-tts",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
    produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
  public void chatVoiceSttLlmTts(@RequestParam("question") MultipartFile question,
  HttpServletResponse response) throws Exception {
    // 실시간으로 음성 데이터 전송
    Flux<byte[]> flux = aiService.chatVoiceSttLlmTts(question.getBytes());
    
    // 출력 스트림 생성
    OutputStream os = response.getOutputStream();

    // Flux 스트림을 순차 반복 가능하게 변환
    for(byte[] chunk : flux.toIterable()){
      os.write(chunk);
      os.flush();
    }
  }

  @PostMapping(
    value = "/chat-voice-one-model",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
    produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
  public byte[] postMethodName(@RequestParam("question") MultipartFile question,
  HttpServletResponse response) throws Exception {
    byte[] bytes = aiService.chatVoiceOneModel(question.getBytes(), question.getContentType());
      return bytes;
  }
  

}
