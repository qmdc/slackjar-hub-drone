import i18n from 'i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import {initReactI18next} from 'react-i18next';
import {resources} from "./resources";


i18n
    .use(LanguageDetector)        // 自动检测浏览器语言
    .use(initReactI18next)        // 集成到 React
    .init({
        fallbackLng: 'zh',        // 降级语言：中文
        lng: 'zh',                // 默认语言：中文
        debug: true,              // 开发模式调试
        resources: resources,     // 翻译资源
        interpolation: {
            escapeValue: false    // React 已处理 XSS，无需转义
        }
    }).then(r => {
});
