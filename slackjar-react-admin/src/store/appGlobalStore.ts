import {create} from 'zustand'
import {persist} from 'zustand/middleware'
import {Locale} from "antd/es/locale"
import zhCN from "antd/locale/zh_CN"
import enUS from "antd/locale/en_US"

export interface AppGlobalState {
    locale: Locale
    language: 'zh' | 'en'
    setLocale: (locale: Locale, language?: 'zh' | 'en') => void
}

export const useAppGlobalStore = create<AppGlobalState>()(
    persist((set) => ({
            locale: zhCN,
            language: 'zh',
            setLocale: (locale: Locale, language?: 'zh' | 'en') => {
                set({locale, language: language || (locale === enUS ? 'en' : 'zh')})
            }
        }),
        {
            name: 'app-global-storage',  // localStorage 中的键名
            partialize: (state) => ({
                language: state.language,
                locale: state.locale
            })
        }
    )
)
