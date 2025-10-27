package com.example.demo.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi.SpeechRequest;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiService {
  // ChatClient, TTS/STT 얻기
  private ChatClient chatClient;
  private OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;  //STT
  private OpenAiAudioSpeechModel openAiAudioSpeechModel;  //TTS

  // 생성자 주입
  public AiService(
    ChatClient.Builder chatClientBuilder,
    OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel,
    OpenAiAudioSpeechModel openAiAudioSpeechModel
  ) {
    this.chatClient = chatClientBuilder.build();
    this.openAiAudioTranscriptionModel = openAiAudioTranscriptionModel;
    this.openAiAudioSpeechModel = openAiAudioSpeechModel;
  }

  // STT
  public String stt(String filename, byte[] bytes) {
    // 음성 데이터를 ByteArrayResource로 생성
    // Spring AI 1.0.3에서는 파일 이름이 필수로 추가되어야 함
    Resource audioResource = new ByteArrayResource(bytes) {
      @Override
      public String getFilename() {
        return filename;
      }
    };

    // AudioTranscriptionOptions 생성하여 옵션 지정, 힌트 제공
    AudioTranscriptionOptions audioTranscriptionOptions = OpenAiAudioTranscriptionOptions.builder()
      .model("whisper-1")
      .language("ko")
      .build();

    // Prompt 생성
    AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource, audioTranscriptionOptions);

    // 모델 호출 및 응답
    AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(prompt);
    String text = response.getResult().getOutput();

    return text;

  }

  // TTS
  public byte[] tts(String text) {
    // Options
    OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
      .model("gpt-4o-mini-tts")
      .voice(SpeechRequest.Voice.ALLOY)
      .speed(1.0f)
      .responseFormat(SpeechRequest.AudioResponseFormat.MP3)
      .build();

    // Prompt
    SpeechPrompt speechPrompt = new SpeechPrompt(text, speechOptions);

    // 모델 호출 및 응답
    SpeechResponse speechResponse = openAiAudioSpeechModel.call(speechPrompt);
    byte[] bytes = speechResponse.getResult().getOutput();

    return bytes;
  }

  // chat-text
  public Map<String, String> chatText(String question) {
    // LLM 사용
    String textAnswer = chatClient.prompt()
      .system("50자 이내의 한국어로 답변해주세요.")
      .user(question)
      .call()
      .content();

    // TTS 사용
    byte[] audio = tts(textAnswer);

    // base64로 변환
    String base64Audio = Base64.getEncoder().encodeToString(audio);

    // Map에 담기
    Map<String, String> map = new HashMap<>();
    map.put("text", textAnswer);
    map.put("audio", base64Audio);

    return map;
  }
}
