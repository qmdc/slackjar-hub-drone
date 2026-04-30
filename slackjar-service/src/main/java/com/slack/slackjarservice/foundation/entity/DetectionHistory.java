package com.slack.slackjarservice.foundation.entity;

import com.slack.slackjarservice.common.base.BaseModel;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("detection_history")
public class DetectionHistory extends BaseModel {

    private String taskName;

    private String taskType;

    private String modelName;

    private String modelPath;

    private String inputPath;

    private String outputPath;

    private String status;

    private Integer totalDetections;

    private String classDistribution;

    private Double avgConfidence;

    private Double minConfidence;

    private Double maxConfidence;

    private String detectionResult;

    private Double confThreshold;

    private Double iouThreshold;

    private Long processTime;

    private String errorMessage;
}