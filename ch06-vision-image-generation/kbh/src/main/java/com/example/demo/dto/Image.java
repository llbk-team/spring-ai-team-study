package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties
// {"url": "xxxxx", "b64_json": "xxxxx"}
public class Image {
  private String url;
  private String b64_json;
}