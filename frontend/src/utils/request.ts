/**
 * HTTP 请求工具模块
 *
 * @module utils/request
 * @description 基于 Axios 封装的 HTTP 请求工具，提供统一的请求拦截、响应处理和错误处理
 *
 * 功能特性：
 * - 自动添加 JWT Token 到请求头
 * - 统一的响应数据格式处理
 * - 401 未授权自动跳转登录页
 * - 统一的错误提示
 * - 支持文件下载
 * - 请求超时处理（默认 15 秒）
 */
import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import type { ApiResponse } from '@/types'

/**
 * Axios 实例配置
 * baseURL: 从环境变量读取，默认为 '/api'
 * timeout: 请求超时时间 15 秒
 */
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

type ApiEnvelope<T> = ApiResponse<T>

/**
 * 防止重复登出跳转
 */
let isLoggingOut = false

/**
 * 请求拦截器
 * 功能：自动为每个请求添加 JWT Token 到 Authorization 请求头
 */
service.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers['Authorization'] = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 功能：
 * 1. 处理文件下载请求（直接返回原始响应）
 * 2. 统一处理业务错误码（code !== 200）
 * 3. 401 未授权时自动登出并跳转登录页
 * 4. 统一错误提示
 */
service.interceptors.response.use(
  (response) => {
    const typedResponse = response as AxiosResponse<ApiEnvelope<unknown> | Blob>
    const res = typedResponse.data

    // 如果是文件下载，直接返回原始响应
    if (typedResponse.config.responseType === 'blob') {
      return typedResponse as never
    }

    // 业务状态码判断（后端统一返回格式：{ code, message, data }）
    if ((res as ApiEnvelope<unknown>).code !== 200) {
      ElMessage.error((res as ApiEnvelope<unknown>).message || '请求失败')

      // 401: Token 过期或未登录，清除登录状态并跳转登录页
      if ((res as ApiEnvelope<unknown>).code === 401) {
        if (isLoggingOut) return Promise.reject(new Error('重复登出'))
        isLoggingOut = true
        const authStore = useAuthStore()
        authStore.logout()
        window.location.href = '/login'
      }

      return Promise.reject(new Error((res as ApiEnvelope<unknown>).message || 'Error'))
    }

    return res as never
  },
  (error) => {
    let message = '请求失败'
    if (error.response) {
      const status = error.response.status
      const responseData = error.response.data
      switch (status) {
        case 401:
          message = '未授权，请重新登录'
          if (isLoggingOut) return Promise.reject(error)
          isLoggingOut = true
          const authStore = useAuthStore()
          authStore.logout()
          window.location.href = '/login'
          break
        case 403:
          message = responseData?.message || '拒绝访问，权限不足'
          break
        case 404:
          message = responseData?.message || '请求的资源不存在'
          break
        case 500:
          message = responseData?.message || '服务器内部错误'
          break
        default:
          message = responseData?.message
            || responseData?.error
            || `请求失败 (${status})`
      }
    } else if (typeof error.message === 'string' && error.message.includes('timeout')) {
      message = '请求超时，请稍后重试'
    } else if (typeof error.message === 'string' && error.message.includes('Network')) {
      message = '网络连接失败，请检查网络'
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 封装请求方法
export const request = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiEnvelope<T>> {
    return service.get(url, config) as Promise<ApiEnvelope<T>>
  },

  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiEnvelope<T>> {
    return service.post(url, data, config) as Promise<ApiEnvelope<T>>
  },

  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiEnvelope<T>> {
    return service.put(url, data, config) as Promise<ApiEnvelope<T>>
  },

  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiEnvelope<T>> {
    return service.delete(url, config) as Promise<ApiEnvelope<T>>
  }
}

export default service
