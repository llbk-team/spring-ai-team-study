package com.example.demo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
// JSON에 정의되어 있지만, Java 클래스 필드에 없는 것들을 무시
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiImageEditResponse {
  private List<Image> data;
}
