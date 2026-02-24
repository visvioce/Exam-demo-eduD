<template>
  <div class="profile">
    <el-row :gutter="20">
      <!-- 左侧用户信息 -->
      <el-col :span="8">
        <el-card class="user-card">
          <div class="user-avatar">
            <el-avatar :size="100" :src="userAvatar">
              <el-icon :size="50"><User /></el-icon>
            </el-avatar>
          </div>
          <div class="user-info">
            <h2>{{ user?.nickname || user?.username }}</h2>
            <p class="username">@{{ user?.username }}</p>
            <el-tag size="small">{{ getRoleName(user?.role) }}</el-tag>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ stats.examCount }}</div>
              <div class="stat-label">参加考试</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.courseCount }}</div>
              <div class="stat-label">加入课程</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.avgScore }}</div>
              <div class="stat-label">平均分</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧设置 -->
      <el-col :span="16">
        <el-card class="settings-card">
          <el-tabs v-model="activeTab">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="info">
              <el-form :model="infoForm" :rules="infoRules" ref="infoFormRef" label-width="100px">
                <el-form-item label="用户名">
                  <el-input :value="user?.username" disabled />
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="infoForm.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="角色">
                  <el-tag size="small">{{ getRoleName(user?.role) }}</el-tag>
                </el-form-item>
                <el-form-item label="状态">
                  <span class="form-value">{{ user?.status === 'ACTIVE' ? '正常' : '禁用' }}</span>
                </el-form-item>
                <el-form-item label="注册时间">
                  <span>{{ formatDate(user?.createdAt) }}</span>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateInfo" :loading="updatingInfo">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="修改密码" name="password">
              <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 考试记录 -->
            <el-tab-pane label="考试记录" name="exams" v-if="isStudent">
              <el-table :data="examSessions" v-loading="loadingExams">
                <el-table-column prop="examTitle" label="考试名称" min-width="200">
                  <template #default="{ row }">
                    {{ getExamTitle(row.examId) }}
                  </template>
                </el-table-column>
                <el-table-column prop="startedAt" label="开始时间" width="180">
                  <template #default="{ row }">
                    {{ formatDate(row.startedAt) }}
                  </template>
                </el-table-column>
                <el-table-column prop="submittedAt" label="提交时间" width="180">
                  <template #default="{ row }">
                    {{ formatDate(row.submittedAt) }}
                  </template>
                </el-table-column>
                <el-table-column prop="score" label="得分" width="100">
                  <template #default="{ row }">
                    <span :class="{ 'high-score': row.score >= 60, 'low-score': row.score < 60 }">
                      {{ row.score ?? '-' }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag size="small">{{ getSessionStatusName(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="{ row }">
                    <el-button size="small" @click="viewResult(row)">查看</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <!-- 我的课程 -->
            <el-tab-pane label="我的课程" name="courses" v-if="isStudent">
              <el-table :data="myCourses" v-loading="loadingCourses">
                <el-table-column prop="name" label="课程名称" min-width="200" />
                <el-table-column prop="code" label="课程代码" width="120" />
                <el-table-column label="教师" width="120">
                  <template #default="{ row }">
                    {{ row.teacherName || (row.teacherId ? `教师#${row.teacherId}` : '-') }}
                  </template>
                </el-table-column>
                <el-table-column prop="credits" label="学分" width="80" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag size="small">{{ row.status === 'ACTIVE' ? '进行中' : '已结束' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="100">
                  <template #default="{ row }">
                    <el-button size="small" @click="goToCourse(row)">查看</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'
import { courseApi } from '@/api/course'
import { examSessionApi } from '@/api/exam'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { formatDate, getRoleName as formatRoleName, getSessionStatusName } from '@/utils/format'
import { getErrorMessage } from '@/utils/error'
import type { FormInstance, FormRules } from 'element-plus'
import type { Course, ExamSession } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const user = computed(() => authStore.user)
const isStudent = computed(() => user.value?.role === 'STUDENT')
const userAvatar = computed(() => user.value?.avatar || '')

const activeTab = ref('info')
const updatingInfo = ref(false)
const changingPassword = ref(false)
const loadingExams = ref(false)
const loadingCourses = ref(false)

const stats = reactive({
  examCount: 0,
  courseCount: 0,
  avgScore: 0
})

const infoFormRef = ref<FormInstance>()
const infoForm = reactive({
  nickname: ''
})

const infoRules = reactive<FormRules>({
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
})

const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = reactive<FormRules>({
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

const examSessions = ref<ExamSession[]>([])
const myCourses = ref<Course[]>([])

function getRoleName(role?: string) {
  return role ? formatRoleName(role) : ''
}

function getExamTitle(examId: number): string {
  // 可以从缓存中获取考试名称
  return `考试 #${examId}`
}

async function loadStats() {
  if (isStudent.value) {
    try {
      const [sessionsRes, coursesRes] = await Promise.all([
        examSessionApi.getMySessions(),
        courseApi.getMyCourses()
      ])
      
      const sessions = sessionsRes.data || []
      const courses = coursesRes.data || []
      
      stats.examCount = sessions.filter((s: ExamSession) => s.gradingStatus === 'COMPLETED').length
      stats.courseCount = courses.length
      
      const gradedSessions = sessions.filter((s: ExamSession) => s.score !== null && s.score !== undefined)
      if (gradedSessions.length > 0) {
        stats.avgScore = Math.round(gradedSessions.reduce((sum: number, s: ExamSession) => sum + (s.score || 0), 0) / gradedSessions.length)
      }
    } catch (error) {
      // 统计数据加载失败，使用默认值
    }
  }
}

async function loadExamSessions() {
  if (!isStudent.value) return
  loadingExams.value = true
  try {
    const res = await examSessionApi.getMySessions()
    examSessions.value = res.data || []
  } catch (error) {
    ElMessage.error('加载考试记录失败')
  } finally {
    loadingExams.value = false
  }
}

async function loadMyCourses() {
  if (!isStudent.value) return
  loadingCourses.value = true
  try {
    const res = await courseApi.getMyCourses()
    myCourses.value = res.data || []
  } catch (error) {
    ElMessage.error('加载课程失败')
  } finally {
    loadingCourses.value = false
  }
}

async function handleUpdateInfo() {
  if (!infoFormRef.value) return

  await infoFormRef.value.validate(async (valid) => {
    if (valid) {
      updatingInfo.value = true
      try {
        await authApi.updateProfile({
          nickname: infoForm.nickname,
          avatar: user.value?.avatar
        })
        // 更新本地用户信息
        await authStore.getCurrentUser()
        ElMessage.success('更新成功')
      } catch (error: unknown) {
        ElMessage.error(getErrorMessage(error, '更新失败'))
      } finally {
        updatingInfo.value = false
      }
    }
  })
}

async function handleChangePassword() {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changingPassword.value = true
      try {
        await authApi.changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功')
        // 清空表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } catch (error: unknown) {
        ElMessage.error(getErrorMessage(error, '密码修改失败'))
      } finally {
        changingPassword.value = false
      }
    }
  })
}

function viewResult(session: ExamSession) {
  router.push(`/exam/${session.examId}/results?sessionId=${session.id}`)
}

function goToCourse(course: Course) {
  router.push(`/course/${course.id}`)
}

onMounted(async () => {
  // 初始化表单
  if (user.value) {
    infoForm.nickname = user.value.nickname || ''
  }
  
  // 加载数据
  loadStats()
  loadExamSessions()
  loadMyCourses()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.profile {
  .user-card {
    text-align: center;

    .user-avatar {
      margin-bottom: $spacing-lg;

      :deep(.el-avatar) {
        background: $bg-secondary;
        color: $text-secondary;
      }
    }

    .user-info {
      h2 {
        margin: 0 0 $spacing-sm 0;
        font-size: $font-size-lg;
        font-weight: $font-weight-medium;
        color: $text-primary;
      }

      .username {
        color: $text-tertiary;
        margin-bottom: $spacing-md;
        font-size: $font-size-sm;
      }
    }

    .user-stats {
      display: flex;
      justify-content: space-around;
      margin-top: $spacing-xl;
      padding-top: $spacing-lg;
      border-top: 1px solid $border-color;

      .stat-item {
        text-align: center;

        .stat-value {
          font-size: $font-size-lg;
          font-weight: $font-weight-medium;
          color: $text-primary;
          margin-bottom: $spacing-xs;
        }

        .stat-label {
          font-size: $font-size-xs;
          color: $text-tertiary;
        }
      }
    }
  }

  .settings-card {
    min-height: 500px;

    .high-score {
      color: $success;
    }

    .low-score {
      color: $danger;
    }
  }
}
</style>
