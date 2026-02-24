import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/auth'
import type { User, LoginRequest, UserResponse, RegisterRequest } from '@/types'
import { ElMessage } from 'element-plus'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<User | null>(null)

  // 登录
  async function login(loginData: LoginRequest) {
    try {
      const res = await authApi.login(loginData)
      token.value = res.data.token
      // 将 UserResponse 转换为 User 类型
      const userResponse: UserResponse = res.data.user
      user.value = {
        id: userResponse.id,
        username: userResponse.username,
        nickname: userResponse.nickname,
        avatar: userResponse.avatar,
        role: userResponse.role as User['role'],
        status: userResponse.status as User['status'],
        createdAt: userResponse.createdAt
      }
      localStorage.setItem('token', res.data.token)
      ElMessage.success('登录成功')
      return true
    } catch (error) {
      return false
    }
  }

  // 注册
  async function register(data: RegisterRequest) {
    try {
      await authApi.register(data)
      ElMessage.success('注册成功，请登录')
      return true
    } catch (error) {
      return false
    }
  }

  // 获取当前用户信息
  async function getCurrentUser() {
    try {
      const res = await authApi.getCurrentUser()
      // 将响应数据转换为 User 类型
      const userData = res.data
      user.value = {
        id: userData.id,
        username: userData.username,
        nickname: userData.nickname,
        avatar: userData.avatar,
        role: userData.role as User['role'],
        status: userData.status as User['status'],
        createdAt: userData.createdAt
      }
    } catch (error) {
      logout()
    }
  }

  // 登出
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  // 修改密码
  async function changePassword(data: { oldPassword: string; newPassword: string }) {
    try {
      await authApi.changePassword(data)
      ElMessage.success('密码修改成功')
      return true
    } catch (error) {
      return false
    }
  }

  return {
    token,
    user,
    login,
    register,
    getCurrentUser,
    logout,
    changePassword
  }
})
