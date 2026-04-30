/**
 * 响应数据结构定义
 */
export interface ResponseData<T = any> {
    code?: number
    message?: string
    data?: T
}

/**
 * 分页结果
 */
export interface PageResult<T> {
    list?: T[]
    total: number
    pageNo?: number
    pageSize: number
    totalPages?: number
}
