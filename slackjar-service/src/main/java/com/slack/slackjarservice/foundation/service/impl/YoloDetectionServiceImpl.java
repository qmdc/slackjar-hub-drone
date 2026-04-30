package com.slack.slackjarservice.foundation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.slackjarservice.foundation.dao.DetectionHistoryDao;
import com.slack.slackjarservice.foundation.entity.DetectionHistory;
import com.slack.slackjarservice.foundation.model.dto.DetectionResultDTO;
import com.slack.slackjarservice.foundation.model.request.DetectionRequest;
import com.slack.slackjarservice.foundation.service.YoloDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

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

    @Override
    public DetectionResultDTO detectImage(DetectionRequest request) {
        String modelFullPath = findModelPath(request.getModelName());
        if (modelFullPath == null) {
            DetectionResultDTO result = new DetectionResultDTO();
            result.setSuccess(false);
            result.setMessage("模型不存在");
            return result;
        }

        String inputPath = storagePath + "/" + request.getFilePath();
        String outputPath = storagePath + "/detection_result_" + System.currentTimeMillis() + ".jpg";

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

        String inputPath = storagePath + "/" + request.getFilePath();
        String outputPath = storagePath + "/detection_result_" + System.currentTimeMillis() + ".mp4";

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

    private DetectionResultDTO executeCommand(List<String> command, String outputPath) {
        long startTime = System.currentTimeMillis();
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();
            log.info("Python脚本执行完成, 退出码: {}, 输出: {}", exitCode, output);

            DetectionResultDTO result = objectMapper.readValue(output.toString(), DetectionResultDTO.class);
            if (result.isSuccess() && outputPath != null) {
                result.setOutputPath(outputPath.substring(storagePath.length() + 1));
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