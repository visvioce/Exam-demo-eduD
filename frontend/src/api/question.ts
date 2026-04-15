import request from '@/utils/request'
import type { Question, PageRequest, PageResult } from '@/types'

export const questionApi = {
  // 分页查询题目
  page(params: PageRequest & { teacherId?: number; type?: string; subject?: string; difficulty?: string }) {
    return request.get<PageResult<Question>>('/questions/page', { params })
  },

  // 获取所有题目
  list() {
    return request.get<Question[]>('/questions')
  },

  // 获取题目详情
  getById(id: number) {
    return request.get<Question>(`/questions/${id}`)
  },

  // 获取教师的题目
  getByTeacherId(teacherId: number) {
    return request.get<Question[]>(`/questions/teacher/${teacherId}`)
  },

  // 按类型获取题目
  getByType(type: string) {
    return request.get<Question[]>(`/questions/type/${type}`)
  },

  // 创建题目
  create(data: Partial<Question>) {
    return request.post<Question>('/questions', data)
  },

  // 更新题目
  update(id: number, data: Partial<Question>) {
    return request.put<Question>(`/questions/${id}`, data)
  },

  // 删除题目
  delete(id: number) {
    return request.delete(`/questions/${id}`)
  },

  // 获取所有学科列表
  getSubjects() {
    return request.get<string[]>('/questions/subjects')
  }
}
