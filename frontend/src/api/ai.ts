import request from '@/utils/request'
import { useAuthStore } from '@/stores/auth'
import type { AiConfig } from '@/types'

export interface GenerateQuestionRequest {
  subject: string
  type: string
  difficulty: string
  count: number
  requirements?: string
}

export interface GeneratedQuestion {
  content: string
  type: string
  options?: { id: string; text: string }[]
  correctAnswer: string | string[]
  explanation?: string
  score?: number
  difficulty?: string
  scoringCriteria?: ScoringCriterion[]
}

export interface ScoringCriterion {
  point: string
  score: number
}

export interface GeneratedQuestionResponse {
  questions: GeneratedQuestion[]
}

export const aiApi = {
  // 获取当前用户的AI配置列表
  getConfigs() {
    return request.get<AiConfig[]>('/ai-configs/my')
  },

  // 获取当前用户的激活配置
  getActiveConfig() {
    return request.get<AiConfig>('/ai-configs/my/active')
  },

  // AI生成题目（设置60秒超时，与后端AI调用超时一致）
  generateQuestions(data: GenerateQuestionRequest) {
    return request.post<GeneratedQuestionResponse>('/ai/generate-questions', data, {
      timeout: 60000  // 60秒超时
    })
  },

  /**
   * 流式生成题目（SSE）
   * @param data 生成请求参数
   * @param callbacks 回调函数
   * @param timeout 超时时间（毫秒），默认5分钟
   * @returns 返回一个关闭连接的函数
   */
  generateQuestionsStream(
    data: GenerateQuestionRequest,
    callbacks: {
      onStart?: () => void
      onChunk?: (content: string) => void
      onComplete?: (response: GeneratedQuestionResponse) => void
      onError?: (error: string) => void
    },
    timeout: number = 5 * 60 * 1000  // 默认5分钟超时
  ): () => void {
    const authStore = useAuthStore()
    const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
    
    // 使用fetch发送POST请求，接收SSE流
    const controller = new AbortController()
    let timeoutId: ReturnType<typeof setTimeout> | null = null
    let isCompleted = false

    // 设置超时
    const clearTimeout_ = () => {
      if (timeoutId) {
        clearTimeout(timeoutId)
        timeoutId = null
      }
    }

    timeoutId = setTimeout(() => {
      if (!isCompleted) {
        clearTimeout_()
        controller.abort()
        callbacks.onError?.('请求超时，请稍后重试')
      }
    }, timeout)
    
    fetch(`${baseUrl}/ai/generate-questions-stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authStore.token}`
      },
      body: JSON.stringify(data),
      signal: controller.signal
    }).then(async response => {
      if (!response.ok) {
        clearTimeout_()
        const errorData = await response.json().catch(() => ({ message: '请求失败' }))
        callbacks.onError?.(errorData.message || '请求失败')
        return
      }

      const reader = response.body?.getReader()
      if (!reader) {
        clearTimeout_()
        callbacks.onError?.('无法读取响应流')
        return
      }

      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          // 流结束时，处理 buffer 中剩余的数据
          if (buffer.trim()) {
            processSSEEvent(buffer, callbacks)
          }
          break
        }

        const chunk = decoder.decode(value, { stream: true })
        buffer += chunk
        
        // 按双换行符分割事件
        const events = buffer.split('\n\n')
        buffer = events.pop() || ''

        for (const event of events) {
          processSSEEvent(event, callbacks)
        }
      }
      clearTimeout_()
      isCompleted = true
    }).catch(error => {
      clearTimeout_()
      if (error.name !== 'AbortError') {
        callbacks.onError?.(error.message || '连接失败')
      }
    })

    // 返回关闭连接的函数
    return () => {
      clearTimeout_()
      controller.abort()
    }
  }
}

/**
 * 处理单个SSE事件
 */
function processSSEEvent(
  eventStr: string,
  callbacks: {
    onStart?: () => void
    onChunk?: (content: string) => void
    onComplete?: (response: GeneratedQuestionResponse) => void
    onError?: (error: string) => void
  }
) {
  if (!eventStr.trim()) return
  
  const lines = eventStr.split('\n')
  let eventType = ''
  let eventData = ''
  
  for (const line of lines) {
    if (line.startsWith('event:')) {
      eventType = line.substring(6).trim()
    } else if (line.startsWith('data:')) {
      eventData = line.substring(5).trim()
    }
  }
  
  // 只处理完整的事件（包含event和data两个字段）
  if (eventType && eventData) {
    handleSSEEvent(eventType, eventData, callbacks)
  }
}

/**
 * 处理SSE事件
 */
function handleSSEEvent(
  eventType: string,
  dataStr: string,
  callbacks: {
    onStart?: () => void
    onChunk?: (content: string) => void
    onComplete?: (response: GeneratedQuestionResponse) => void
    onError?: (error: string) => void
  }
) {
  try {
    const data = JSON.parse(dataStr)
    
    switch (eventType) {
      case 'start':
        callbacks.onStart?.()
        break
      case 'chunk':
        if (data.content) {
          callbacks.onChunk?.(data.content)
        }
        break
      case 'complete':
        callbacks.onComplete?.(data as GeneratedQuestionResponse)
        break
      case 'error':
        callbacks.onError?.(data.message || '生成失败')
        break
    }
  } catch (e) {
    // 忽略JSON解析错误 - 这通常是因为数据流被分割，完整数据会在后续chunk中到达
    // 只在error事件类型时才报告错误
    if (eventType === 'error') {
      callbacks.onError?.('生成失败')
    }
  }
}
