<template>
  <div class="course-detail base-detail-page">
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="text-large font-600 mr-3">{{ course?.name || '课程详情' }}</span>
        </template>
      </el-page-header>
    </div>

    <el-card v-loading="loading">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="课程名称">{{ course?.name }}</el-descriptions-item>
        <el-descriptions-item label="课程代码">{{ course?.code }}</el-descriptions-item>
        <el-descriptions-item label="授课教师">{{ getTeacherDisplayName() }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ course?.credits }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="course?.status === 'ACTIVE' ? 'success' : 'info'">
            {{ course?.status === 'ACTIVE' ? '进行中' : '已结束' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ formatDate(course?.deadline) }}</el-descriptions-item>
        <el-descriptions-item label="课程描述" :span="2">{{ course?.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 操作按钮 -->
      <div class="actions">
        <template v-if="isStudent">
          <el-button v-if="!isJoined" type="primary" @click="handleJoin" :loading="joining">
            加入课程
          </el-button>
          <el-button v-else type="danger" @click="handleLeave" :loading="leaving">
            退出课程
          </el-button>
        </template>
        <template v-else-if="canEdit">
          <el-button type="primary" @click="handleEdit">编辑课程</el-button>
          <el-button type="danger" @click="handleDelete">删除课程</el-button>
        </template>
      </div>
    </el-card>

    <!-- 课程成员 -->
    <el-card class="members-card" v-if="showMembers">
      <template #header>
        <div class="card-header">
          <span>课程成员</span>
          <span class="member-count">共 {{ members.length }} 人</span>
        </div>
      </template>
      <el-table :data="members" v-loading="membersLoading" stripe max-height="400">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleColor(row.role)" size="small">{{ getRoleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'" size="small">
              {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 课程考试 -->
    <el-card class="exams-card">
      <template #header>
        <span>课程考试</span>
      </template>
      <el-table :data="exams" v-loading="examsLoading" stripe>
        <el-table-column prop="title" label="考试名称" min-width="200" />
        <el-table-column prop="startedAt" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.startedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="endedAt" label="结束时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.endedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="goToExam(row)">
              {{ isStudent ? '参加' : '查看' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑课程" width="500px" class="base-dialog">
      <el-form :model="courseForm" :rules="rules" ref="courseFormRef" label-width="80px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程代码" prop="code">
          <el-input v-model="courseForm.code" placeholder="请输入课程代码" />
        </el-form-item>
        <el-form-item label="学分" prop="credits">
          <el-input-number v-model="courseForm.credits" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="courseForm.status">
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已结束" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="courseForm.deadline" type="date" placeholder="选择截止日期" class="full-width" />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="updating">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { courseApi } from '@/api/course'
import { examApi } from '@/api/exam'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDate, getRoleColor, getRoleName, getStatusColor, getStatusName } from '@/utils/format'
import { getErrorMessage } from '@/utils/error'
import type { FormInstance, FormRules } from 'element-plus'
import type { Course, User, Exam } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const membersLoading = ref(false)
const examsLoading = ref(false)
const joining = ref(false)
const leaving = ref(false)
const updating = ref(false)
const editDialogVisible = ref(false)

const course = ref<Course | null>(null)
const members = ref<User[]>([])
const exams = ref<Exam[]>([])
const isJoined = ref(false)
const membersLoaded = ref(false)

const courseFormRef = ref<FormInstance>()
const courseForm = reactive({
  name: '',
  code: '',
  credits: 1,
  status: 'ACTIVE',
  deadline: '',
  description: ''
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  credits: [{ required: true, message: '请输入学分', trigger: 'blur' }]
})

const courseId = computed(() => Number(route.params.id))
const isStudent = computed(() => authStore.user?.role === 'STUDENT')
const canEdit = computed(() => {
  if (authStore.user?.role === 'ADMIN') return true
  if (authStore.user?.role === 'TEACHER' && course.value?.teacherId === authStore.user?.id) return true
  return false
})
const showMembers = computed(() => canEdit.value || isJoined.value)

function goBack() {
  router.back()
}

function getTeacherDisplayName(): string {
  if (!course.value) return '-'
  if (course.value.teacherName && course.value.teacherName.trim()) {
    return course.value.teacherName
  }
  if (course.value.teacherId) {
    return `教师#${course.value.teacherId}`
  }
  return '-'
}

async function loadCourse() {
  loading.value = true
  try {
    const res = await courseApi.getById(courseId.value)
    course.value = res.data
  } catch (error) {
    ElMessage.error('加载课程失败')
  } finally {
    loading.value = false
  }
}

async function loadMembers() {
  membersLoading.value = true
  try {
    const res = await courseApi.getMembers(courseId.value)
    members.value = res.data || []
  } catch (error) {
    ElMessage.error('加载成员失败')
  } finally {
    membersLoading.value = false
  }
}

async function loadExams() {
  examsLoading.value = true
  try {
    if (isStudent.value) {
      const res = await examApi.getMyExams()
      exams.value = (res.data || []).filter((exam) => exam.courseId === courseId.value)
    } else {
      const res = await examApi.page({ current: 1, size: 100, courseId: courseId.value })
      exams.value = res.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载考试失败')
  } finally {
    examsLoading.value = false
  }
}

async function checkJoined() {
  if (!isStudent.value) return
  try {
    // 使用优化的检查接口，避免获取所有课程
    const res = await courseApi.checkJoined(courseId.value)
    isJoined.value = res.data
    if (isJoined.value) {
      loadMembers()
    }
  } catch (error) {
    // 如果接口失败，静默处理
    isJoined.value = false
  }
}

async function handleJoin() {
  joining.value = true
  try {
    await courseApi.join(courseId.value)
    ElMessage.success('加入成功')
    isJoined.value = true
    loadMembers()
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, '加入失败'))
  } finally {
    joining.value = false
  }
}

async function handleLeave() {
  leaving.value = true
  try {
    await ElMessageBox.confirm('确定要退出该课程吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await courseApi.leave(courseId.value)
    ElMessage.success('退出成功')
    isJoined.value = false
    members.value = []
    membersLoaded.value = false
  } catch {
    // 取消
  } finally {
    leaving.value = false
  }
}

function handleEdit() {
  if (!course.value) return
  Object.assign(courseForm, {
    name: course.value.name,
    code: course.value.code,
    credits: course.value.credits,
    status: course.value.status,
    deadline: course.value.deadline || '',
    description: course.value.description || ''
  })
  editDialogVisible.value = true
}

async function handleUpdate() {
  if (!courseFormRef.value) return

  await courseFormRef.value.validate(async (valid) => {
    if (valid) {
      updating.value = true
      try {
        await courseApi.update(courseId.value, {
          name: courseForm.name,
          code: courseForm.code,
          credits: courseForm.credits,
          status: courseForm.status as 'ACTIVE' | 'INACTIVE',
          deadline: courseForm.deadline || undefined,
          description: courseForm.description
        })
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        loadCourse()
      } catch (error: unknown) {
        ElMessage.error(getErrorMessage(error, '更新失败'))
      } finally {
        updating.value = false
      }
    }
  })
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除该课程吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await courseApi.delete(courseId.value)
    ElMessage.success('删除成功')
    router.push('/course')
  } catch {
    // 取消
  }
}

function goToExam(exam: Exam) {
  if (isStudent.value) {
    router.push(`/exam/${exam.id}/take`)
  } else {
    router.push(`/exam/${exam.id}`)
  }
}

onMounted(() => {
  loadCourse()
  checkJoined()
  loadExams()
})

watchEffect(() => {
  if (showMembers.value && !membersLoaded.value) {
    membersLoaded.value = true
    loadMembers()
  } else if (!showMembers.value && membersLoaded.value) {
    membersLoaded.value = false
    members.value = []
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;
@use '@/styles/views/base-list.scss';
@use '@/styles/views/base-detail.scss';

.course-detail {
  .actions {
    margin-top: $spacing-xl;
    text-align: center;
  }

  .full-width {
    width: 100%;
  }

  .members-card,
  .exams-card {
    margin-top: $spacing-xl;
    border: 1px solid $border-color;
    border-radius: $radius-md;
    background: $bg-primary;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .member-count {
        color: $text-tertiary;
        font-size: $font-size-sm;
      }
    }
  }
}
</style>
