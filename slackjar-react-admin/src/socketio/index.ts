// SocketIO 统一导出
export {socketManager} from './socketManager';
export {initSocketMessageHandlers} from './handlers';
export type {SocketMessageDTO, MessageHandler} from './types';
export {PushWithBackendEnum, PushWithFrontEnum} from './types';
export {generatePlatformId} from '../utils/browserUniqueId';
