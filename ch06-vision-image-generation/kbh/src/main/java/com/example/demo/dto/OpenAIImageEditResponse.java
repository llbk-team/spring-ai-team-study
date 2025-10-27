package com.example.demo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
// {"data": [{"url": "xxxxx", "b64_json": "xxxxx"}, ... ] }
// 원하는 필드가 아닌 원하지 않는 필드 속성이 와도 필드에 선언하지 않으면 에러남
// 아래 어노테이션을 쓰면 선언하지 않은 나머지는 무시해줌
@JsonIgnoreProperties
public class OpenAIImageEditResponse {
  private List<Image> data;
}
