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
      <div class="course-cover" v-if="course?.coverUrl">
        <img :src="course.coverUrl" :alt="course.name" />
      </div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="课程名称">{{ course?.name }}</el-descriptions-item>
        <el-descriptions-item label="课程代码">{{ course?.code }}</el-descriptions-item>
        <el-descriptions-item label="封面地址" :span="2">
          <el-link v-if="course?.coverUrl" :href="course.coverUrl" target="_blank" type="primary">{{ course.coverUrl }}</el-link>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="授课教师">{{ getTeacherDisplayName() }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ course?.credits }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="course?.status === 'ACTIVE' ? 'success' : 'info'">
            {{ course?.status === 'ACTIVE' ? '进行中' : '已结束' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ formatDate(course?.deadline) }}</el-descriptions-item>
        <el-descriptions-item label="课程描述" :span="2">{{ course?.description || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="showMembers" label="课程成员" :span="2">
          <div class="members-inline">
            <div class="members-inline-header">
              <span class="member-count">共 {{ members.length }} 人</span>
            </div>
            <div class="members-inline-list" v-loading="membersLoading">
              <template v-if="members.length > 0">
                <div class="member-inline-item" v-for="row in members" :key="row.id">
                  <el-avatar :size="28" :src="row.avatar">
                    {{ (row.nickname || row.username || '?').slice(0, 1) }}
                  </el-avatar>
                  <span class="member-inline-name">{{ row.nickname || row.username || `用户#${row.id}` }}</span>
                </div>
              </template>
              <el-empty v-else description="暂无成员" :image-size="40" />
            </div>
          </div>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 操作按钮 -->
      <div class="actions">
        <template v-if="isStudent">
          <el-button
            v-if="!isJoined"
            type="primary"
            @click="handleJoin"
            :loading="joining"
            :disabled="!canJoinCourse"
          >
            {{ canJoinCourse ? '加入课程' : '课程不可加入' }}
          </el-button>
          <el-button v-else type="danger" @click="handleLeave" :loading="leaving">
            退出课程
          </el-button>
        </template>
        <template v-else-if="canEdit">
          <ActionButtons
            :show-view="false"
            @edit="handleEdit"
            @delete="handleDelete"
          />
        </template>
      </div>
    </el-card>

    <!-- 课程考试 -->
    <el-card class="exams-card">
      <template #header>
        <div class="card-header">
          <span>课程考试</span>
          <span v-if="isStudent && !isJoined" class="member-count">可预览，加入课程后可参加考试</span>
        </div>
      </template>
      <el-table :data="exams" v-loading="examsLoading" stripe table-layout="fixed" :fit="true" class="detail-table">
        <el-table-column prop="title" label="考试名称" min-width="200" />
        <el-table-column prop="startedAt" label="开始时间" min-width="168">
          <template #default="{ row }">
            {{ formatDate(row.startedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="endedAt" label="结束时间" min-width="168">
          <template #default="{ row }">
            {{ formatDate(row.endedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" min-width="108" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="120">
          <template #default="{ row }">
            <el-button
              v-if="isStudent"
              size="small"
              type="primary"
              :disabled="!canTakeCourseExam(row)"
              @click="goToExam(row)"
            >
              {{ getStudentExamActionText(row) }}
            </el-button>
            <ActionButtons
              v-else
              :show-edit="false"
              :show-delete="false"
              @view="goToExam(row)"
            />
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
        <el-form-item label="封面地址">
          <el-input v-model="courseForm.coverUrl" placeholder="请输入课程封面URL（可选）" />
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
import { formatDate, getStatusColor, getStatusName } from '@/utils/format'
import { getErrorMessage } from '@/utils/error'
import type { FormInstance, FormRules } from 'element-plus'
import type { Course, User, Exam } from '@/types'
import ActionButtons from '@/components/ActionButtons.vue'

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
  coverUrl: '',
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
  const role = authStore.user?.role
  return (role === 'TEACHER' || role === 'ADMIN') && course.value?.teacherId === authStore.user?.id
})
const showMembers = computed(() => canEdit.value || isJoined.value)
const canJoinCourse = computed(() => {
  if (!course.value || isJoined.value) return false
  if (course.value.status !== 'ACTIVE') return false
  if (!course.value.deadline) return true
  return new Date(course.value.deadline).getTime() >= Date.now()
})

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
  } catch (error: unknown) {
    // 兼容后端权限策略未更新的场景：学生仍可通过课程列表预览基础信息
    if (isStudent.value) {
      try {
        const [activeRes, myRes] = await Promise.all([
          courseApi.getActiveCourses(),
          courseApi.getMyCourses()
        ])
        const previewCourse = [...(myRes.data || []), ...(activeRes.data || [])]
          .find((item) => item.id === courseId.value)
        if (previewCourse) {
          course.value = previewCourse
          ElMessage.warning('当前为课程预览模式，加入课程后可参与考试与成员相关功能')
          return
        }
      } catch {
        // 忽略降级查询失败，走统一错误提示
      }
    }
    ElMessage.error(getErrorMessage(error, '加载课程失败'))
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
      if (isJoined.value) {
        const res = await examApi.getMyExams()
        exams.value = (res.data || []).filter((exam) => exam.courseId === courseId.value)
      } else {
        // 未加入课程时仅提供考试预览信息，不允许参加
        const res = await examApi.getPublishedExams()
        exams.value = (res.data || [])
          .filter((exam) => exam.courseId === courseId.value)
          .map((exam) => ({ ...exam, studentExamStatus: 'NOT_STARTED' }))
      }
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
    coverUrl: course.value.coverUrl || '',
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
          coverUrl: courseForm.coverUrl || undefined,
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
    if (!canTakeCourseExam(exam)) {
      if (!isJoined.value) {
        ElMessage.warning('请先加入课程后再参加考试')
      } else {
        ElMessage.warning('当前考试不可参加')
      }
      return
    }
    router.push(`/exam/${exam.id}/take`)
  } else {
    router.push(`/exam/${exam.id}`)
  }
}

function canTakeCourseExam(exam: Exam): boolean {
  if (!isStudent.value) return false
  if (!isJoined.value) return false
  if (course.value?.status !== 'ACTIVE') return false
  if (exam.studentExamStatus === 'SUBMITTED' || exam.studentExamStatus === 'GRADED') return false
  if (exam.status !== 'PUBLISHED' && exam.status !== 'STARTED') return false

  const now = Date.now()
  const start = new Date(exam.startedAt).getTime()
  const end = new Date(exam.endedAt).getTime()
  return now >= start && now <= end
}

function getStudentExamActionText(exam: Exam): string {
  if (!isJoined.value) return '仅预览'
  if (course.value?.status !== 'ACTIVE') return '课程已结束'
  if (exam.studentExamStatus === 'SUBMITTED') return '已提交'
  if (exam.studentExamStatus === 'GRADED') return '已完成'
  if (exam.studentExamStatus === 'IN_PROGRESS') return '继续考试'
  if (exam.status === 'CANCELLED') return '已取消'
  if (exam.status === 'ENDED') return '已结束'
  if (exam.status === 'DRAFT') return '未发布'

  const now = Date.now()
  const start = new Date(exam.startedAt).getTime()
  const end = new Date(exam.endedAt).getTime()
  if (now < start) return '未开始'
  if (now > end) return '已结束'
  return '参加考试'
}

onMounted(() => {
  loadCourse()
  checkJoined()
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

watchEffect(() => {
  if (!course.value) return
  // 学生加入/退出课程后需要刷新考试数据，避免仍显示旧状态
  if (isStudent.value) {
    void isJoined.value
  }
  loadExams()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;
@use '@/styles/views/base-list.scss';
@use '@/styles/views/base-detail.scss';

.course-detail {
  .course-cover {
    width: 100%;
    max-height: 260px;
    border-radius: $radius-md;
    overflow: hidden;
    border: 1px solid $border-light;
    margin-bottom: $spacing-lg;

    img {
      width: 100%;
      max-height: 260px;
      object-fit: cover;
      display: block;
    }
  }

  .actions {
    margin-top: $spacing-xl;
    display: flex;
    justify-content: center;
    gap: $spacing-sm;
  }

  .full-width {
    width: 100%;
  }

  .exams-card {
    margin-top: $spacing-xl;
    border: none;
    border-radius: 0;
    background: transparent;

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

  .members-inline {
    width: 100%;

    .members-inline-header {
      display: flex;
      justify-content: flex-end;
      margin-bottom: $spacing-xs;

      .member-count {
        color: $text-tertiary;
        font-size: $font-size-sm;
      }
    }

    .members-inline-list {
      max-height: 148px;
      overflow-y: auto;
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      row-gap: $spacing-xs;
      column-gap: $spacing-lg;
      align-content: start;
      padding-right: $spacing-xs;
    }

    .member-inline-item {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      min-width: 0;
      padding: 2px 0;
    }

    .member-inline-name {
      color: $text-primary;
      font-size: $font-size-sm;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  @media (max-width: $breakpoint-sm) {
    .members-inline {
      .members-inline-list {
        grid-template-columns: 1fr;
      }
    }
  }

  .detail-table {
    width: 100%;

    :deep(.el-table__inner-wrapper),
    :deep(.el-table__header),
    :deep(.el-table__body) {
      width: 100% !important;
    }

    :deep(.el-table__header-wrapper),
    :deep(.el-table__body-wrapper) {
      width: 100%;
    }
  }
}
</style>
