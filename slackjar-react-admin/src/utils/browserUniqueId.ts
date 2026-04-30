/**
 * 生成浏览器唯一标识
 * 基于浏览器指纹生成稳定的 platformId
 * 
 * @returns 浏览器唯一标识
 */
export const generatePlatformId = (): string => {
    // 尝试从 localStorage 获取已存在的 platformId
    const existingId = localStorage.getItem('platform_id');
    if (existingId) {
        return existingId;
    }

    // 生成新的 platformId
    // 使用多种浏览器特征组合生成唯一标识
    const fingerprint = [
        navigator.userAgent,
        navigator.language,
        screen.colorDepth,
        screen.width + 'x' + screen.height,
        new Date().getTimezoneOffset(),
        navigator.hardwareConcurrency || 'unknown',
        (navigator as any).deviceMemory || 'unknown'
    ].join('|');

    // 简单哈希算法生成 ID
    let hash = 0;
    for (let i = 0; i < fingerprint.length; i++) {
        const char = fingerprint.charCodeAt(i);
        hash = ((hash << 5) - hash) + char;
        hash = hash & hash; // Convert to 32bit integer
    }

    // 生成最终 ID（包含时间戳确保唯一性）
    const platformId = `platform_${Math.abs(hash).toString(36)}_${Date.now().toString(36)}`;

    // 存储到 localStorage，保证刷新后不变
    localStorage.setItem('platform_id', platformId);

    return platformId;
};
