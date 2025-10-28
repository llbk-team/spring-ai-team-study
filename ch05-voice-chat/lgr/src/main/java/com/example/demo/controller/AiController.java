package com.example.demo.controller;

import java.io.IOException;
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
import reactor.core.publisher.Flux;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/ai")
public class AiController {
    @Autowired
    private AiService aiService;

    @PostMapping(value = "/stt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String stt(@RequestParam("speech") MultipartFile speech) throws IOException {
        String originalFileName = speech.getOriginalFilename();
        byte[] bytes = speech.getBytes();
        String text = aiService.stt(originalFileName, bytes);
        return text;
    }

    @PostMapping(value = "/tts", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] tts(@RequestParam("text") String text) {
        byte[] bytes = aiService.tts(text);
        return bytes;
    }

    @PostMapping(value = "/chat-text", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> chat(@RequestParam("question") String question) {
        Map<String, String> response = aiService.chatText(question);

        return response;
    }

    @PostMapping(value = "/chat-voice-stt-llm-tts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void chatVoiceSttLlmTts(
        @RequestParam("question") MultipartFile question,
        HttpServletResponse response)throws Exception {
        
            Flux<byte[]> flux = aiService.chatVoiceSttLlmTts(question.getBytes());
            OutputStream os = response.getOutputStream();
            for(byte[] chunk: flux.toIterable()){
                os.write(chunk);
                os.flush();
            }
    }

     @PostMapping(value = "/chat-voice-one-model", 
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE, 
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] chatVoiceOneModel(@RequestParam("question") MultipartFile question)throws Exception {
        byte[] bytes = aiService.chatVoiceOneModel(question.getBytes(), question.getContentType());
        return bytes;

    }
    



}
