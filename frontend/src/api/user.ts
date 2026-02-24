import request from '@/utils/request'
import type { User, PageRequest, PageResult } from '@/types'

export const userApi = {
  // 分页查询用户
  page(params: PageRequest & { role?: string; status?: string; keyword?: string }) {
    return request.get<PageResult<User>>('/users/page', { params })
  },

  // 获取所有用户
  list() {
    return request.get<User[]>('/users')
  },

  // 获取用户详情
  getById(id: number) {
    return request.get<User>(`/users/${id}`)
  },

  // 创建用户
  create(data: Partial<User> & { password?: string }) {
    return request.post<User>('/users', data)
  },

  // 更新用户
  update(id: number, data: Partial<User>) {
    return request.put<User>(`/users/${id}`, data)
  },

  // 删除用户
  delete(id: number) {
    return request.delete(`/users/${id}`)
  },

  // 批量删除用户
  deleteBatch(ids: number[]) {
    return request.delete('/users/batch', { data: ids })
  }
}
