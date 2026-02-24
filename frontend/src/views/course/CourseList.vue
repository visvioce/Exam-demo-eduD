<template>
  <div class="course-list">
    <div class="page-header">
      <h2>{{ isStudent ? '我的课程' : '课程管理' }}</h2>
      <el-button type="primary" @click="handleCreate" v-if="hasPermission(['ADMIN', 'TEACHER'])">
        <el-icon><Plus /></el-icon>
        创建课程
      </el-button>
    </div>

    <!-- 课程卡片列表 -->
    <el-row :gutter="16" v-loading="loading">
      <el-col :span="8" v-for="course in courses" :key="course.id">
        <el-card class="course-card" shadow="never">
          <div class="course-card__header">
            <h3 class="course-card__title">{{ course.name }}</h3>
            <el-tag size="small" type="info">{{ course.code }}</el-tag>
          </div>
          <div class="course-card__info">
            <div class="info-item">
              <span class="info-label">教师：</span>
              <span>{{ getTeacherDisplayName(course) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">学分：</span>
              <span>{{ course.credits }}</span>
            </div>
            <div class="info-item" v-if="course.deadline">
              <span class="info-label">截止：</span>
              <span>{{ formatDate(course.deadline) }}</span>
            </div>
          </div>
          <div class="course-card__footer">
            <el-tag :type="course.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ course.status === 'ACTIVE' ? '进行中' : '已结束' }}
            </el-tag>
            <div class="course-card__actions">
              <ActionButtons
                @view="handleView(course)"
                @edit="handleEdit(course)"
                @delete="handleDelete(course)"
                :show-edit="hasPermission(['ADMIN', 'TEACHER'])"
                :show-delete="hasPermission(['ADMIN', 'TEACHER'])"
              />
              <el-button size="small" type="success" @click="handleJoin(course)"
                v-if="!hasPermission(['ADMIN', 'TEACHER']) && !isJoined(course)">加入</el-button>
              <el-button size="small" type="warning" @click="handleLeave(course)"
                v-if="!hasPermission(['ADMIN', 'TEACHER']) && isJoined(course)">退出</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑课程' : '创建课程'" width="600px">
      <el-form :model="courseForm" :rules="rules" ref="courseFormRef" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程代码" prop="code">
          <el-input v-model="courseForm.code" placeholder="请输入课程代码" />
        </el-form-item>
        <el-form-item label="学分" prop="credits">
          <el-input-number v-model="courseForm.credits" :min="0.5" :max="10" :step="0.5" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="courseForm.status" placeholder="请选择状态" class="full-width">
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已结束" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="courseForm.deadline" type="date" placeholder="选择截止日期" class="full-width" />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="courseForm.description" type="textarea" :rows="4" placeholder="请输入课程描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { courseApi } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/format'
import { getErrorMessage } from '@/utils/error'
import type { FormInstance, FormRules } from 'element-plus'
import type { Course } from '@/types'
import ActionButtons from '@/components/ActionButtons.vue'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const courses = ref<Course[]>([])
const myCourses = ref<Course[]>([])
const editDialogVisible = ref(false)
const isEdit = ref(false)
const courseFormRef = ref<FormInstance>()

const courseForm = reactive({
  id: 0,
  name: '',
  code: '',
  description: '',
  credits: 3.0,
  status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE',
  deadline: ''
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  credits: [{ required: true, message: '请输入学分', trigger: 'blur' }]
})

function hasPermission(roles: string[]) {
  const userRole = authStore.user?.role
  return roles.includes(userRole || '')
}

const isStudent = computed(() => authStore.user?.role === 'STUDENT')

async function loadCourses() {
  loading.value = true
  try {
    let res
    if (hasPermission(['ADMIN', 'TEACHER'])) {
      // 管理员和教师查看自己管理的课程
      res = await courseApi.list()
    } else {
      // 学生查看活跃课程
      res = await courseApi.getActiveCourses()
      // 同时加载学生已加入的课程
      const myRes = await courseApi.getMyCourses()
      myCourses.value = myRes.data
    }
    courses.value = res.data
  } catch (error) {
    ElMessage.error('加载课程失败')
  } finally {
    loading.value = false
  }
}

// 使用 Set 优化课程加入状态检查
const joinedCourseIds = computed(() => new Set(myCourses.value.map(c => c.id)))

// 检查是否已加入课程
function isJoined(course: Course) {
  return joinedCourseIds.value.has(course.id)
}

function getTeacherDisplayName(course: Course): string {
  if (course.teacherName && course.teacherName.trim()) {
    return course.teacherName
  }
  if (course.teacherId) {
    return `教师#${course.teacherId}`
  }
  return '-'
}

// 加入课程
async function handleJoin(course: Course) {
  try {
    await courseApi.join(course.id)
    ElMessage.success(`已加入课程：${course.name}`)
    // 重新加载已加入的课程列表
    const res = await courseApi.getMyCourses()
    myCourses.value = res.data
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, '加入课程失败'))
  }
}

// 退出课程
async function handleLeave(course: Course) {
  try {
    await ElMessageBox.confirm(`确定要退出课程"${course.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await courseApi.leave(course.id)
    ElMessage.success(`已退出课程：${course.name}`)
    // 重新加载已加入的课程列表
    const res = await courseApi.getMyCourses()
    myCourses.value = res.data
  } catch (error) {
    // 用户取消或处理错误
    if (error !== 'cancel') {
      ElMessage.error('退出课程失败')
    }
  }
}

function handleCreate() {
  isEdit.value = false
  Object.assign(courseForm, {
    id: 0,
    name: '',
    code: '',
    description: '',
    credits: 3.0,
    status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE',
    deadline: ''
  })
  editDialogVisible.value = true
}

function handleView(row: Course) {
  router.push(`/course/${row.id}`)
}

function handleEdit(row: Course) {
  isEdit.value = true
  Object.assign(courseForm, {
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description || '',
    credits: row.credits,
    status: row.status,
    deadline: row.deadline || ''
  })
  editDialogVisible.value = true
}

async function handleDelete(row: Course) {
  try {
    await ElMessageBox.confirm('确定要删除该课程吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await courseApi.delete(row.id)
    ElMessage.success('删除成功')
    loadCourses()
  } catch {
    // 取消删除
  }
}

async function handleSubmit() {
  if (!courseFormRef.value) return

  await courseFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = {
          name: courseForm.name,
          code: courseForm.code,
          description: courseForm.description,
          credits: courseForm.credits,
          status: courseForm.status,
          deadline: courseForm.deadline || undefined
        }

        if (isEdit.value) {
          await courseApi.update(courseForm.id, data)
          ElMessage.success('更新成功')
        } else {
          await courseApi.create(data)
          ElMessage.success('创建成功')
        }
        editDialogVisible.value = false
        loadCourses()
      } catch (error: unknown) {
        ElMessage.error(getErrorMessage(error, '操作失败'))
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.course-list {
  padding: $spacing-xl;

  .full-width {
    width: 100%;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-xl;

    h2 {
      margin: 0;
      font-size: $font-size-3xl;
      font-weight: $font-weight-medium;
      color: $text-primary;
      letter-spacing: -0.5px;
    }
  }

  .course-card {
    margin-bottom: $spacing-lg;

    &__header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: $spacing-md;
    }

    &__title {
      margin: 0;
      font-size: $font-size-xl;
      font-weight: $font-weight-medium;
      color: $text-primary;
      line-height: 1.3;
    }

    &__info {
      margin-bottom: $spacing-lg;

      .info-item {
        font-size: $font-size-sm;
        color: $text-secondary;
        margin-bottom: $spacing-sm;

        &:last-child {
          margin-bottom: 0;
        }

        .info-label {
          color: $text-tertiary;
        }
      }
    }

    &__footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: $spacing-md;
      border-top: 1px solid $border-light;
    }

    &__actions {
      display: flex;
      gap: $spacing-sm;
    }
  }
}

// 对话框样式优化
:deep(.el-dialog) {
  .el-dialog__header {
    border-bottom: 1px solid $border-light;
    padding: $spacing-lg $spacing-xl;
  }

  .el-dialog__body {
    padding: $spacing-xl;
  }

  .el-dialog__footer {
    border-top: 1px solid $border-light;
    padding: $spacing-lg $spacing-xl;
  }
}

// 响应式
@media (max-width: $breakpoint-md) {
  .course-list {
    padding: $spacing-md;

    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: $spacing-md;

      h2 {
        font-size: $font-size-2xl;
      }
    }
  }
}
</style>
