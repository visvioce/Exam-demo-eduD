import request from '@/utils/request'
import type { LoginRequest, LoginResponse, RegisterRequest, UserResponse } from '@/types'

export const authApi = {
  // 用户登录
  login(data: LoginRequest) {
    return request.post<LoginResponse>('/auth/login', data)
  },

  // 用户注册
  register(data: RegisterRequest) {
    return request.post<UserResponse>('/auth/register', data)
  },

  // 获取当前用户信息
  getCurrentUser() {
    return request.get<UserResponse>('/auth/me')
  },

  // 修改密码
  changePassword(data: { oldPassword: string; newPassword: string }) {
    return request.post('/auth/change-password', data)
  },

  // 更新个人资料
  updateProfile(data: { nickname: string; avatar?: string }) {
    return request.put<UserResponse>('/auth/profile', data)
  }
}
