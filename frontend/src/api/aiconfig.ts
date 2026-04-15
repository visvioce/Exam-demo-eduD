import request from '@/utils/request'
import type { AiConfig, ActiveModelInfo } from '@/types'

// 常用AI平台预设模板（用于快速填充，仅供参考）
// 注意：本系统使用 OpenAI 兼容格式 API，非兼容平台需通过代理服务使用
export const AI_PRESETS = [
  {
    name: '通义千问',
    baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    models: ['qwen-plus', 'qwen-turbo', 'qwen-max', 'qwen-long'],
    compatible: true
  },
  {
    name: 'DeepSeek',
    baseUrl: 'https://api.deepseek.com/v1',
    models: ['deepseek-chat', 'deepseek-coder'],
    compatible: true
  },
  {
    name: '智谱AI',
    baseUrl: 'https://open.bigmodel.cn/api/paas/v4',
    models: ['glm-4', 'glm-4-flash', 'glm-4-plus'],
    compatible: true
  },
  {
    name: 'Moonshot',
    baseUrl: 'https://api.moonshot.cn/v1',
    models: ['moonshot-v1-8k', 'moonshot-v1-32k', 'moonshot-v1-128k'],
    compatible: true
  },
  {
    name: 'NVIDIA NIM',
    baseUrl: 'https://integrate.api.nvidia.com/v1',
    models: ['meta/llama-3.1-405b-instruct', 'meta/llama-3.1-70b-instruct', 'meta/llama-3.1-8b-instruct', 'nvidia/llama-3.1-nemotron-70b-instruct'],
    compatible: true
  },
  {
    name: 'OpenAI',
    baseUrl: 'https://api.openai.com/v1',
    models: ['gpt-4o', 'gpt-4o-mini', 'gpt-4-turbo', 'gpt-3.5-turbo'],
    compatible: true
  },
  {
    name: 'Claude (需代理)',
    baseUrl: '',
    models: ['claude-3-5-sonnet-20241022', 'claude-3-opus-20240229', 'claude-3-haiku-20240307'],
    compatible: false,
    note: 'Claude API 不兼容 OpenAI 格式，需使用兼容代理服务（如 OpenRouter）'
  },
  {
    name: '自定义',
    baseUrl: '',
    models: [],
    compatible: true
  }
]

export const aiConfigApi = {
  // 获取当前用户的配置列表
  getMyConfigs() {
    return request.get<AiConfig[]>('/ai-configs/my')
  },

  // 获取当前用户的激活配置
  getMyActiveConfig() {
    return request.get<AiConfig>('/ai-configs/my/active')
  },

  // 获取当前激活的模型信息
  getActiveModel() {
    return request.get<ActiveModelInfo>('/ai-configs/active-model')
  },

  // 获取配置详情
  getById(id: number) {
    return request.get<AiConfig>(`/ai-configs/${id}`)
  },

  // 创建配置
  create(data: Partial<AiConfig>) {
    return request.post<AiConfig>('/ai-configs', data)
  },

  // 更新配置
  update(id: number, data: Partial<AiConfig>) {
    return request.put<AiConfig>(`/ai-configs/${id}`, data)
  },

  // 删除配置
  delete(id: number) {
    return request.delete(`/ai-configs/${id}`)
  },

  // 添加模型到配置
  addModel(configId: number, model: string) {
    return request.post<AiConfig>(`/ai-configs/${configId}/models`, { model })
  },

  // 从配置中删除模型
  removeModel(configId: number, model: string) {
    return request.delete<AiConfig>(`/ai-configs/${configId}/models/${encodeURIComponent(model)}`)
  },

  // 激活模型（设为当前使用的模型）
  activateModel(configId: number, model: string) {
    // 使用请求体接口，避免URL路径中模型名特殊字符问题
    return request.post<AiConfig>(`/ai-configs/${configId}/activate-model`, { model })
  }
}
