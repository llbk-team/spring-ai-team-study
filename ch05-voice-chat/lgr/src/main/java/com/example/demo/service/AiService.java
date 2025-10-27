package com.example.demo.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
  private ChatClient chatClient;
  private OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;
  private OpenAiAudioSpeechModel openAiAudioSpeechModel;

  private AiService(ChatClient.Builder chatClientBuilder,
  OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel,
  OpenAiAudioSpeechModel openAiAudioSpeechModel){
    this.chatClient=chatClientBuilder.build();
    this.openAiAudioTranscriptionModel=openAiAudioTranscriptionModel;
    this.openAiAudioSpeechModel=openAiAudioSpeechModel;
  }

  //음성 데이터를 받아서 ByteArrayResource로 생성, 리소스랑 prompt 둘중 선택
  
  public String stt(String fileName,byte[] bytes){
    Resource audioResource = new ByteArrayResource(bytes){
      @Override
      public String getFilename() {
        return fileName;
      }
    };
    
    AudioTranscriptionOptions audioTranscriptionOptions = OpenAiAudioTranscriptionOptions.builder()
      .model("whisper-1")
      .language("ko")
      .build();

    AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource,audioTranscriptionOptions);

    AudioTranscriptionResponse response = openAiAudioTranscriptionModel.call(prompt);
    String text =response.getResult().getOutput();
    return text;
  }

  public byte[] tts(String text){
    OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
    .model("gpt-4o-mini-tts")
    .voice(SpeechRequest.Voice.ALLOY)
    .speed(1.0f)
    .responseFormat(SpeechRequest.AudioResponseFormat.MP3)
    .build();

    SpeechPrompt speechPrompt = new SpeechPrompt(text,speechOptions);
    SpeechResponse speechResponse = openAiAudioSpeechModel.call(speechPrompt);
    byte[] bytes =speechResponse.getResult().getOutput();
    return bytes;
  }

  public Map<String, String> chatText(String question){
    String textAnswer =chatClient.prompt()
      .system("50자 이내로 한국어로 답변해주세요.")
      .user(question)
      .call()
      .content();

      byte[] audio = tts(textAnswer);
      String base64Audio = Base64.getEncoder().encodeToString(audio);

      Map<String, String> map = new HashMap<>();
      map.put("text",textAnswer);
      map.put("audio",base64Audio);

      return map;
  }







}
