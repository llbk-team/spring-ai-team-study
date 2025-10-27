package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.ImageMessage;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.dto.OpenAiImageEditResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AiService {
  // ChatClient 얻기
  private ChatClient chatClient;

  // 이미지 생성형 모델 얻기
  @Autowired
  private ImageModel imageModel;

  public AiService(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  // 이미지 분석
  public Flux<String> imageAnalysis(String question, String contentType, byte[] bytes) {
    // 시스템 메시지 생성
    SystemMessage systemMessage = SystemMessage.builder()
      .text("""
          당신은 이미지 분석 전문가입니다.
          사용자의 질문에 맞게 이미지를 분석하고 답변을 한국어로 하세요.
        """)
      .build();

    // 리소스 객체 생성(바이트 배열 처리)
    Resource resource = new ByteArrayResource(bytes);

    // 미디어 객체 생성
    // 방법 1
    Media media = Media.builder()
      .mimeType(MimeType.valueOf(contentType))
      .data(resource)
      .build();

    // 방법 2
    // Media media2 = new Media(MimeType.valueOf(contentType), resource);

    // 사용자 메시지 생성
    UserMessage userMessage = UserMessage.builder()
      .text(question)
      .media(media)
      .build();

    // Stream으로 프롬프트 생성
    Flux<String> fluxString = chatClient.prompt()
      .messages(systemMessage, userMessage)
      .stream()
      .content();

    return fluxString;
  }

  // 이미지 생성
  public String generateImage(String description) {
    // 한글-영어 변환하는 메소드 호출
    String englishDescription = koToEn(description);

    // 이미지 생성에 필요한 텍스트 (+ 가중치)
    // 방법 1
    List<ImageMessage> imageMessageList = new ArrayList<>();
    ImageMessage imageMessage = new ImageMessage(englishDescription);
    imageMessageList.add(imageMessage);

    // 방법 2
    // ImageMessage imageMessage2 = new ImageMessage(englishDescription);
    // List<ImageMessage> listImageMessages = List.of(imageMEssage);

    // 이미지 옵션 지정
    ImageOptions imageOptions = OpenAiImageOptions.builder()
      .model("gpt-image-1")
      .width(1536)
      .height(1024)
      .N(1)
      .build();

    // 프롬프트 생성
    ImagePrompt imagePrompt = new ImagePrompt(imageMessageList, imageOptions);

    // 모델 호출 및 응답
    ImageResponse imageResponse = imageModel.call(imagePrompt);

    // 이미지 객체 얻은 후 base64 문자열로 변환
    String b64Json = imageResponse.getResult().getOutput().getB64Json();

    return b64Json;
    
  }

  // 한->영
  public String koToEn(String str) {
    String translatedStr = chatClient.prompt()
      .system("당신은 번역사입니다. 사용자의 한국어 질문을 영어 질문으로 변환시켜주세요.")
      .user(str)
      .call()
      .content();

    return translatedStr;
  }

  // Spring AI에서는 이미지 생성만 가능하고, 편집 기능은 제공 X
  // 이미지 편집은 직접 OpenAI API를 이용해야 함 (시스템 환경 변수 이용)
  @Value("${spring.ai.openai.api-key}")
  private String openAiApiKey;
  
  // 이미지 편집
  public String editImage(String description, byte[] originalImage, byte[] maskImage) {
    
    // OpenAI의 API 이용하기 위해 WebClient 생성
    WebClient webClient = WebClient.builder()
      // 이미지 편집을 위한 엔드포인트 설정
      .baseUrl("https://api.openai.com/v1/images/edits")  
      // Authorization 헤더 추가
      .defaultHeader("Authorization", "Bearer " + openAiApiKey)
      // base64로 인코딩된 이미지 데이터를 처리하기 위해 메모리 사이즈 늘리기
      .exchangeStrategies(ExchangeStrategies.builder()
        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(1024*1024*20))
        .build()
      )
      .build();

    // 파일 이름 & Content type 얻기
    // 익명 자식 객체를 상속받아 메소드를 재정의
    Resource originalResource = new ByteArrayResource(originalImage) {
      @Override
      public String getFilename() {
        return "original.png";
      }
    };
    Resource maskResource = new ByteArrayResource(maskImage) {
      @Override
      public String getFilename() {
        return "mask.png";
      }
    };

    // 이미지 모델 옵션 설정
    // MultiValueMap: 하나의 key에 여러 value를 등록할 수 있음
    MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
    // 문자파트
    multiValueMap.add("model", "gpt-image-1");
    multiValueMap.add("prompt", koToEn(description));
    multiValueMap.add("n", "1");
    multiValueMap.add("size", "1536x1024");
    multiValueMap.add("quality", "low");
    // 파일파트
    multiValueMap.add("image", originalResource);
    multiValueMap.add("mask", maskResource);

    // 비동기로 이미지 하나를 요청하기 때문에 Mono를 사용(Flux가 아니라)
    // {"data": [{"url": "xxxxx", "b64_json": "xxxxx"}, ... ]}
    Mono<OpenAiImageEditResponse> mono = webClient.post()
      .contentType(MediaType.MULTIPART_FORM_DATA) // 텍스트 + 이미지
      // 멀티파트 데이터로 body에 넣어줌
      .body(BodyInserters.fromMultipartData(multiValueMap)) 
      // 서버로 실제 요청을 보내고 응답을 받음
      .retrieve()
      // 해당 객체 타입으로 비동기로 오는 데이터를 받음
      .bodyToMono(OpenAiImageEditResponse.class);

    // 비동기 응답이 완료될 때까지 기다리고 완료된 객체 얻기
    // 비동기를 동기 방식으로 기다리기
    OpenAiImageEditResponse editResponse = mono.block();
    // base64 문자열로 변환
    String b64Json = editResponse.getData().get(0).getB64_json();

    return b64Json;
  }
  
}
