package com.slack.slackjarservice.foundation.model.request;

import lombok.Data;

@Data
public class DetectionRequest {

    private String filePath;

    private String modelName;

    private Double confThreshold = 0.25;

    private Double iouThreshold = 0.45;

    private String outputPath;
}