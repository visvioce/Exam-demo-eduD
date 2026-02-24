import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

// 请求拦截器
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

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    // 如果是文件下载，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    // 业务状态码判断
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // 401: Token 过期或未登录
      if (res.code === 401) {
        const authStore = useAuthStore()
        authStore.logout()
        window.location.href = '/login'
      }

      return Promise.reject(new Error(res.message || 'Error'))
    }

    return res
  },
  (error) => {
    let message = '请求失败'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录'
          const authStore = useAuthStore()
          authStore.logout()
          window.location.href = '/login'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '资源不存在'
          break
        case 500:
          message = '服务器错误'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时'
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 封装请求方法
export const request = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config)
  },

  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },

  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },

  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config)
  }
}

export default service
