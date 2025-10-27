package com.example.demo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

// {"data": [{"url": "xxxxx", "b64_json": "xxxxx"}, ... ], "metadata":"yyy"}  갑자기 객체가 추가될 수도??
// 이 필드를 선언하지 않으면 에러남. 앞에것만 받고 나머지는 무시하겠다는게 있음
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 알려지지 않은 properties 무시해
public class OpenAIImageEditResponse {
  private List<Image> data;
}
