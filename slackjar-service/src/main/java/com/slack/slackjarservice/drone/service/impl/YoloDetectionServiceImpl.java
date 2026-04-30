package com.slack.slackjarservice.drone.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.slackjarservice.common.enumtype.foundation.PushWithBackendEnum;
import com.slack.slackjarservice.drone.dao.DetectionHistoryDao;
import com.slack.slackjarservice.drone.entity.DetectionHistory;
import com.slack.slackjarservice.drone.model.dto.DetectionResultDTO;
import com.slack.slackjarservice.drone.model.request.DetectionRequest;
import com.slack.slackjarservice.drone.service.YoloDetectionService;
import com.slack.slackjarservice.drone.stream.DetectionFrameTracker;
import com.slack.slackjarservice.foundation.model.dto.SocketMessageDTO;
import com.slack.slackjarservice.foundation.socketio.BackendMessagePush;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class YoloDetectionServiceImpl extends ServiceImpl<DetectionHistoryDao, DetectionHistory> implements YoloDetectionService {

    @Value("${system.python.path:python3}")
    private String pythonPath;

    @Value("${system.yolo.script.path:/Users/ppsn/Documents/trae/slack-hub-drone/yolov-model/py-script/yolo_detector.py}")
    private String scriptPath;

    @Value("${system.yolo.model.path:/Users/ppsn/Documents/trae/slack-hub-drone/yolov-model}")
    private String modelPath;

    @Value("${system.local.storage.path:/Users/ppsn/Documents/trae/slack-hub-drone/file}")
    private String storagePath;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BackendMessagePush backendMessagePush;
    private final DetectionFrameTracker frameTracker;
    private final ScheduledExecutorService progressLogger = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "detection-progress-logger");
        t.setDaemon(true);
        return t;
    });

    public YoloDetectionServiceImpl(BackendMessagePush backendMessagePush, DetectionFrameTracker frameTracker) {
        this.backendMessagePush = backendMessagePush;
        this.frameTracker = frameTracker;
    }

    @Override
    public DetectionResultDTO detectImage(DetectionRequest request) {
        String modelFullPath = findModelPath(request.getModelName());
        if (modelFullPath == null) {
            DetectionResultDTO result = new DetectionResultDTO();
            result.setSuccess(false);
            result.setMessage("模型不存在");
            return result;
        }

        String fileKey = extractFileKey(request.getFilePath());
        String inputPath = new File(storagePath, fileKey).getAbsolutePath();
        String outputPath = new File(storagePath, "detection_result_" + System.currentTimeMillis() + ".jpg").getAbsolutePath();

        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add(scriptPath);
        command.add("detect_image");
        command.add(inputPath);
        command.add(modelFullPath);
        command.add(String.valueOf(request.getConfThreshold()));
        command.add(String.valueOf(request.getIouThreshold()));
        command.add(outputPath);

        return executeCommand(command, outputPath);
    }

    @Override
    public DetectionResultDTO detectVideo(DetectionRequest request) {
        String modelFullPath = findModelPath(request.getModelName());
        if (modelFullPath == null) {
            DetectionResultDTO result = new DetectionResultDTO();
            result.setSuccess(false);
            result.setMessage("模型不存在");
            return result;
        }

        String fileKey = extractFileKey(request.getFilePath());
        String inputPath = new File(storagePath, fileKey).getAbsolutePath();
        String outputPath = new File(storagePath, "detection_result_" + System.currentTimeMillis() + ".mp4").getAbsolutePath();

        List<String> command = new ArrayList<>();
        command.add(pythonPath);
        command.add(scriptPath);
        command.add("detect_video");
        command.add(inputPath);
        command.add(outputPath);
        command.add(modelFullPath);
        command.add(String.valueOf(request.getConfThreshold()));
        command.add(String.valueOf(request.getIouThreshold()));

        return executeCommand(command, outputPath);
    }

    @Override
    public String startVideoDetection(DetectionRequest request, String userId) {
        String modelFullPath = findModelPath(request.getModelName());
        if (modelFullPath == null) {
            return null;
        }

        String fileKey = extractFileKey(request.getFilePath());
        String inputPath = new File(storagePath, fileKey).getAbsolutePath();
        String taskId = "video_" + System.currentTimeMillis();
        String outputDir = new File(storagePath, "detection_frames_" + taskId).getAbsolutePath();

        frameTracker.createSession(taskId);

        CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            List<String> command = new ArrayList<>();
            command.add(pythonPath);
            command.add(scriptPath);
            command.add("detect_video_stream");
            command.add(inputPath);
            command.add(outputDir);
            command.add(modelFullPath);
            command.add(String.valueOf(request.getConfThreshold()));
            command.add(String.valueOf(request.getIouThreshold()));

            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                Process process = pb.start();

                frameTracker.getSession(taskId).setProcess(process);

                ScheduledFuture<?> progressLogTask = progressLogger.scheduleAtFixedRate(() -> {
                    DetectionFrameTracker.FrameSession s = frameTracker.getSession(taskId);
                    if (s != null && !s.isDestroyed()) {
                        log.info("[检测进度] taskId={}, 帧={}/{}, 检测目标数={}, 耗时={}ms",
                                taskId, s.getCurrentFrameIndex(), s.getTotalFrames(),
                                s.getTotalDetections(), System.currentTimeMillis() - startTime);
                    }
                }, 5, 5, TimeUnit.SECONDS);

                CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() -> {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                    } catch (Exception e) {
                        sb.append("读取stderr失败: ").append(e.getMessage());
                    }
                    return sb.toString();
                });

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        DetectionFrameTracker.FrameSession currentSession = frameTracker.getSession(taskId);
                        if (currentSession == null || currentSession.isDestroyed()) {
                            log.info("[检测中断] taskId={}, 客户端已断开", taskId);
                            break;
                        }
                        while (currentSession != null && currentSession.isPaused()) {
                            Thread.sleep(200);
                            currentSession = frameTracker.getSession(taskId);
                            if (currentSession == null || currentSession.isDestroyed()) {
                                break;
                            }
                        }
                        if (currentSession == null || currentSession.isDestroyed()) {
                            break;
                        }

                        try {
                            Map<String, Object> frameData = objectMapper.readValue(line, Map.class);
                            String type = (String) frameData.get("type");

                            if ("start".equals(type)) {
                                Object totalFramesObj = frameData.get("totalFrames");
                                if (totalFramesObj instanceof Number) {
                                    frameTracker.getSession(taskId).setTotalFrames(((Number) totalFramesObj).intValue());
                                }
                            } else if ("frame".equals(type)) {
                                String framePath = (String) frameData.get("framePath");
                                if (framePath != null) {
                                    frameTracker.addFrame(taskId, framePath);
                                }
                                Object detectionCountObj = frameData.get("detectionCount");
                                if (detectionCountObj instanceof Number) {
                                    frameTracker.getSession(taskId).addDetections(((Number) detectionCountObj).intValue());
                                }
                            }

                            frameData.put("taskId", taskId);
                            DetectionFrameTracker.FrameSession session = frameTracker.getSession(taskId);
                            if (session != null) {
                                frameData.put("currentFrame", session.getCurrentFrameIndex());
                                frameData.put("totalFrames", session.getTotalFrames());
                                frameData.put("totalDetections", session.getTotalDetections());
                            }
                            SocketMessageDTO message = new SocketMessageDTO(frameData, PushWithBackendEnum.VIDEO_DETECTION_FRAME.getCode());
                            backendMessagePush.pushMessageToUser(userId, message);
                        } catch (Exception e) {
                            log.warn("解析帧数据失败: {}", line, e);
                        }
                    }
                }

                process.waitFor();
                progressLogTask.cancel(false);
                String stderrOutput = stderrFuture.get();
                log.info("流式视频检测完成, taskId: {}, stderr: {}", taskId, stderrOutput);

                long processTime = System.currentTimeMillis() - startTime;
                DetectionFrameTracker.FrameSession session = frameTracker.getSession(taskId);
                int finalCurrentFrame = 0;
                int finalTotalFrames = 0;
                int finalTotalDetections = 0;
                if (session != null) {
                    session.setProcessTime(processTime);
                    finalCurrentFrame = session.getCurrentFrameIndex();
                    finalTotalFrames = session.getTotalFrames();
                    finalTotalDetections = session.getTotalDetections();
                }
                frameTracker.markComplete(taskId);

                log.info("[检测完成] taskId={}, 帧={}/{}, 目标数={}, 耗时={}ms", taskId, finalCurrentFrame, finalTotalFrames, finalTotalDetections, processTime);

                SocketMessageDTO completeMsg = new SocketMessageDTO(
                        Map.of("type", "complete", "taskId", taskId,
                                "currentFrame", finalCurrentFrame,
                                "totalFrames", finalTotalFrames,
                                "totalDetections", finalTotalDetections,
                                "processTime", processTime),
                        PushWithBackendEnum.VIDEO_DETECTION_FRAME.getCode()
                );
                backendMessagePush.pushMessageToUser(userId, completeMsg);

            } catch (Exception e) {
                log.error("流式视频检测失败, taskId: {}", taskId, e);
                frameTracker.markError(taskId);
                SocketMessageDTO errorMsg = new SocketMessageDTO(
                        Map.of("type", "error", "taskId", taskId, "message", "检测失败: " + e.getMessage()),
                        PushWithBackendEnum.VIDEO_DETECTION_FRAME.getCode()
                );
                backendMessagePush.pushMessageToUser(userId, errorMsg);
            }
        });

        return taskId;
    }

    private String extractFileKey(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return filePath;
        }
        String prefix = "/api/sys-file/local/download?filePath=";
        if (filePath.contains(prefix)) {
            return filePath.substring(filePath.indexOf(prefix) + prefix.length());
        }
        if (filePath.contains("filePath=")) {
            return filePath.substring(filePath.indexOf("filePath=") + "filePath=".length());
        }
        return filePath;
    }

    private DetectionResultDTO executeCommand(List<String> command, String outputPath) {
        long startTime = System.currentTimeMillis();
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() -> {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                } catch (Exception e) {
                    sb.append("读取stderr失败: ").append(e.getMessage());
                }
                return sb.toString();
            });

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();
            String errorOutput = stderrFuture.get();
            log.info("Python脚本执行完成, 退出码: {}, stderr: {}, stdout: {}", exitCode, errorOutput, output);

            DetectionResultDTO result = objectMapper.readValue(output.toString(), DetectionResultDTO.class);
            if (result.isSuccess() && outputPath != null) {
                String relativePath = outputPath.substring(new File(storagePath).getAbsolutePath().length() + 1);
                result.setOutputPath(relativePath);
            }
            return result;

        } catch (Exception e) {
            log.error("执行Python脚本失败", e);
            DetectionResultDTO result = new DetectionResultDTO();
            result.setSuccess(false);
            result.setMessage("检测失败: " + e.getMessage());
            return result;
        }
    }

    private String findModelPath(String modelName) {
        File modelDir = new File(modelPath);
        if (!modelDir.exists() || !modelDir.isDirectory()) {
            return null;
        }

        File[] subDirs = modelDir.listFiles(File::isDirectory);
        if (subDirs == null) {
            return null;
        }

        for (File subDir : subDirs) {
            if (subDir.getName().equalsIgnoreCase(modelName)) {
                File weightsDir = new File(subDir, "weights");
                if (weightsDir.exists()) {
                    File[] weights = weightsDir.listFiles((dir, name) -> name.endsWith(".pt"));
                    if (weights != null && weights.length > 0) {
                        return weights[0].getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> listModels() {
        List<Map<String, Object>> models = new ArrayList<>();
        File modelDir = new File(modelPath);

        if (!modelDir.exists() || !modelDir.isDirectory()) {
            return models;
        }

        File[] subDirs = modelDir.listFiles(File::isDirectory);
        if (subDirs == null) {
            return models;
        }

        for (File subDir : subDirs) {
            File weightsDir = new File(subDir, "weights");
            if (weightsDir.exists()) {
                File[] weights = weightsDir.listFiles((dir, name) -> name.endsWith(".pt"));
                if (weights != null && weights.length > 0) {
                    Map<String, Object> model = new HashMap<>();
                    model.put("name", subDir.getName());
                    model.put("path", weights[0].getAbsolutePath());
                    model.put("type", weights[0].getName().replace(".pt", ""));
                    models.add(model);
                }
            }
        }
        return models;
    }

    @Override
    public Map<String, Object> getDetectionStats(Long historyId) {
        DetectionHistory history = getById(historyId);
        if (history == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDetections", history.getTotalDetections());
        stats.put("avgConfidence", history.getAvgConfidence());
        stats.put("minConfidence", history.getMinConfidence());
        stats.put("maxConfidence", history.getMaxConfidence());
        stats.put("classDistribution", history.getClassDistribution());

        if (history.getDetectionResult() != null) {
            try {
                List<DetectionResultDTO.DetectionBox> detections = objectMapper.readValue(
                        history.getDetectionResult(),
                        new TypeReference<List<DetectionResultDTO.DetectionBox>>() {}
                );

                Map<String, Integer> classCounts = new HashMap<>();
                List<Double> confidences = new ArrayList<>();

                for (DetectionResultDTO.DetectionBox box : detections) {
                    classCounts.merge(box.getClassName(), 1, Integer::sum);
                    confidences.add(box.getConfidence());
                }

                stats.put("classCounts", classCounts);
                stats.put("confidenceList", confidences);

            } catch (Exception e) {
                log.error("解析检测结果失败", e);
            }
        }

        return stats;
    }

    @Override
    public DetectionHistory saveDetectionResult(String taskType, String modelName, String modelPath,
                                                String inputPath, String outputPath,
                                                DetectionResultDTO result, Double confThreshold,
                                                Double iouThreshold, long processTime) {
        DetectionHistory history = new DetectionHistory();
        history.setTaskName("检测任务_" + System.currentTimeMillis());
        history.setTaskType(taskType);
        history.setModelName(modelName);
        history.setModelPath(modelPath);
        history.setInputPath(inputPath);
        history.setOutputPath(outputPath);
        history.setStatus(result.isSuccess() ? "SUCCESS" : "FAILED");
        history.setConfThreshold(confThreshold);
        history.setIouThreshold(iouThreshold);
        history.setProcessTime(processTime);

        if (result.isSuccess()) {
            history.setTotalDetections(result.getDetections() != null ? result.getDetections().size() : 0);

            try {
                history.setDetectionResult(objectMapper.writeValueAsString(result.getDetections()));

                if (result.getDetections() != null && !result.getDetections().isEmpty()) {
                    Map<String, Integer> classCounts = new HashMap<>();
                    double totalConfidence = 0;
                    double minConfidence = Double.MAX_VALUE;
                    double maxConfidence = Double.MIN_VALUE;

                    for (DetectionResultDTO.DetectionBox box : result.getDetections()) {
                        classCounts.merge(box.getClassName(), 1, Integer::sum);
                        totalConfidence += box.getConfidence();
                        minConfidence = Math.min(minConfidence, box.getConfidence());
                        maxConfidence = Math.max(maxConfidence, box.getConfidence());
                    }

                    history.setClassDistribution(objectMapper.writeValueAsString(classCounts));
                    history.setAvgConfidence(totalConfidence / result.getDetections().size());
                    history.setMinConfidence(minConfidence);
                    history.setMaxConfidence(maxConfidence);
                }
            } catch (Exception e) {
                log.error("序列化检测结果失败", e);
            }
        } else {
            history.setErrorMessage(result.getMessage());
        }

        save(history);
        return history;
    }

    @Override
    public void deleteDetectionHistory(Long id) {
        removeById(id);
    }

    @Override
    public List<DetectionHistory> getAllHistory() {
        return list(new LambdaQueryWrapper<DetectionHistory>().orderByDesc(DetectionHistory::getCreateTime));
    }

    @Override
    public DetectionHistory getHistoryById(Long id) {
        return getById(id);
    }
}
