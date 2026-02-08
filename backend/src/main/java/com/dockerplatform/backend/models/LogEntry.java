package com.dockerplatform.backend.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEntry {
    @JsonProperty("@timestamp")
    private String timestamp;
    private String level;
    private String message;
    @JsonProperty("logger_name")
    private String logger;
    @JsonProperty("thread_name")
    private String thread;
    @JsonProperty("stack_trace")
    private String stackTrace;
    private Object additionalData;
}
