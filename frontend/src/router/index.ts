import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { requiresAuth: false, title: '注册' }
  },
  {
    // 答题页面 - 独立全屏页面，不显示侧边栏导航
    path: '/exam/:id/take',
    name: 'ExamTake',
    component: () => import('@/views/exam/ExamTake.vue'),
    meta: { requiresAuth: true, roles: ['STUDENT'], title: '参加考试' }
  },
  {
    path: '/',
    component: () => import('@/components/Layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { requiresAuth: true, title: '首页' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/UserList.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'], title: '用户管理' }
      },
      {
        path: 'course',
        name: 'CourseList',
        component: () => import('@/views/course/CourseList.vue'),
        meta: { requiresAuth: true, title: '课程' }
      },
      {
        path: 'course/:id',
        name: 'CourseDetail',
        component: () => import('@/views/course/CourseDetail.vue'),
        meta: { requiresAuth: true, title: '课程详情' }
      },
      {
        path: 'question',
        name: 'QuestionList',
        component: () => import('@/views/question/QuestionList.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '题库管理' }
      },
      {
        path: 'question/create',
        name: 'QuestionCreate',
        redirect: { name: 'QuestionList', query: { action: 'create' } },
        meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '创建题目' }
      },
      {
        path: 'question/:id/edit',
        name: 'QuestionEdit',
        redirect: to => ({ name: 'QuestionList', query: { action: 'edit', id: String(to.params.id) } }),
        meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '编辑题目' }
      },
      {
        path: 'paper',
        name: 'PaperList',
        component: () => import('@/views/paper/PaperList.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '试卷管理' }
      },
      {
        path: 'exam',
        name: 'ExamList',
        component: () => import('@/views/exam/ExamList.vue'),
        meta: { requiresAuth: true, title: '考试' }
      },
      {
        path: 'exam/:id',
        name: 'ExamDetail',
        component: () => import('@/views/exam/ExamDetail.vue'),
        meta: { requiresAuth: true, title: '考试详情' }
      },
      {
        path: 'exam/:id/results',
        name: 'ExamResults',
        component: () => import('@/views/exam/ExamResults.vue'),
        meta: { requiresAuth: true, title: '考试结果' }
      },
      {
        path: 'exam/:id/review',
        name: 'ExamReview',
        component: () => import('@/views/exam/ExamReview.vue'),
        meta: { requiresAuth: true, title: '考试回顾' }
      },
      {
        path: 'exam/:id/student/:studentId',
        name: 'ExamStudentResult',
        component: () => import('@/views/exam/ExamResults.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN', 'TEACHER'], title: '学生考试结果' }
      },
      {
        path: 'announcement',
        name: 'AnnouncementList',
        component: () => import('@/views/announcement/AnnouncementList.vue'),
        meta: { requiresAuth: true, title: '公告' }
      },
      {
        path: 'carousel',
        name: 'CarouselList',
        component: () => import('@/views/carousel/CarouselList.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'], title: '轮播图管理' }
      },
      {
        path: 'aiconfig',
        name: 'AiConfigList',
        component: () => import('@/views/aiconfig/AiConfigList.vue'),
        meta: { requiresAuth: true, title: 'AI配置' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { requiresAuth: true, title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 南方职业学院在线考试系统`
  }

  // 需要认证的页面
  if (to.meta.requiresAuth !== false) {
    if (!authStore.token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    // 刷新后先恢复用户信息，避免角色判断误拦截
    if (!authStore.user) {
      const loaded = await authStore.getCurrentUser()
      if (!loaded || !authStore.user) {
        // token 无效或网络错误，跳转到登录页
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }
    }

    // 角色权限检查
    const roles = to.meta.roles as string[] | undefined
    if (roles && roles.length > 0) {
      const userRole = authStore.user?.role
      if (!userRole || !roles.includes(userRole)) {
        ElMessage.error('无权访问该页面')
        next({ name: 'Dashboard' })
        return
      }
    }
  }

  next()
})

export default router
