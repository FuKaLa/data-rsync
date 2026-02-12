const TOKEN_KEY = 'data-rsync-token'
const XSRF_TOKEN_KEY = 'data-rsync-xsrf-token'

/**
 * 获取token
 */
export const getToken = (): string | null => {
  return sessionStorage.getItem(TOKEN_KEY)
}

/**
 * 设置token
 */
export const setToken = (token: string): void => {
  sessionStorage.setItem(TOKEN_KEY, token)
}

/**
 * 删除token
 */
export const removeToken = (): void => {
  sessionStorage.removeItem(TOKEN_KEY)
}

/**
 * 检查是否有token
 */
export const hasToken = (): boolean => {
  return getToken() !== null
}

/**
 * 获取XSRF token
 */
export const getXsrfToken = (): string | null => {
  return sessionStorage.getItem(XSRF_TOKEN_KEY)
}

/**
 * 设置XSRF token
 */
export const setXsrfToken = (token: string): void => {
  sessionStorage.setItem(XSRF_TOKEN_KEY, token)
}

/**
 * 清除所有存储的信息
 */
export const clearAll = (): void => {
  sessionStorage.clear()
}
