package com.example.demo.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionFinishReason;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest;
import org.springframework.ai.openai.api.OpenAiApi.ChatCompletionRequest.AudioParameters;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class AiService {

  private ChatClient chatClient;
  private OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel; // STT
  private OpenAiAudioSpeechModel openAiAudioSpeechModel; // TTS

  public AiService(ChatClient.Builder chatClientBuilder,
      OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel,
      OpenAiAudioSpeechModel openAiAudioSpeechModel) {
    this.chatClient = chatClientBuilder.build();
    this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
    this.openAiAudioSpeechModel = openAiAudioSpeechModel;
  }

  public String stt(String fileName, byte[] bytes) {
    // 음성 데이터를 ByteArrayResource로 생성
    // Spring AI 1.0.3은 파일 이름이 필수로 추가되어야 함
    Resource audioResource = new ByteArrayResource(bytes) {
      @Override
      public String getFilename() {
        return fileName;
      }
    };
    // 옵션 설정
    AudioTranscriptionOptions audioTranscriptionOptions = OpenAiAudioTranscriptionOptions.builder()
        .model("whisper-1")
        .language("ko")
        .build();

    // 프롬프트 생성
    AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource, audioTranscriptionOptions);

    AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(prompt);
    String text = response.getResult().getOutput();

    return text;
  }

  public byte[] tts(String text) {
    // 옵션 설정
    OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
        .model("gpt-4o-mini-tts")
        .voice(SpeechRequest.Voice.ECHO)
        .speed(1.0f)
        .responseFormat(SpeechRequest.AudioResponseFormat.MP3)
        .build();

    // 프롬프트 생성
    SpeechPrompt speechPrompt = new SpeechPrompt(text, speechOptions);

    // 모델에 요청하고 응답하기
    SpeechResponse speechResponse = openAiAudioSpeechModel.call(speechPrompt);
    byte[] bytes = speechResponse.getResult().getOutput();

    return bytes;
  }

  public Map<String, String> chatText(String question) {
    // LLM 사용
    String textAnswer = chatClient.prompt()
        .system("100자 이내로 한국어로 답변해.")
        .user(question)
        .call()
        .content();

    // TTS 사용
    byte[] audio = tts(textAnswer);
    String base64Audio = Base64.getEncoder().encodeToString(audio);

    // Map에 담기
    Map<String, String> map = new HashMap<>();
    map.put("text", textAnswer);
    map.put("audio", base64Audio);

    return map;
  }

  public Flux<byte[]> ttsFlux(String text) {
    // 옵션 설정
    OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
        .model("gpt-4o-mini-tts")
        .voice(SpeechRequest.Voice.ASH)
        .speed(1.0f)
        .responseFormat(SpeechRequest.AudioResponseFormat.MP3)
        .build();

    // 프롬프트 생성
    SpeechPrompt speechPrompt = new SpeechPrompt(text, speechOptions);

    Flux<SpeechResponse> fluxSpeechResponse = openAiAudioSpeechModel.stream(speechPrompt);
    Flux<byte[]> fluxBytes = fluxSpeechResponse.map(speechResponse -> speechResponse.getResult().getOutput());
    return fluxBytes;
  }

  public Flux<byte[]> chatVoiceSttLlmTts(byte[] audioBytes) {
    // 음성 -> STT -> 텍스트
    String textQuestion = stt("speech.mp3", audioBytes);

    // 텍스트 -> LLM -> 텍스트
    String textAnswer = chatClient.prompt()
        .system("50자 이내로 답변해")
        .user(textQuestion)
        .call()
        .content();

    // 텍스트 -> TTS -> 음성
    Flux<byte[]> fluxBytes = ttsFlux(textAnswer);

    return fluxBytes;
  }

  public byte[] chatVoiceOneModel(byte[] audioBytes, String contentType) {
    Resource resource = new ByteArrayResource(audioBytes) {
      @Override
      public String getFilename() {
        return "speech.mp3";
      }
    };

    UserMessage userMessage = UserMessage.builder()
      .text("제공되는 음성에 맞는 자연스러운 대화로 이어주세요.")
      .media(new Media(MimeType.valueOf(contentType), resource))
      .build();

    ChatOptions chatOptions = OpenAiChatOptions.builder()
      .model(OpenAiApi.ChatModel.GPT_4_O_MINI_AUDIO_PREVIEW)
      .outputModalities(List.of("text", "audio")) // 원하는 데이터를 받음
      .outputAudio(new AudioParameters(
        ChatCompletionRequest.AudioParameters.Voice.ASH,
        ChatCompletionRequest.AudioParameters.AudioResponseFormat.MP3
      ))
      .build();

    // 스트리밍을 지원하지 않기 때문에 동기 방식
    ChatResponse response = chatClient.prompt()
      .system("50자 이내로 답변")
      .messages(userMessage) // 음성이기 때문에 text를 줄 수 없다
      .options(chatOptions)
      .call()
      .chatResponse();

    AssistantMessage assistantMessage = response.getResult().getOutput();
    String textAnswer = assistantMessage.getText(); // 텍스트 답변
    log.info("텍스트 응답: "+ textAnswer);
    byte[] audioAnswer = assistantMessage.getMedia().get(0).getDataAsByteArray(); // 음성 답변

    return audioAnswer;
  }

}
