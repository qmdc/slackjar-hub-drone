import {socketManager} from './index';
import {SocketMessageDTO, PushWithBackendEnum} from './index';
import {getNotificationApi} from '../App';
import {useAuthStore} from '../store/authStore';

/**
 * Socket消息处理器注册入口
 * 在应用启动时调用，注册所有业务类型的消息处理器
 *
 * @author zhn
 */
export const initSocketMessageHandlers = (): void => {
    console.log('[Socket Handler] 开始注册消息处理器');

    // 注册成功通知消息处理器
    socketManager.registerHandler(
        PushWithBackendEnum.SUCCESS_STRING_NOTICE,
        handleSuccessNotice
    );

    // 注册失败通知消息处理器
    socketManager.registerHandler(
        PushWithBackendEnum.FAIL_STRING_NOTICE,
        handleFailNotice
    );

    // 注册IP地级市信息处理器
    socketManager.registerHandler(
        PushWithBackendEnum.IP_CITY_INFO,
        handleIpCityInfo
    );

    console.log('[Socket Handler] 消息处理器注册完成');
};

/**
 * 处理成功通知消息
 */
const handleSuccessNotice = (messageDTO: SocketMessageDTO): void => {
    const content = messageDTO.content;
    if (typeof content === 'string') {
        const api = getNotificationApi();
        if (api) {
            api.success({
                title: content,
                // description: content,
                placement: 'bottomRight',
                duration: 3  // 3秒后自动关闭
            });
        }
    }
};

/**
 * 处理失败通知消息
 */
const handleFailNotice = (messageDTO: SocketMessageDTO): void => {
    const content = messageDTO.content;
    if (typeof content === 'string') {
        const api = getNotificationApi();
        if (api) {
            api.error({
                title: content,
                placement: 'bottomRight',
                duration: 5
            });
        }
    }
};

/**
 * 处理IP地级市信息
 */
const handleIpCityInfo = (messageDTO: SocketMessageDTO): void => {
    console.log('[Socket Handler] 处理IP地级市信息:', messageDTO);
    const content = messageDTO.content as { ip: string; city: string };

    if (content && content.city) {
        const {userInfo, setUserInfo} = useAuthStore.getState();
        if (userInfo) {
            setUserInfo({...userInfo, city: content.city});
            console.log('[Socket Handler] 已更新用户城市信息:', content.city);
        }
    }
};
