# 前端技术详解

## 一、技术栈概览

### 核心框架与依赖

前端基于 **Vue 3.5** 构建，采用 Composition API 和 TypeScript 开发。

| 依赖 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.25 | 渐进式JavaScript框架 |
| TypeScript | 5.9.3 | 类型安全的JavaScript超集 |
| Vite | 7.3.1 | 下一代前端构建工具（极快的开发体验） |
| Element Plus | 2.13.2 | Vue 3 UI组件库 |
| Pinia | 3.0.4 | Vue 3 官方状态管理库 |
| Vue Router | 4.6.4 | Vue.js 官方路由 |
| Axios | 1.13.5 | HTTP 客户端 |
| ECharts | 6.0.0 | 数据可视化图表库 |
| Sass | 1.97.3 | CSS 预处理器 |

### package.json 核心配置

```json
{
  "dependencies": {
    "vue": "^3.5.25",
    "vue-router": "^4.6.4",
    "pinia": "^3.0.4",
    "element-plus": "^2.13.2",
    "axios": "^1.13.5",
    "echarts": "^6.0.0"
  },
  "devDependencies": {
    "typescript": "^5.9.3",
    "vite": "^7.3.1",
    "sass": "^1.97.3",
    "@vitejs/plugin-vue": "^5.2.1"
  }
}
```

---

## 二、项目结构

```
frontend/src/
├── main.ts                 # 入口文件
├── App.vue                 # 根组件
│
├── views/                  # 页面组件 (21个)
│   ├── aiconfig/
│   │   └── AiConfigList.vue        # AI配置管理
│   ├── announcement/
│   │   └── AnnouncementList.vue    # 公告列表
│   ├── auth/
│   │   ├── Login.vue               # 登录页
│   │   └── Register.vue            # 注册页
│   ├── carousel/
│   │   └── CarouselList.vue        # 轮播图管理
│   ├── course/
│   │   ├── CourseList.vue          # 课程列表
│   │   └── CourseDetail.vue        # 课程详情
│   ├── dashboard/
│   │   ├── Dashboard.vue           # 仪表盘主页
│   │   ├── StudentDashboard.vue    # 学生仪表盘
│   │   └── TeacherDashboard.vue    # 教师仪表盘
│   ├── exam/
│   │   ├── ExamList.vue            # 考试列表
│   │   ├── ExamDetail.vue          # 考试详情
│   │   ├── ExamTake.vue            # 答题页面
│   │   ├── ExamResults.vue         # 考试结果
│   │   └── ExamReview.vue          # 考试回顾
│   ├── paper/
│   │   ├── PaperList.vue           # 试卷列表
│   │   └── components/
│   │       ├── AutoPaperForm.vue   # 自动组卷表单
│   │       └── ManualPaperForm.vue # 手动组卷表单
│   ├── question/
│   │   └── QuestionList.vue        # 题目列表
│   └── user/
│       ├── UserList.vue            # 用户列表
│       └── Profile.vue             # 个人中心
│
├── components/             # 公共组件 (4个)
│   ├── ActionButtons.vue           # 操作按钮组
│   ├── DeleteActionButton.vue      # 删除按钮
│   ├── IconActionButton.vue        # 图标按钮
│   └── Layout/
│       └── MainLayout.vue          # 主布局组件
│
├── api/                    # API接口 (10个)
│   ├── auth.ts             # 认证接口
│   ├── user.ts             # 用户接口
│   ├── course.ts           # 课程接口
│   ├── question.ts         # 题目接口
│   ├── paper.ts            # 试卷接口
│   ├── exam.ts             # 考试接口
│   ├── announcement.ts     # 公告接口
│   ├── aiconfig.ts         # AI配置接口
│   ├── ai.ts               # AI出题接口
│   └── carousel.ts         # 轮播图接口
│
├── stores/                 # 状态管理 (1个)
│   └── auth.ts             # 认证状态
│
├── router/                 # 路由配置
│   └── index.ts
│
├── types/                  # TypeScript类型定义
│   └── index.ts
│
├── utils/                  # 工具函数
│   ├── request.ts          # Axios封装
│   ├── error.ts            # 错误处理
│   ├── format.ts           # 格式化工具
│   └── sanitize.ts         # 数据清理
│
├── composables/            # 组合式函数
│   └── usePagedList.ts     # 分页列表Hook
│
└── styles/                 # 样式文件
    ├── index.scss          # 样式入口
    ├── design-tokens.scss  # 设计Token
    ├── global-styles.scss  # 全局样式
    └── views/              # 页面样式
        ├── auth-form-shared.scss
        ├── base-detail.scss
        └── base-list.scss
```

---

## 三、路由配置

### 3.1 路由表

**文件位置**: `frontend/src/router/index.ts`

```typescript
const routes: RouteRecordRaw[] = [
  // 公开页面（无需登录）
  { path: '/login', component: () => import('@/views/auth/Login.vue') },
  { path: '/register', component: () => import('@/views/auth/Register.vue') },
  
  // 答题页面（全屏，学生权限）
  { 
    path: '/exam/:id/take', 
    component: () => import('@/views/exam/ExamTake.vue'),
    meta: { requiresAuth: true, roles: ['STUDENT'] }
  },
  
  // 主布局内的页面
  {
    path: '/',
    component: () => import('@/components/Layout/MainLayout.vue'),
    children: [
      // 仪表盘
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: () => import('@/views/dashboard/Dashboard.vue') },
      
      // 用户管理（管理员）
      { 
        path: 'user', 
        component: () => import('@/views/user/UserList.vue'),
        meta: { roles: ['ADMIN'] }
      },
      
      // 课程
      { path: 'course', component: () => import('@/views/course/CourseList.vue') },
      { path: 'course/:id', component: () => import('@/views/course/CourseDetail.vue') },
      
      // 题目管理（管理员/教师）
      { 
        path: 'question', 
        component: () => import('@/views/question/QuestionList.vue'),
        meta: { roles: ['ADMIN', 'TEACHER'] }
      },
      
      // 试卷管理（管理员/教师）
      { 
        path: 'paper', 
        component: () => import('@/views/paper/PaperList.vue'),
        meta: { roles: ['ADMIN', 'TEACHER'] }
      },
      
      // 考试
      { path: 'exam', component: () => import('@/views/exam/ExamList.vue') },
      { path: 'exam/:id', component: () => import('@/views/exam/ExamDetail.vue') },
      { path: 'exam/:id/results', component: () => import('@/views/exam/ExamResults.vue') },
      { path: 'exam/:id/review', component: () => import('@/views/exam/ExamReview.vue') },
      
      // 公告
      { path: 'announcement', component: () => import('@/views/announcement/AnnouncementList.vue') },
      
      // 轮播图（管理员）
      { 
        path: 'carousel', 
        component: () => import('@/views/carousel/CarouselList.vue'),
        meta: { roles: ['ADMIN'] }
      },
      
      // AI配置
      { path: 'aiconfig', component: () => import('@/views/aiconfig/AiConfigList.vue') },
      
      // 个人中心
      { path: 'profile', component: () => import('@/views/user/Profile.vue') },
    ]
  }
]
```

### 3.2 路由守卫

```typescript
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 需要登录的页面
  if (to.meta.requiresAuth && !authStore.token) {
    next('/login')
    return
  }
  
  // 角色权限检查
  if (to.meta.roles && !to.meta.roles.includes(authStore.user?.role)) {
    ElMessage.error('权限不足')
    next('/dashboard')
    return
  }
  
  next()
})
```

---

## 四、状态管理

### 4.1 认证状态 (auth.ts)

**文件位置**: `frontend/src/stores/auth.ts`

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getCurrentUser, changePassword } from '@/api/auth'
import type { User } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(null)
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isTeacher = computed(() => user.value?.role === 'TEACHER')
  const isStudent = computed(() => user.value?.role === 'STUDENT')
  
  // 登录
  async function loginAction(username: string, password: string) {
    const res = await login({ username, password })
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
  }
  
  // 注册
  async function registerAction(data: RegisterData) {
    const res = await register(data)
    return res.data
  }
  
  // 获取当前用户
  async function fetchCurrentUser() {
    if (!token.value) return
    try {
      const res = await getCurrentUser()
      user.value = res.data
    } catch (error) {
      logout()
    }
  }
  
  // 登出
  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }
  
  // 修改密码
  async function changePasswordAction(oldPassword: string, newPassword: string) {
    await changePassword({ oldPassword, newPassword })
  }
  
  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    isTeacher,
    isStudent,
    loginAction,
    registerAction,
    fetchCurrentUser,
    logout,
    changePasswordAction
  }
})
```

---

## 五、API 调用层

### 5.1 Axios 封装

**文件位置**: `frontend/src/utils/request.ts`

```typescript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器：添加Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data
    
    if (code === 200) {
      return { data, message }
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message))
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
```

### 5.2 API 模块示例

**认证API** (`frontend/src/api/auth.ts`):

```typescript
import request from '@/utils/request'
import type { LoginRequest, RegisterRequest, LoginResponse, User } from '@/types'

// 登录
export function login(data: LoginRequest) {
  return request.post<LoginResponse>('/auth/login', data)
}

// 注册
export function register(data: RegisterRequest) {
  return request.post<User>('/auth/register', data)
}

// 获取当前用户
export function getCurrentUser() {
  return request.get<User>('/auth/me')
}

// 修改密码
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.post('/auth/change-password', data)
}
```

**考试API** (`frontend/src/api/exam.ts`):

```typescript
import request from '@/utils/request'
import type { Exam, ExamDetail, ExamSession } from '@/types'

// 获取考试列表
export function getExams(params?: { courseId?: number; status?: string }) {
  return request.get<Exam[]>('/exams', { params })
}

// 获取已发布考试（学生）
export function getPublishedExams() {
  return request.get<Exam[]>('/exams/published')
}

// 获取我的考试（学生）
export function getMyExams() {
  return request.get<Exam[]>('/exams/my')
}

// 获取考试详情
export function getExamDetail(id: number) {
  return request.get<ExamDetail>(`/exams/${id}`)
}

// 开始考试
export function startExam(id: number) {
  return request.post<ExamSession>(`/exams/${id}/start`)
}

// 提交考试
export function submitExam(id: number, answers: Answer[]) {
  return request.post<ExamSession>(`/exams/${id}/submit`, { answers })
}

// 自动保存
export function autoSave(id: number, answers: Answer[]) {
  return request.put(`/exams/${id}/auto-save`, { answers })
}
```

---

## 六、组件设计

### 6.1 主布局组件

**文件位置**: `frontend/src/components/Layout/MainLayout.vue`

```vue
<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-if="!isCollapse">在线考试系统</span>
        <span v-else>考</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        class="menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        
        <el-menu-item index="/course">
          <el-icon><Reading /></el-icon>
          <span>课程</span>
        </el-menu-item>
        
        <el-menu-item index="/exam">
          <el-icon><Document /></el-icon>
          <span>考试</span>
        </el-menu-item>
        
        <!-- 教师和管理员菜单 -->
        <template v-if="isAdmin || isTeacher">
          <el-menu-item index="/question">
            <el-icon><Edit /></el-icon>
            <span>题库</span>
          </el-menu-item>
          
          <el-menu-item index="/paper">
            <el-icon><Files /></el-icon>
            <span>试卷</span>
          </el-menu-item>
        </template>
        
        <!-- 管理员菜单 -->
        <el-menu-item v-if="isAdmin" index="/user">
          <el-icon><User /></el-icon>
          <span>用户</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
        
        <div class="right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="user?.avatar" />
              <span>{{ user?.nickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)
const user = computed(() => authStore.user)
const isAdmin = computed(() => authStore.isAdmin)
const isTeacher = computed(() => authStore.isTeacher)

function handleCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>
```

### 6.2 分页列表 Hook

**文件位置**: `frontend/src/composables/usePagedList.ts`

```typescript
import { ref, reactive, watch } from 'vue'
import type { Ref } from 'vue'

interface Pagination {
  page: number
  pageSize: number
  total: number
}

export function usePagedList<T>(
  fetchFn: (params: any) => Promise<{ data: T[]; total: number }>,
  defaultPageSize = 10
) {
  const loading = ref(false)
  const list: Ref<T[]> = ref([])
  const pagination = reactive<Pagination>({
    page: 1,
    pageSize: defaultPageSize,
    total: 0
  })
  const filters = reactive<Record<string, any>>({})
  
  async function fetchData() {
    loading.value = true
    try {
      const { data, total } = await fetchFn({
        page: pagination.page,
        pageSize: pagination.pageSize,
        ...filters
      })
      list.value = data
      pagination.total = total
    } finally {
      loading.value = false
    }
  }
  
  function handlePageChange(page: number) {
    pagination.page = page
    fetchData()
  }
  
  function handleSizeChange(size: number) {
    pagination.pageSize = size
    pagination.page = 1
    fetchData()
  }
  
  function refresh() {
    pagination.page = 1
    fetchData()
  }
  
  return {
    loading,
    list,
    pagination,
    filters,
    fetchData,
    handlePageChange,
    handleSizeChange,
    refresh
  }
}
```

---

## 七、样式方案

### 7.1 设计Token

**文件位置**: `frontend/src/styles/design-tokens.scss`

采用 **Notion 风格极简设计**，黑白灰主色调。

```scss
// 颜色
$color-primary: #000000;
$color-secondary: #6b7280;
$color-border: #e5e7eb;
$color-bg: #ffffff;
$color-bg-secondary: #f9fafb;
$color-text: #111827;
$color-text-secondary: #6b7280;

// 间距
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;
$spacing-xl: 32px;

// 圆角
$radius-sm: 4px;
$radius-md: 8px;
$radius-lg: 12px;

// 字体
$font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
$font-size-sm: 12px;
$font-size-base: 14px;
$font-size-lg: 16px;
$font-size-xl: 20px;

// 阴影（极简风格，几乎不使用阴影）
$shadow-sm: none;
$shadow-md: none;
$shadow-lg: none;
```

### 7.2 全局样式

**文件位置**: `frontend/src/styles/global-styles.scss`

```scss
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: $font-family;
  font-size: $font-size-base;
  color: $color-text;
  background-color: $color-bg-secondary;
}

// 卡片样式
.card {
  background: $color-bg;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $spacing-lg;
}

// 表格样式
.el-table {
  border: 1px solid $color-border;
  border-radius: $radius-md;
  
  th {
    background-color: $color-bg-secondary !important;
  }
}

// 按钮样式
.el-button--primary {
  background-color: $color-primary;
  border-color: $color-primary;
  
  &:hover {
    background-color: #333333;
    border-color: #333333;
  }
}
```

---

## 八、TypeScript 类型定义

**文件位置**: `frontend/src/types/index.ts`

```typescript
// 用户角色
export type Role = 'ADMIN' | 'TEACHER' | 'STUDENT'

// 用户
export interface User {
  id: number
  username: string
  nickname: string
  avatar?: string
  role: Role
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
  createdAt: string
}

// 课程
export interface Course {
  id: number
  name: string
  code: string
  description?: string
  coverUrl?: string
  teacherId: number
  teacher?: User
  credits: number
  status: 'ACTIVE' | 'INACTIVE'
  memberCount?: number
}

// 题目类型
export type QuestionType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'TRUE_FALSE' | 'FILL_BLANK' | 'ESSAY'

// 难度
export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD'

// 题目
export interface Question {
  id: number
  content: string
  type: QuestionType
  difficulty: Difficulty
  score: number
  subject?: string
  options?: string[]
  correctAnswer: string | string[]
  scoringCriteria?: any
  teacherId: number
}

// 试卷
export interface Paper {
  id: number
  name: string
  courseId: number
  teacherId: number
  totalScore: number
  type: 'MANUAL' | 'AUTO'
  questions: Array<{ questionId: number; score: number }>
  createdAt: string
}

// 考试状态
export type ExamStatus = 'DRAFT' | 'PUBLISHED' | 'STARTED' | 'ENDED' | 'CANCELLED'

// 考试
export interface Exam {
  id: number
  title: string
  courseId: number
  paperId: number
  teacherId: number
  startedAt: string
  endedAt: string
  duration: number
  totalScore: number
  status: ExamStatus
  course?: Course
  paper?: Paper
}

// 考试详情（包含题目）
export interface ExamDetail extends Exam {
  questions: Question[]
}

// 考试记录状态
export type SessionStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'SUBMITTED' | 'GRADED'

// 考试记录
export interface ExamSession {
  id: number
  examId: number
  studentId: number
  startedAt: string
  submittedAt?: string
  score?: number
  answers: Record<string, { answer: any; score?: number; comment?: string }>
  status: SessionStatus
  gradingStatus: 'PENDING' | 'GRADING' | 'COMPLETED'
}

// API 响应
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 分页响应
export interface PageResponse<T> {
  data: T[]
  total: number
  page: number
  pageSize: number
}
```

---

## 九、构建配置

### vite.config.ts

**文件位置**: `frontend/vite.config.ts`

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  
  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'echarts': ['echarts'],
          'vendor': ['vue', 'vue-router', 'pinia']
        }
      }
    }
  }
})
```

---

## 十、开发命令

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview

# 类型检查
npm run type-check
```
