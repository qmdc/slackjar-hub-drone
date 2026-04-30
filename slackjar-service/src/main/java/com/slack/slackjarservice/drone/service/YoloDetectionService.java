package com.slack.slackjarservice.drone.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slack.slackjarservice.drone.entity.DetectionHistory;
import com.slack.slackjarservice.drone.model.dto.DetectionResultDTO;
import com.slack.slackjarservice.drone.model.request.DetectionRequest;

import java.util.List;
import java.util.Map;

public interface YoloDetectionService extends IService<DetectionHistory> {

    DetectionResultDTO detectImage(DetectionRequest request);

    DetectionResultDTO detectVideo(DetectionRequest request);

    String startVideoDetection(DetectionRequest request, String userId);

    List<Map<String, Object>> listModels();

    Map<String, Object> getDetectionStats(Long historyId);

    DetectionHistory saveDetectionResult(String taskType, String modelName, String modelPath,
                                         String inputPath, String outputPath, DetectionResultDTO result,
                                         Double confThreshold, Double iouThreshold, long processTime);

    void deleteDetectionHistory(Long id);

    List<DetectionHistory> getAllHistory();

    DetectionHistory getHistoryById(Long id);
}
