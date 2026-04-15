import request from '@/utils/request'
import type { Announcement, PageRequest, PageResult } from '@/types'

export const announcementApi = {
  // 分页查询公告
  page(params: PageRequest & { keyword?: string; status?: string; type?: string }) {
    return request.get<PageResult<Announcement>>('/announcements/page', { params })
  },

  // 获取所有公告
  list() {
    return request.get<Announcement[]>('/announcements')
  },

  // 获取公告详情
  getById(id: number) {
    return request.get<Announcement>(`/announcements/${id}`)
  },

  // 创建公告
  create(data: Partial<Announcement>) {
    return request.post<Announcement>('/announcements', data)
  },

  // 更新公告
  update(id: number, data: Partial<Announcement>) {
    return request.put<Announcement>(`/announcements/${id}`, data)
  },

  // 删除公告
  delete(id: number) {
    return request.delete(`/announcements/${id}`)
  }
}
