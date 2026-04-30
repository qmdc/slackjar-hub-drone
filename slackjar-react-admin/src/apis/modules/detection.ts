import request from '../request'
import type {ResponseData} from './types'

export function listModels(): Promise<ResponseData<ModelInfo[]>> {
    return request.get('/yolo/models')
}

export function detectImage(params: DetectionRequest): Promise<ResponseData<DetectionResult>> {
    return request.post('/yolo/detect/image', params)
}

export function detectVideo(params: DetectionRequest): Promise<ResponseData<{ taskId: string; message: string }>> {
    return request.post('/yolo/detect/video', params)
}

export function detectMultiVideo(requests: DetectionRequest[]): Promise<ResponseData<{ taskId: string; message: string }[]>> {
    return request.post('/yolo/detect/multi-video', requests)
}

export function getDetectionHistory(): Promise<ResponseData<DetectionHistory[]>> {
    return request.get('/yolo/history')
}

export function getHistoryById(id: number): Promise<ResponseData<DetectionHistory>> {
    return request.get(`/yolo/history/${id}`)
}

export function deleteHistory(id: number): Promise<ResponseData<void>> {
    return request.delete(`/yolo/history/${id}`)
}

export function getDetectionStats(id: number): Promise<ResponseData<DetectionStats>> {
    return request.get(`/yolo/stats/${id}`)
}

export interface ModelInfo {
    name: string
    path: string
    type: string
}

export interface DetectionRequest {
    filePath: string
    modelName: string
    confThreshold?: number
    iouThreshold?: number
    outputPath?: string
}

export interface DetectionBox {
    x1: number
    y1: number
    x2: number
    y2: number
    confidence: number
    classId: number
    className: string
    frame?: number
}

export interface DetectionResult {
    success: boolean
    message?: string
    detections: DetectionBox[]
    outputPath?: string
    totalFrames?: number
}

export interface DetectionHistory {
    id: number
    taskName: string
    taskType: string
    modelName: string
    modelPath: string
    inputPath: string
    outputPath: string
    status: string
    totalDetections: number
    classDistribution: string
    avgConfidence: number
    minConfidence: number
    maxConfidence: number
    detectionResult: string
    confThreshold: number
    iouThreshold: number
    processTime: number
    errorMessage: string
    createTime: string
    updateTime: string
}

export interface DetectionStats {
    totalDetections: number
    avgConfidence: number
    minConfidence: number
    maxConfidence: number
    classDistribution: string
    classCounts: Record<string, number>
    confidenceList: number[]
}