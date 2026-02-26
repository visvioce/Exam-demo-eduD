<template>
  <div class="course-list base-list-page">
    <div class="page-header">
      <h2>{{ isStudent ? '我的课程' : '课程管理' }}</h2>
      <el-button type="primary" @click="handleCreate" v-if="hasPermission(['ADMIN', 'TEACHER'])">
        <el-icon><Plus /></el-icon>
        创建课程
      </el-button>
    </div>

    <div v-if="isStudent" class="student-course-section" v-loading="loading">
      <div class="section-header">
        <span>已加入课程</span>
        <span class="section-count">{{ myCourses.length }} 门</span>
      </div>

      <el-row v-if="myCourses.length > 0" :gutter="16">
        <el-col :xs="24" :sm="12" :lg="8" v-for="course in myCourses" :key="course.id">
          <el-card class="course-card course-card--clickable ui-interactive-surface" shadow="never" @click="handleView(course)">
            <div class="course-card__cover">
              <img v-if="course.coverUrl" :src="course.coverUrl" :alt="course.name" />
              <div v-else class="course-card__cover-placeholder">课程封面</div>
            </div>
            <div class="course-card__header">
              <h3 class="course-card__title ui-title-clamp-2">{{ course.name }}</h3>
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
              <el-tag size="small" type="success">已参加</el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="你还没有加入任何课程" />
    </div>

    <!-- 教师/管理员课程列表 -->
    <el-row v-else :gutter="16" v-loading="loading">
      <el-col :xs="24" :sm="12" :lg="8" v-for="course in courses" :key="course.id">
        <el-card class="course-card" shadow="never">
          <div class="course-card__cover">
            <img v-if="course.coverUrl" :src="course.coverUrl" :alt="course.name" />
            <div v-else class="course-card__cover-placeholder">课程封面</div>
          </div>
          <div class="course-card__header">
            <h3 class="course-card__title ui-title-clamp-2">{{ course.name }}</h3>
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
                :show-edit="canManage(course)"
                :show-delete="canManage(course)"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑课程' : '创建课程'" width="600px" class="base-dialog">
      <el-form :model="courseForm" :rules="rules" ref="courseFormRef" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程代码" prop="code">
          <el-input v-model="courseForm.code" placeholder="请输入课程代码" />
        </el-form-item>
        <el-form-item label="封面地址">
          <el-input v-model="courseForm.coverUrl" placeholder="请输入课程封面URL（可选）" />
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

const defaultCourseForm = {
  id: 0,
  name: '',
  code: '',
  description: '',
  coverUrl: '',
  credits: 3.0,
  status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE',
  deadline: ''
}

const courseForm = reactive({ ...defaultCourseForm })

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

function canManage(course: Course) {
  const role = authStore.user?.role
  return (role === 'ADMIN' || role === 'TEACHER') && course.teacherId === authStore.user?.id
}

async function loadCourses() {
  loading.value = true
  try {
    if (hasPermission(['ADMIN', 'TEACHER'])) {
      const managedRes = await courseApi.list()
      courses.value = managedRes.data || []
      return
    }

    const myRes = await courseApi.getMyCourses()
    myCourses.value = myRes.data || []
  } catch (error) {
    ElMessage.error('加载课程失败')
  } finally {
    loading.value = false
  }
}

function resetCourseForm(overrides: Partial<typeof defaultCourseForm> = {}) {
  Object.assign(courseForm, defaultCourseForm, overrides)
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

function handleCreate() {
  isEdit.value = false
  resetCourseForm()
  editDialogVisible.value = true
}

function handleView(row: Course) {
  router.push(`/course/${row.id}`)
}

function handleEdit(row: Course) {
  isEdit.value = true
  resetCourseForm({
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description || '',
    coverUrl: row.coverUrl || '',
    credits: row.credits,
    status: row.status as 'ACTIVE' | 'INACTIVE',
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
    void loadCourses()
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
          coverUrl: courseForm.coverUrl || undefined,
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
        void loadCourses()
      } catch (error: unknown) {
        ElMessage.error(getErrorMessage(error, '操作失败'))
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  void loadCourses()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;
@use '@/styles/views/base-list.scss';

.course-list {
  .student-course-section {
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: $spacing-md;
      color: $text-secondary;

      .section-count {
        color: $text-tertiary;
        font-size: $font-size-sm;
      }
    }
  }

  .full-width {
    width: 100%;
  }

  .course-card {
    margin-bottom: $spacing-lg;

    &__cover {
      width: 100%;
      height: 148px;
      border-radius: $radius-sm;
      overflow: hidden;
      margin-bottom: $spacing-md;
      border: 1px solid $border-light;
      background: $bg-hover;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }
    }

    &__cover-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: $text-tertiary;
      font-size: $font-size-sm;
    }

    &--clickable {
      cursor: pointer;
    }

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
</style>
