import request from '@/utils/request'
import type { Course, PageRequest, PageResult } from '@/types'

export const courseApi = {
  // 分页查询课程
  page(params: PageRequest & { teacherId?: number; status?: string; keyword?: string }) {
    return request.get<PageResult<Course>>('/courses/page', { params })
  },

  // 获取所有课程
  list() {
    return request.get<Course[]>('/courses')
  },

  // 获取可加入的课程
  getActiveCourses() {
    return request.get<Course[]>('/courses/active')
  },

  // 获取我的课程
  getMyCourses() {
    return request.get<Course[]>('/courses/my')
  },

  // 获取课程详情
  getById(id: number) {
    return request.get<Course>(`/courses/${id}`)
  },

  // 创建课程
  create(data: Partial<Course>) {
    return request.post<Course>('/courses', data)
  },

  // 更新课程
  update(id: number, data: Partial<Course>) {
    return request.put<Course>(`/courses/${id}`, data)
  },

  // 删除课程
  delete(id: number) {
    return request.delete(`/courses/${id}`)
  },

  // 加入课程
  join(id: number) {
    return request.post(`/courses/${id}/join`)
  },

  // 退出课程
  leave(id: number) {
    return request.post(`/courses/${id}/leave`)
  },

  // 检查是否已加入课程（优化接口）
  checkJoined(id: number) {
    return request.get<boolean>(`/courses/${id}/joined`)
  },

  // 获取课程成员
  getMembers(id: number) {
    return request.get(`/courses/${id}/members`)
  }
}
