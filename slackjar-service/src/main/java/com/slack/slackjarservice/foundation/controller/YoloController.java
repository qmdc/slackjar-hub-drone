package com.slack.slackjarservice.foundation.controller;

import com.slack.slackjarservice.common.base.BaseController;
import com.slack.slackjarservice.common.response.ApiResponse;
import com.slack.slackjarservice.foundation.entity.DetectionHistory;
import com.slack.slackjarservice.foundation.model.dto.DetectionResultDTO;
import com.slack.slackjarservice.foundation.model.request.DetectionRequest;
import com.slack.slackjarservice.foundation.service.YoloDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/yolo")
public class YoloController extends BaseController {

    private final YoloDetectionService detectionService;

    public YoloController(YoloDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @GetMapping("/models")
    public ApiResponse<List<Map<String, Object>>> listModels() {
        try {
            List<Map<String, Object>> models = detectionService.listModels();
            return success(models);
        } catch (Exception e) {
            log.error("获取模型列表失败", e);
            return error("获取模型列表失败");
        }
    }

    @PostMapping("/detect/image")
    public ApiResponse<DetectionResultDTO> detectImage(@RequestBody DetectionRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            DetectionResultDTO result = detectionService.detectImage(request);
            
            long processTime = System.currentTimeMillis() - startTime;
            detectionService.saveDetectionResult("IMAGE", request.getModelName(), 
                    request.getFilePath(), request.getFilePath(), 
                    result.getOutputPath(), result, 
                    request.getConfThreshold(), request.getIouThreshold(), processTime);
            
            return success(result);
        } catch (Exception e) {
            log.error("图片检测失败", e);
            return error("图片检测失败: " + e.getMessage());
        }
    }

    @PostMapping("/detect/video")
    public ApiResponse<DetectionResultDTO> detectVideo(@RequestBody DetectionRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            DetectionResultDTO result = detectionService.detectVideo(request);
            
            long processTime = System.currentTimeMillis() - startTime;
            detectionService.saveDetectionResult("VIDEO", request.getModelName(), 
                    request.getFilePath(), request.getFilePath(), 
                    result.getOutputPath(), result, 
                    request.getConfThreshold(), request.getIouThreshold(), processTime);
            
            return success(result);
        } catch (Exception e) {
            log.error("视频检测失败", e);
            return error("视频检测失败: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ApiResponse<List<DetectionHistory>> getDetectionHistory() {
        try {
            List<DetectionHistory> history = detectionService.getAllHistory();
            return success(history);
        } catch (Exception e) {
            log.error("获取检测历史失败", e);
            return error("获取检测历史失败");
        }
    }

    @GetMapping("/history/{id}")
    public ApiResponse<DetectionHistory> getHistoryById(@PathVariable Long id) {
        try {
            DetectionHistory history = detectionService.getHistoryById(id);
            if (history == null) {
                return error("检测记录不存在");
            }
            return success(history);
        } catch (Exception e) {
            log.error("获取检测记录失败", e);
            return error("获取检测记录失败");
        }
    }

    @DeleteMapping("/history/{id}")
    public ApiResponse<Void> deleteHistory(@PathVariable Long id) {
        try {
            detectionService.deleteDetectionHistory(id);
            return success();
        } catch (Exception e) {
            log.error("删除检测记录失败", e);
            return error("删除检测记录失败");
        }
    }

    @GetMapping("/stats/{id}")
    public ApiResponse<Map<String, Object>> getDetectionStats(@PathVariable Long id) {
        try {
            Map<String, Object> stats = detectionService.getDetectionStats(id);
            return success(stats);
        } catch (Exception e) {
            log.error("获取检测统计失败", e);
            return error("获取检测统计失败");
        }
    }

    @PostMapping("/detect/multi-video")
    public ApiResponse<List<DetectionResultDTO>> detectMultiVideo(@RequestBody List<DetectionRequest> requests) {
        try {
            List<DetectionResultDTO> results = requests.stream()
                    .map(request -> {
                        long startTime = System.currentTimeMillis();
                        DetectionResultDTO result = detectionService.detectVideo(request);
                        long processTime = System.currentTimeMillis() - startTime;
                        detectionService.saveDetectionResult("VIDEO", request.getModelName(),
                                request.getFilePath(), request.getFilePath(),
                                result.getOutputPath(), result,
                                request.getConfThreshold(), request.getIouThreshold(), processTime);
                        return result;
                    })
                    .toList();
            return success(results);
        } catch (Exception e) {
            log.error("多路视频检测失败", e);
            return error("多路视频检测失败: " + e.getMessage());
        }
    }
}