import request from '@/utils/request'
import type { Paper, PageRequest, PageResult, AutoGeneratePaperRequest } from '@/types'

export const paperApi = {
  // 分页查询试卷
  page(
    params: PageRequest & {
      courseId?: number
      teacherId?: number
      keyword?: string
      type?: string
      status?: string
    }
  ) {
    return request.get<PageResult<Paper>>('/papers/page', { params })
  },

  // 获取所有试卷
  list() {
    return request.get<Paper[]>('/papers')
  },

  // 获取课程的试卷
  getByCourseId(courseId: number) {
    return request.get<Paper[]>(`/papers/course/${courseId}`)
  },

  // 获取试卷详情
  getById(id: number) {
    return request.get<Paper>(`/papers/${id}`)
  },

  // 获取考试用试卷信息（仅教师/管理员）
  getForExam(paperId: number) {
    return request.get<Paper>(`/papers/exam/${paperId}`)
  },

  // 创建试卷
  create(data: Partial<Paper>) {
    return request.post<Paper>('/papers', data)
  },

  // 更新试卷
  update(id: number, data: Partial<Paper>) {
    return request.put<Paper>(`/papers/${id}`, data)
  },

  // 删除试卷
  delete(id: number) {
    return request.delete(`/papers/${id}`)
  },

  // 自动组卷
  autoGenerate(data: AutoGeneratePaperRequest) {
    return request.post<Paper>('/papers/auto-generate', data)
  }
}
