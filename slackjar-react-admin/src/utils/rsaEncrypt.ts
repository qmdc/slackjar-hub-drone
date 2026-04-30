// @ts-ignore
import JSEncrypt from 'jsencrypt'

// 公钥（从后端获取或配置）
const PUBLIC_KEY = `-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxgRM3bntNcqXaK1OdHEQ
J4qz5urXBFKK5OMqUOPFqPggOcDImaUxaSX+3kz7MJKJtKV+0IJJCn23PvJwW25x
xFBD/YK2UQGhEYGEJsffExu3p13zRtsn/rM7yeloZZoJB7owQtXA3/pYllyFckEy
grLzYgQH4Ghwt7Lv5X9JruJ4ZBFuYWhuNUve+AChFfOHWQgkVIt/OeBSeVAObizm
C1gsxW47H/8SizaFfwt6mbkPwF+4xOaSElu2nHxdfLdjcejsrk12eSflPyfYsDGo
IP17iipmtYPAnUYxdbr2IMQlws6oWm7xu7ZEKO1j0Rek46RmiR4D2csA9IR645Lp
eQIDAQAB
-----END PUBLIC KEY-----`

/**
 * 使用 RSA 公钥加密数据
 * @param data - 要加密的明文数据
 * @returns 加密后的 Base64 字符串
 */
export function encryptByRSA(data: string): string {
    const encrypt = new JSEncrypt()
    encrypt.setPublicKey(PUBLIC_KEY)
    const encrypted = encrypt.encrypt(data)
    
    if (!encrypted) {
        throw new Error('RSA 加密失败')
    }
    
    return encrypted
}

/**
 * 加密密码
 * @param password - 明文密码
 * @returns 加密后的密码
 */
export function encryptPassword(password: string): string {
    return encryptByRSA(password)
}
