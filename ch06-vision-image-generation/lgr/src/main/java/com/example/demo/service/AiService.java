package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
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

  public AiService(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  @Autowired
  private ImageModel imageModel;

  // ##### 이미지 분석 메소드 #####
  // 그림을 올려주고 글자로 사진속의 배경의 위치 정보를 알려줘(입력 데이터= 사진contenttype, 사진속의 배경이라는
  // 텍스트question,bytes 바이너리데이터)
  public Flux<String> imageAnalysis(String question, String contentType, byte[] bytes) {
    SystemMessage systemMessage = SystemMessage.builder()
        .text("""
            당신은 이미지 분석 전문가입니다.
                  사용자 질문에 맞게 이미지를 분석하고 답변을 한국어로 하세요.
            """)
        .build();

    // 미디어 생성
    Media media = Media.builder()
        .mimeType(MimeType.valueOf(contentType))
        .data(new ByteArrayResource(bytes))
        .build();
    // 사용자 메시지 생성
    UserMessage userMessage = UserMessage.builder()
        .text(question)
        .media(media)
        .build();
    // 프롬프트 생성
    Prompt prompt = Prompt.builder()
        .messages(systemMessage, userMessage)
        .build();
    // LLM에 요청하고 응답받기
    Flux<String> flux = chatClient.prompt(prompt)
        .stream()
        .content();
    return flux;

  }

  public String koToEn(String str) {
    String translatedStr = chatClient.prompt()
        .system("너는 통번역가이다. 사용자의 한글 질문을 영어 질문으로 변환시켜야한다.")
        .user(str)
        .call()
        .content();
    return translatedStr;
  }

  public String generateImage(String description) {
    String englishDescription = koToEn(description);

    List<ImageMessage> imageMessageList = new ArrayList<>();
    ImageMessage imageMessage = new ImageMessage(englishDescription);
    imageMessageList.add(imageMessage);

    // List<ImageMessage> listImagemessages = List.of(imageMessage);

    // ImageOptions imageOptions = OpenAiImageOptions.builder()
    // .model("dall-e-3")
    // .responseFormat("b64_json")
    // .width(1024)
    // .height(1024)
    // .N(1)
    // .build();

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

  @Value("${OPENAI_API_KEY}")
  private String openAiApiKey;

  public String editImages(String description, byte[] originalImage, byte[] maskImage) {
    String englishDescription = koToEn(description);

    WebClient webClient = WebClient.builder()
        .baseUrl("https://api.openai.com/v1/images/edits")
        .defaultHeader("Authorization", "Bearer" + openAiApiKey)
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(24 * 1024 * 1024))
            .build())
        .build();

        Resource originalResource = new ByteArrayResource(originalImage){
          
        };
        Resource maskResource = new ByteArrayResource(maskImage){
          
        };

        MultiValueMap<String,Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("model","gpt-image-1");
        multiValueMap.add("image",originalResource);
        multiValueMap.add("mask",maskResource);
        multiValueMap.add("prompt",englishDescription);
        multiValueMap.add("n", "1");
        multiValueMap.add("size", "1536x1024");
        multiValueMap.add("quality", "low");

        Mono<OpenAIImageEditResponse> mono = webClient.post()
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .body(BodyInserters.fromMultipartData(multiValueMap))
          .retrieve()
          .bodyToMono(OpenAIImageEditResponse.class);

          OpenAIImageEditResponse editResponse = mono.block();

          String b64Json = editResponse.getData().get(0).getB64_json();

          return b64Json;

  }

}
