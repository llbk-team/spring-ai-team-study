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
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.dto.OpenAIImageEditResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AiService {
  private ChatClient chatClient;

  @Autowired
  private ImageModel imageModel;

  public AiService(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  // 이미지 분석
  public Flux<String> imageAnalysis(String question, String contentType, byte[] bytes) {
    SystemMessage systemMessage = SystemMessage.builder()
        .text("""
            당신은 이미지 분석 전문가입니다.
            사용자의 질문에 맞게 이미지를 분석하고 답변을 한국어로 하세요
            """)
        .build();

    // 다양한 데이터를 하나의 공통 방식으로 읽고 전송
    Resource resource = new ByteArrayResource(bytes);
    // 이미지나 오디오같은 미디어 데이터를 AI 모델에 전달
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
    String englishDescription = koToEn(description);

    // ImageModel(이미지 생성 모델)에 전달할 입력 메시지
    // ImageMessage : 프롬프트 리스트, 중요도
    List<ImageMessage> imageMessageList = new ArrayList<>();
    ImageMessage imageMessage = new ImageMessage(englishDescription);
    imageMessageList.add(imageMessage);

    ImageOptions imageOptions = OpenAiImageOptions.builder()
        .model("gpt-image-1")
        .width(1024)
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

  // 이미지 편집
  public String editImage(String description, byte[] originalImage, byte[] maskImage) {

    WebClient webClient = WebClient.builder()
        .baseUrl("https://api.openai.com/v1/images/edits")
        .defaultHeader("Authorization", openAiApiKey)
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(1024 * 1024 * 20))
            .build())
        .build();

    // 익명 클래스
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

    // multipart/form-data 형식의 요청을 만들 때 쓰이는 기본 문장
    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
    form.add("model", "gpt-image-1");
    form.add("image", originalResource);
    form.add("mask", maskResource);
    form.add("n", "1");
    form.add("size", "1536x1024");
    form.add("quality", "low");

    // webClient : RestTemplate이 동기식, webClient는 비동기
    // 결과를 Mono나 Flux 형태로 감싸서 나중에 받을 수 있음
    Mono<OpenAIImageEditResponse> mono = webClient.post()
        .contentType(MediaType.MULTIPART_FORM_DATA) // 폼데이터
        .body(BodyInserters.fromMultipartData(form))
        .retrieve()
        .bodyToMono(OpenAIImageEditResponse.class);

    OpenAIImageEditResponse editResponse = mono.block(); // block = await랑 비슷
    String b64Json = editResponse.getData().get(0).getB64_json();

    return b64Json;
  }

}