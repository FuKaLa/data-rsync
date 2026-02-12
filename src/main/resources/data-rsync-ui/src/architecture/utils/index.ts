// 工具函数库

import { storageConfig } from '@/architecture/config'
import { ERROR_CODE, MESSAGE } from '@/architecture/constants'

// 存储工具类
export class StorageUtils {
  // 设置本地存储
  static set(key: string, value: any, expire?: number) {
    const storageKey = `${storageConfig.prefix}${key}`
    const data = {
      value,
      expire: expire || storageConfig.expire,
      time: Date.now()
    }
    localStorage.setItem(storageKey, JSON.stringify(data))
  }

  // 获取本地存储
  static get(key: string) {
    const storageKey = `${storageConfig.prefix}${key}`
    const item = localStorage.getItem(storageKey)
    if (!item) return null

    try {
      const data = JSON.parse(item)
      if (Date.now() - data.time > data.expire) {
        this.remove(key)
        return null
      }
      return data.value
    } catch (error) {
      this.remove(key)
      return null
    }
  }

  // 移除本地存储
  static remove(key: string) {
    const storageKey = `${storageConfig.prefix}${key}`
    localStorage.removeItem(storageKey)
  }

  // 清空本地存储
  static clear() {
    const keys = Object.keys(localStorage)
    keys.forEach(key => {
      if (key.startsWith(storageConfig.prefix)) {
        localStorage.removeItem(key)
      }
    })
  }

  // 获取存储大小
  static getSize() {
    let size = 0
    const keys = Object.keys(localStorage)
    keys.forEach(key => {
      if (key.startsWith(storageConfig.prefix)) {
        size += localStorage.getItem(key)?.length || 0
      }
    })
    return size
  }
}

// 加密工具类
export class EncryptUtils {
  // 简单的字符串加密
  static encrypt(text: string, key: string = 'data-rsync-secret-key') {
    let result = ''
    for (let i = 0; i < text.length; i++) {
      const charCode = text.charCodeAt(i) ^ key.charCodeAt(i % key.length)
      result += String.fromCharCode(charCode)
    }
    return btoa(result)
  }

  // 简单的字符串解密
  static decrypt(encryptedText: string, key: string = 'data-rsync-secret-key') {
    try {
      const decoded = atob(encryptedText)
      let result = ''
      for (let i = 0; i < decoded.length; i++) {
        const charCode = decoded.charCodeAt(i) ^ key.charCodeAt(i % key.length)
        result += String.fromCharCode(charCode)
      }
      return result
    } catch (error) {
      return encryptedText
    }
  }

  // 生成随机密钥
  static generateKey(length: number = 32) {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+[]{}|;:,.<>?'
    let key = ''
    for (let i = 0; i < length; i++) {
      key += chars.charAt(Math.floor(Math.random() * chars.length))
    }
    return key
  }
}

// 日期工具类
export class DateUtils {
  // 格式化日期
  static format(date: Date | string | number, format: string = 'YYYY-MM-DD HH:mm:ss') {
    const d = new Date(date)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    const seconds = String(d.getSeconds()).padStart(2, '0')

    return format
      .replace('YYYY', String(year))
      .replace('MM', month)
      .replace('DD', day)
      .replace('HH', hours)
      .replace('mm', minutes)
      .replace('ss', seconds)
  }

  // 格式化时间戳
  static formatTimestamp(timestamp: number, format: string = 'YYYY-MM-DD HH:mm:ss') {
    return this.format(new Date(timestamp), format)
  }

  // 计算时间差
  static getTimeDiff(start: Date | string | number, end: Date | string | number) {
    const startDate = new Date(start)
    const endDate = new Date(end)
    const diff = endDate.getTime() - startDate.getTime()

    return {
      milliseconds: diff,
      seconds: Math.floor(diff / 1000),
      minutes: Math.floor(diff / (1000 * 60)),
      hours: Math.floor(diff / (1000 * 60 * 60)),
      days: Math.floor(diff / (1000 * 60 * 60 * 24))
    }
  }

  // 获取相对时间
  static getRelativeTime(date: Date | string | number) {
    const now = new Date()
    const target = new Date(date)
    const diff = now.getTime() - target.getTime()

    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (days > 0) {
      return `${days}天前`
    } else if (hours > 0) {
      return `${hours}小时前`
    } else if (minutes > 0) {
      return `${minutes}分钟前`
    } else if (seconds > 0) {
      return `${seconds}秒前`
    } else {
      return '刚刚'
    }
  }
}

// 验证工具类
export class ValidateUtils {
  // 验证邮箱
  static isValidEmail(email: string) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(email)
  }

  // 验证手机号
  static isValidPhone(phone: string) {
    const phoneRegex = /^1[3-9]\d{9}$/
    return phoneRegex.test(phone)
  }

  // 验证密码强度
  static isValidPassword(password: string) {
    // 至少8位，包含大小写字母、数字和特殊字符
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/
    return passwordRegex.test(password)
  }

  // 验证URL
  static isValidUrl(url: string) {
    const urlRegex = /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$/
    return urlRegex.test(url)
  }

  // 验证IP地址
  static isValidIp(ip: string) {
    const ipRegex = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/
    return ipRegex.test(ip)
  }

  // 验证端口号
  static isValidPort(port: number | string) {
    const portNum = parseInt(String(port))
    return portNum >= 1 && portNum <= 65535
  }

  // 验证数字范围
  static isInRange(value: number, min: number, max: number) {
    return value >= min && value <= max
  }

  // 验证字符串长度
  static isLengthValid(str: string, min: number, max: number) {
    return str.length >= min && str.length <= max
  }
}

// 网络工具类
export class NetworkUtils {
  // 检查网络连接
  static async checkNetwork() {
    try {
      const response = await fetch('https://www.baidu.com', {
        method: 'HEAD',
        mode: 'no-cors'
      })
      return true
    } catch (error) {
      return false
    }
  }

  // 获取IP地址
  static async getIpAddress() {
    try {
      const response = await fetch('https://api.ipify.org?format=json')
      const data = await response.json()
      return data.ip
    } catch (error) {
      return '127.0.0.1'
    }
  }

  // 延迟函数
  static delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms))
  }

  // 重试函数
  static async retry<T>(fn: () => Promise<T>, retries: number = 3, delay: number = 1000): Promise<T> {
    try {
      return await fn()
    } catch (error) {
      if (retries <= 0) {
        throw error
      }
      await this.delay(delay)
      return this.retry(fn, retries - 1, delay * 2)
    }
  }
}

// 字符串工具类
export class StringUtils {
  // 截断字符串
  static truncate(str: string, maxLength: number, suffix: string = '...') {
    if (str.length <= maxLength) return str
    return str.substring(0, maxLength - suffix.length) + suffix
  }

  // 驼峰转下划线
  static camelToSnake(str: string) {
    return str.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`)
  }

  // 下划线转驼峰
  static snakeToCamel(str: string) {
    return str.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase())
  }

  // 首字母大写
  static capitalize(str: string) {
    return str.charAt(0).toUpperCase() + str.slice(1)
  }

  // 生成随机字符串
  static random(length: number = 10) {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    let result = ''
    for (let i = 0; i < length; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length))
    }
    return result
  }

  // 脱敏处理
  static mask(str: string, type: 'phone' | 'email' | 'idcard' | 'bankcard') {
    switch (type) {
      case 'phone':
        return str.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
      case 'email':
        return str.replace(/(\w{2})\w*(\w{2}@[\w.]+)/, '$1****$2')
      case 'idcard':
        return str.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2')
      case 'bankcard':
        return str.replace(/(\d{4})\d{8,12}(\d{4})/, '$1**** **** ****$2')
      default:
        return str
    }
  }

  // 格式化数字
  static formatNumber(num: number, decimals: number = 2) {
    return num.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  }
}

// 数组工具类
export class ArrayUtils {
  // 去重
  static unique<T>(array: T[]): T[] {
    return [...new Set(array)]
  }

  // 按属性去重
  static uniqueBy<T>(array: T[], key: keyof T): T[] {
    const seen = new Set()
    return array.filter(item => {
      const value = item[key]
      if (seen.has(value)) {
        return false
      }
      seen.add(value)
      return true
    })
  }

  // 分组
  static groupBy<T>(array: T[], key: keyof T): Record<string, T[]> {
    return array.reduce((groups, item) => {
      const value = item[key]
      const group = groups[value as any] || []
      group.push(item)
      groups[value as any] = group
      return groups
    }, {} as Record<string, T[]>)
  }

  // 排序
  static sortBy<T>(array: T[], key: keyof T, order: 'asc' | 'desc' = 'asc') {
    return [...array].sort((a, b) => {
      const aValue = a[key]
      const bValue = b[key]
      if (aValue < bValue) return order === 'asc' ? -1 : 1
      if (aValue > bValue) return order === 'asc' ? 1 : -1
      return 0
    })
  }

  // 分页
  static paginate<T>(array: T[], page: number, size: number): T[] {
    const start = (page - 1) * size
    const end = start + size
    return array.slice(start, end)
  }

  // 打乱数组
  static shuffle<T>(array: T[]): T[] {
    const result = [...array]
    for (let i = result.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[result[i], result[j]] = [result[j], result[i]]
    }
    return result
  }
}

// 对象工具类
export class ObjectUtils {
  // 深拷贝
  static deepClone<T>(obj: T): T {
    if (obj === null || typeof obj !== 'object') return obj
    if (obj instanceof Date) return new Date(obj.getTime()) as any
    if (obj instanceof Array) return obj.map(item => this.deepClone(item)) as any
    if (typeof obj === 'object') {
      const clonedObj: any = {}
      for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
          clonedObj[key] = this.deepClone(obj[key])
        }
      }
      return clonedObj
    }
    return obj
  }

  // 合并对象
  static merge<T>(target: T, ...sources: any[]): T {
    if (!sources.length) return target
    const source = sources.shift()

    if (target && source && (typeof target === 'object' && typeof source === 'object')) {
      for (const key in source) {
        if (source.hasOwnProperty(key)) {
          if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {
            if (!target[key]) {
              (target as any)[key] = {}
            }
            this.merge(target[key], source[key])
          } else {
            (target as any)[key] = source[key]
          }
        }
      }
    }

    return this.merge(target, ...sources)
  }

  // 比较对象
  static isEqual(obj1: any, obj2: any): boolean {
    if (obj1 === obj2) return true
    if (obj1 == null || obj2 == null) return false
    if (typeof obj1 !== typeof obj2) return false

    if (typeof obj1 === 'object') {
      const keys1 = Object.keys(obj1)
      const keys2 = Object.keys(obj2)
      if (keys1.length !== keys2.length) return false

      for (const key of keys1) {
        if (!keys2.includes(key)) return false
        if (!this.isEqual(obj1[key], obj2[key])) return false
      }
      return true
    }

    return false
  }

  // 提取对象属性
  static pick<T>(obj: T, keys: Array<keyof T>): Partial<T> {
    const result: any = {}
    keys.forEach(key => {
      if (key in obj) {
        result[key] = obj[key]
      }
    })
    return result
  }

  // 排除对象属性
  static omit<T>(obj: T, keys: Array<keyof T>): Partial<T> {
    const result: any = { ...obj }
    keys.forEach(key => {
      delete result[key]
    })
    return result
  }
}

// 导出默认工具
export default {
  storage: StorageUtils,
  encrypt: EncryptUtils,
  date: DateUtils,
  validate: ValidateUtils,
  network: NetworkUtils,
  string: StringUtils,
  array: ArrayUtils,
  object: ObjectUtils
}
