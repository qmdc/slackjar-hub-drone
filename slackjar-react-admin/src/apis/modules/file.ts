import request from '../request'
import type {ResponseData} from './types'

/**
 * 上传文件
 */
export function uploadFile(file: File, bizType: string, expired?: number): Promise<ResponseData<FileUploadResponse>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('bizType', bizType)
    if (expired !== undefined) {
        formData.append('expired', expired.toString())
    }
    return request.post('/sys-file/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

/**
 * 下载文件
 */
export function downloadFile(filePath: string): Promise<ResponseData<Blob>> {
    return request.get('/sys-file/download', {
        params: {filePath},
        responseType: 'blob'
    })
}

/**
 * 批量删除文件
 */
export function batchDeleteFiles(filePaths: string[]): Promise<ResponseData<BatchDeleteResponse>> {
    return request.post('/sys-file/batch-delete', {filePaths})
}

// ============================================
// 类型定义
// ============================================

/**
 * 文件上传响应
 */
export interface FileUploadResponse {
    fileId: number
    fileName: string
    fileUrl: string
    fileSize: number
    fileType: string
    thumbnailUrl?: string
    expired: number
}

/**
 * 批量删除文件响应
 */
export interface BatchDeleteResponse {
    successFiles: string[]
    failedFiles: string[]
    successCount: number
    failedCount: number
    totalCount: number
}
