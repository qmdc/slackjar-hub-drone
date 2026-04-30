package com.slack.slackjarservice.foundation.controller;

import com.slack.slackjarservice.common.base.BaseController;
import com.slack.slackjarservice.common.response.ApiResponse;
import com.slack.slackjarservice.foundation.entity.DetectionHistory;
import com.slack.slackjarservice.foundation.model.dto.DetectionResultDTO;
import com.slack.slackjarservice.foundation.model.request.DetectionRequest;
import com.slack.slackjarservice.foundation.service.YoloDetectionService;
import com.slack.slackjarservice.foundation.socketio.DetectionFrameTracker;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/yolo")
public class YoloController extends BaseController {

    private final YoloDetectionService detectionService;
    private final DetectionFrameTracker frameTracker;

    public YoloController(YoloDetectionService detectionService, DetectionFrameTracker frameTracker) {
        this.detectionService = detectionService;
        this.frameTracker = frameTracker;
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
    public ApiResponse<Map<String, String>> detectVideo(@RequestBody DetectionRequest request) {
        try {
            String userId = String.valueOf(StpUtil.getLoginId());
            String taskId = detectionService.startVideoDetection(request, userId);
            if (taskId == null) {
                return error("模型不存在");
            }
            return success(Map.of("taskId", taskId, "message", "检测已启动"));
        } catch (Exception e) {
            log.error("启动视频检测失败", e);
            return error("启动视频检测失败: " + e.getMessage());
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
    public ApiResponse<List<Map<String, String>>> detectMultiVideo(@RequestBody List<DetectionRequest> requests) {
        try {
            String userId = String.valueOf(StpUtil.getLoginId());
            List<Map<String, String>> results = requests.stream()
                    .map(request -> {
                        String taskId = detectionService.startVideoDetection(request, userId);
                        return Map.of("taskId", taskId != null ? taskId : "", "message", taskId != null ? "检测已启动" : "模型不存在");
                    })
                    .toList();
            return success(results);
        } catch (Exception e) {
            log.error("多路视频检测失败", e);
            return error("多路视频检测失败: " + e.getMessage());
        }
    }

    @GetMapping("/stream/{taskId}")
    public ResponseEntity<StreamingResponseBody> streamDetection(@PathVariable String taskId) {
        DetectionFrameTracker.FrameSession session = frameTracker.getSession(taskId);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        String boundary = "frame_boundary";

        StreamingResponseBody body = outputStream -> {
            int lastIndex = -1;
            try {
                while (true) {
                    List<String> framePaths = session.getFramePaths();
                    for (int i = lastIndex + 1; i < framePaths.size(); i++) {
                        File frameFile = new File(framePaths.get(i));
                        if (frameFile.exists()) {
                            byte[] imageData = Files.readAllBytes(frameFile.toPath());

                            outputStream.write(("--" + boundary + "\r\n").getBytes());
                            outputStream.write("Content-Type: image/jpeg\r\n".getBytes());
                            outputStream.write(("Content-Length: " + imageData.length + "\r\n").getBytes());
                            outputStream.write("\r\n".getBytes());
                            outputStream.write(imageData);
                            outputStream.write("\r\n".getBytes());
                            outputStream.flush();
                        }
                        lastIndex = i;
                    }

                    if (session.isComplete() && lastIndex >= framePaths.size() - 1) {
                        break;
                    }
                    if (session.isError()) {
                        break;
                    }

                    Thread.sleep(30);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.warn("MJPEG流中断: taskId={}, error={}", taskId, e.getMessage());
            } finally {
                try {
                    outputStream.write(("--" + boundary + "--\r\n").getBytes());
                    outputStream.flush();
                } catch (Exception ignored) {
                }
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("multipart/x-mixed-replace; boundary=" + boundary))
                .body(body);
    }
}