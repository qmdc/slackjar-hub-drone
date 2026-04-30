import {io, Socket} from 'socket.io-client';
import {SocketMessageDTO, MessageHandler} from './types';
import {generatePlatformId} from '../utils/browserUniqueId';

/**
 * Socket连接管理类
 * 负责管理WebSocket连接、消息分发和处理器注册
 *
 * @author zhn
 */
class SocketManager {
    // Socket实例
    private socket: Socket | null = null;
    // 消息处理器映射表（bizType -> handler[]）
    private handlers: Map<string, MessageHandler[]> = new Map();
    // 是否已连接
    private connected: boolean = false;
    // Socket服务地址
    private socketUrl: string = '';

    /**
     * 初始化Socket连接
     * @param url Socket服务地址，如：http://127.0.0.1:9092
     * @param userId 用户ID
     */
    init(url: string, userId: string): void {
        if (this.socket) {
            console.warn('[Socket] Socket已初始化，无需重复初始化');
            return;
        }

        // 生成平台ID
        const platformId = generatePlatformId();

        this.socketUrl = url;
        console.log('[Socket] 开始初始化Socket连接:', url, 'userId:', userId, 'platformId:', platformId);

        // 构建带参数的 URL
        const connectUrl = `${url}?userId=${userId}&platformId=${platformId}`;

        this.socket = io(connectUrl, {
            transports: ['websocket'],
            autoConnect: true,
            reconnection: true,
            reconnectionAttempts: 5,
            reconnectionDelay: 1000,
        });

        // 连接成功
        this.socket.on('connect', () => {
            this.connected = true;
            console.log('[Socket] 连接成功，Socket ID:', this.socket?.id);
        });

        // 连接断开
        this.socket.on('disconnect', (reason) => {
            this.connected = false;
            console.warn('[Socket] 连接断开, 原因:', reason);

            // 如果是服务器端主动断开，清理资源
            if (reason === 'io server disconnect') {
                this.socket = null;
                this.handlers.clear();
                console.log('[Socket] 服务器主动断开，已清理资源');
            }
        });

        // 连接错误
        this.socket.on('connect_error', (error) => {
            console.error('[Socket] 连接错误:', error);
        });

        // 监听后端推送的消息
        this.socket.on('backend-message', (data: SocketMessageDTO) => {
            this.handleMessage(data);
        });
    }

    /**
     * 处理接收到的消息
     * @param message Socket消息
     */
    private handleMessage(message: SocketMessageDTO): void {
        console.log('[Socket] 收到消息:', message);

        const {bizType} = message;
        const handlers = this.handlers.get(bizType);

        if (handlers && handlers.length > 0) {
            // 执行该业务类型的所有处理器
            handlers.forEach(handler => {
                try {
                    handler(message);
                } catch (error) {
                    console.error(`[Socket] 处理消息失败 (bizType: ${bizType}):`, error);
                }
            });
        } else {
            console.warn(`[Socket] 未注册 bizType=${bizType} 的处理器`);
        }
    }

    /**
     * 注册消息处理器
     * @param bizType 业务类型
     * @param handler 处理器函数
     */
    registerHandler(bizType: string, handler: MessageHandler): void {
        if (!this.handlers.has(bizType)) {
            this.handlers.set(bizType, []);
        }
        
        // 检查是否已经注册过该 handler，避免重复注册
        const existingHandlers = this.handlers.get(bizType)!;
        if (!existingHandlers.includes(handler)) {
            existingHandlers.push(handler);
            console.log(`[Socket] 注册处理器: ${bizType}`);
        } else {
            console.log(`[Socket] 处理器已存在，跳过重复注册: ${bizType}`);
        }
    }

    /**
     * 断开Socket连接
     */
    disconnect(): void {
        if (this.socket) {
            this.socket.disconnect();
            this.socket = null;
            this.connected = false;
            this.handlers.clear();
            console.log('[Socket] 已断开连接');
        }
    }

    /**
     * 获取连接状态
     */
    isConnected(): boolean {
        return this.connected;
    }

    /**
     * 获取Socket实例
     */
    getSocket(): Socket | null {
        return this.socket;
    }

    /**
     * 发送消息给后端
     * @param message Socket消息
     */
    sendMessageToFrontend(message: SocketMessageDTO): void {
        if (!this.socket || !this.connected) {
            console.warn('[Socket] Socket未连接，无法发送消息');
            return;
        }

        console.log('[Socket] 发送消息给后端:', message);
        this.socket.emit('front-message', message);
    }
}

// 导出单例
export const socketManager = new SocketManager();
