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

import com.example.demo.dto.OpenAIImageEditResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AiService {
  private ChatClient chatClient;

  @Autowired
  private ImageModel imageModel;

  public AiService(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  public Flux<String> imageAnalysis(String question, String contentType, byte[] bytes) {
    SystemMessage systemMessage = SystemMessage.builder()
        .text("""
            당신은 이미지 분석가이다.
            사용자의 질문에 맞게 이미지를 분석하고 답변을 한국어로 해라.
            """)
        .build();

    // ByteArrayResource -> 실제 파일이 아닌 메모리 상의 byte 데이터를 파일 형태처럼 다루게 해주는 객체
    Resource resource = new ByteArrayResource(bytes);

    // 미디어는 사용자 메시지, AI 메시지에 포함하므로 따로 생성
    Media media = Media.builder()
        .mimeType(MimeType.valueOf(contentType))
        .data(resource)
        .build();

    UserMessage userMessage = UserMessage.builder()
        .text(question)
        .media(media)
        .build();

    Flux<String> fluxString = chatClient.prompt()
        .messages(systemMessage, userMessage)
        .stream()
        .content();

    return fluxString;
  }

  public String generateImage(String description) {
    // 한글 질문을 영어 질문으로 번역
    String englishDescription = koToEn(description);

    List<ImageMessage> imageMessageList = new ArrayList<>();
    ImageMessage imageMessage = new ImageMessage(englishDescription);
    imageMessageList.add(imageMessage);

    ImageOptions imageOptions = OpenAiImageOptions.builder()
        .model("gpt-image-1")
        .width(1536)
        .height(1024)
        .N(1)
        .build();

    ImagePrompt imagePrompt = new ImagePrompt(imageMessageList, imageOptions);

    ImageResponse imageResponse = imageModel.call(imagePrompt);
    String b64Json = imageResponse.getResult().getOutput().getB64Json();

    return b64Json;
  }

  public String koToEn(String str) {
    String translatedStr = chatClient.prompt()
        .system("당신은 번역사입니다. 사용자의 한글 질문을 영어 질문으로 변환시키세요.")
        .user(str)
        .call()
        .content();
    return translatedStr;
  }

  @Value("${spring.ai.openai.api-key}")
  private String openAiApiKey;

  public String editImage(String description, byte[] originalImage, byte[] maskImage) {
    String englishDescription = koToEn(description);

    WebClient webClient = WebClient.builder()
        .baseUrl("https://api.openai.com/v1/images/edits")
        .defaultHeader("Authorization", "Bearer " + openAiApiKey)
        // base64로 인코딩된 데이터를 처리하기 위해 메모리 사이즈 늘림
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(1024 * 1024 * 20))
            .build())
        .build();

    // 파일 이름만 주어도 확장명을 통해 파일 타입 구분 가능
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

    MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
    multiValueMap.add("model", "gpt-image-1");
    multiValueMap.add("image", originalResource);
    multiValueMap.add("mask", maskResource);
    multiValueMap.add("prompt", englishDescription);
    multiValueMap.add("n", "1");
    multiValueMap.add("size", "1536x1024");
    multiValueMap.add("quality", "low");

    Mono<OpenAIImageEditResponse> mono = webClient.post()
      // 문자 + 파일을 받기 위해 Multipart
      .contentType(MediaType.MULTIPART_FORM_DATA)
      // 바디도 마찬가지로 멀티파트 데이터를 줌
      .body(BodyInserters.fromMultipartData(multiValueMap))
      // 요청
      .retrieve()
      .bodyToMono(OpenAIImageEditResponse.class);

    // 비동기 응답을 동기식으로 작동시킴
    OpenAIImageEditResponse response = mono.block();

    String b64Json = response.getData().get(0).getB64_json();

    return b64Json;
  }
}
