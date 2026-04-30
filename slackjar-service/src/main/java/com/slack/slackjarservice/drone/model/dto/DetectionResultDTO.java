package com.slack.slackjarservice.drone.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class DetectionResultDTO {

    private boolean success;
    private String message;
    private List<DetectionBox> detections;
    private String outputPath;
    private Integer totalFrames;

    @Data
    public static class DetectionBox {
        private Double x1;
        private Double y1;
        private Double x2;
        private Double y2;
        private Double confidence;
        private Integer classId;
        private String className;
        private Integer frame;
    }
}
