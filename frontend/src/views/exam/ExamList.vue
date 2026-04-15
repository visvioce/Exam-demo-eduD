<template>
  <div class="exam-list base-list-page">
    <div class="page-header">
      <h2>{{ isStudent ? '我的考试' : '考试管理' }}</h2>
      <el-button type="primary" @click="handleCreate" v-if="hasPermission(['ADMIN', 'TEACHER'])">
        <el-icon><Plus /></el-icon>
        创建考试
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px">
        <el-form-item label="课程">
          <el-select v-model="searchForm.courseId" placeholder="全部" clearable @change="handleCourseChange" class="search-control">
            <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <div class="filter-tabs">
            <button
              type="button"
              v-for="item in (isStudent ? studentStatusOptions : statusOptions)" 
              :key="item.value"
              :class="['tab-item', { active: searchForm.status === item.value }]"
              :aria-pressed="searchForm.status === item.value"
              @click="handleStatusChange(item.value)"
            >
              {{ item.label }}
            </button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 学生视图：我的考试 -->
    <template v-if="isStudent">
      <el-card class="table-card">
        <template #header>
          <span>我的考试</span>
        </template>
        <el-table :data="filteredMyExams" v-loading="loading" stripe table-layout="auto" :fit="true">
          <el-table-column prop="title" label="考试名称" min-width="200" />
          <el-table-column prop="courseName" label="课程" min-width="120" />
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
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button size="small" type="primary" @click="handleTakeExam(row)"
                  :disabled="!canTakeExam(row)">
                  {{ getExamActionText(row) }}
                </el-button>
                <el-button
                  v-if="canViewResult(row)"
                  size="small"
                  @click="handleViewResult(row)"
                >
                  查看回顾
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>

    <!-- 教师/管理员视图：考试列表 -->
    <template v-else>
      <el-card class="table-card">
        <el-table :data="exams" v-loading="loading" stripe table-layout="auto" :fit="true">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="考试名称" min-width="200" />
          <el-table-column prop="courseName" label="课程" min-width="120" />
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
          <el-table-column prop="totalScore" label="总分" min-width="88" />
          <el-table-column prop="status" label="状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <ActionButtons
                  :show-edit="canEdit(row) && (!row.status || row.status === 'DRAFT')"
                  :show-delete="canEdit(row) && (!row.status || row.status === 'DRAFT')"
                  @view="handleView(row)"
                  @edit="handleEdit(row)"
                  @delete="handleDelete(row)"
                />
                <el-button size="small" type="success" @click="handlePublish(row)"
                  v-if="canEdit(row) && (!row.status || row.status === 'DRAFT')">发布</el-button>
                <el-button size="small" type="warning" @click="handleCancel(row)"
                  v-if="canEdit(row) && ['PUBLISHED', 'STARTED'].includes(row.status)">取消</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadExams"
            @current-change="loadExams"
          />
        </div>
      </el-card>
    </template>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑考试' : '创建考试'" width="700px" class="base-dialog">
      <el-form :model="examForm" :rules="rules" ref="examFormRef" label-width="100px">
        <el-form-item label="考试名称" prop="title">
          <el-input v-model="examForm.title" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="examForm.courseId" placeholder="请选择课程" class="full-width">
            <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="试卷" prop="paperId">
          <el-select v-model="examForm.paperId" placeholder="请选择试卷" class="full-width">
            <el-option v-for="paper in papers" :key="paper.id" :label="paper.name" :value="paper.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startedAt">
              <el-date-picker v-model="examForm.startedAt" type="datetime" placeholder="选择开始时间" class="full-width" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endedAt">
              <el-date-picker v-model="examForm.endedAt" type="datetime" placeholder="选择结束时间" class="full-width" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考试时长" prop="duration">
              <el-input-number v-model="examForm.duration" :min="1" :max="300" />
              <span class="unit-label">分钟</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="及格分" prop="passScore">
              <el-input-number v-model="examForm.passScore" :min="0" :max="examForm.totalScore" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="考试说明">
          <el-input v-model="examForm.description" type="textarea" :rows="3" placeholder="请输入考试说明（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 考试详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="考试详情" width="700px" class="base-dialog">
      <el-descriptions :column="2" border v-if="currentExam">
        <el-descriptions-item label="考试名称">{{ currentExam.title }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusColor(currentExam.status)">{{ getStatusName(currentExam.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDate(currentExam.startedAt) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDate(currentExam.endedAt) }}</el-descriptions-item>
        <el-descriptions-item label="考试时长">{{ currentExam.duration }}分钟</el-descriptions-item>
        <el-descriptions-item label="总分/及格分">{{ currentExam.totalScore }} / {{ currentExam.passScore }}</el-descriptions-item>
        <el-descriptions-item label="考试说明" :span="2">{{ currentExam.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="results-action">
        <el-button type="primary" @click="handleViewResults(currentExam!)">查看考试记录</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { examApi } from '@/api/exam'
import { courseApi } from '@/api/course'
import { paperApi } from '@/api/paper'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getStatusName, getStatusColor, formatDate } from '@/utils/format'
import { getErrorMessage } from '@/utils/error'
import { useListPage } from '@/composables/useListPage'
import type { FormInstance, FormRules } from 'element-plus'
import type { Exam, Course, Paper } from '@/types'
import ActionButtons from '@/components/ActionButtons.vue'

const router = useRouter()
const authStore = useAuthStore()

const submitting = ref(false)
const myExams = ref<Exam[]>([])
const courses = ref<Course[]>([])
const papers = ref<Paper[]>([])
const editDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const isEdit = ref(false)
const examFormRef = ref<FormInstance>()
const currentExam = ref<Exam | null>(null)

const statusOptions = [
  { label: '全部', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '进行中', value: 'STARTED' },
  { label: '已结束', value: 'ENDED' },
  { label: '已取消', value: 'CANCELLED' }
]

const studentStatusOptions = [
  { label: '全部', value: '' },
  { label: '未开始', value: 'PUBLISHED' },
  { label: '进行中', value: 'STARTED' },
  { label: '已结束', value: 'ENDED' },
  { label: '已取消', value: 'CANCELLED' }
]

const isStudent = computed(() => authStore.user?.role === 'STUDENT')
const myExamsLoading = ref(false)

const searchForm = reactive({
  courseId: null as number | null,
  status: ''
})

const {
  data: exams,
  loading: teacherLoading,
  total,
  pagination,
  loadData: loadTeacherExams,
  reset
} = useListPage<Exam>({
  fetchFn: (params) => examApi.page({
    ...params,
    courseId: searchForm.courseId || undefined,
    status: searchForm.status || undefined
  }),
  immediate: false
})

const loading = computed(() => (isStudent.value ? myExamsLoading.value : teacherLoading.value))

const examForm = reactive({
  id: 0,
  title: '',
  description: '',
  courseId: null as number | null,
  paperId: null as number | null,
  startedAt: '',
  endedAt: '',
  duration: 60,
  totalScore: 100,
  passScore: 60
})

const validatePassScore = (_rule: any, value: number, callback: any) => {
  if (examForm.totalScore && value > examForm.totalScore) {
    callback(new Error('及格分不能大于总分'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  title: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  paperId: [{ required: true, message: '请选择试卷', trigger: 'change' }],
  startedAt: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endedAt: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  duration: [
    { required: true, message: '请输入考试时长', trigger: 'blur' },
    { type: 'number', min: 1, message: '考试时长必须大于0', trigger: 'blur' }
  ],
  totalScore: [
    { required: true, message: '请输入总分', trigger: 'blur' },
    { type: 'number', min: 1, message: '总分必须大于0', trigger: 'blur' }
  ],
  passScore: [
    { required: true, message: '请输入及格分数', trigger: 'blur' },
    { type: 'number', min: 0, message: '及格分不能为负数', trigger: 'blur' },
    { validator: validatePassScore, trigger: 'blur' }
  ]
})

// 学生视图：筛选后的考试列表
const filteredMyExams = computed(() => {
  let result = [...myExams.value]

  // 按课程筛选
  if (searchForm.courseId) {
    result = result.filter(exam => exam.courseId === searchForm.courseId)
  }

  // 按状态筛选
  if (searchForm.status) {
    result = result.filter(exam => exam.status === searchForm.status)
  }

  return result
})

function hasPermission(roles: string[]) {
  return roles.includes(authStore.user?.role || '')
}

function canEdit(exam: Exam) {
  const role = authStore.user?.role
  // 管理员可以操作所有考试（与后端 checkOwnership 逻辑一致）
  if (role === 'ADMIN') return true
  // 教师只能操作自己创建的考试
  return role === 'TEACHER' && exam.teacherId === authStore.user?.id
}

function canTakeExam(exam: Exam) {
  // 如果学生已经提交或已完成考试，则不能再次参加
  if (exam.studentExamStatus === 'SUBMITTED' || exam.studentExamStatus === 'GRADED') {
    return false
  }
  if (exam.status !== 'PUBLISHED' && exam.status !== 'STARTED') return false
  
  // 如果学生有进行中的考试会话，允许继续（与后端30秒宽限时间一致）
  if (exam.studentExamStatus === 'IN_PROGRESS') {
    const now = new Date().getTime()
    const end = new Date(exam.endedAt).getTime()
    // 给予30秒宽限时间
    return now <= end + 30000
  }
  
  const now = new Date().getTime()
  const start = new Date(exam.startedAt).getTime()
  const end = new Date(exam.endedAt).getTime()
  return now >= start && now <= end
}

function getExamActionText(exam: Exam) {
  // 优先显示学生考试状态
  if (exam.studentExamStatus === 'SUBMITTED') return '已提交'
  if (exam.studentExamStatus === 'GRADED') return '已完成'
  if (exam.studentExamStatus === 'IN_PROGRESS') return '继续考试'

  if (exam.status === 'CANCELLED') return '已取消'
  if (exam.status === 'ENDED') return '已结束'
  if (exam.status === 'DRAFT') return '未发布'
  const now = new Date().getTime()
  const start = new Date(exam.startedAt).getTime()
  const end = new Date(exam.endedAt).getTime()
  if (now < start) return '未开始'
  if (now > end) return '已结束'
  return '参加考试'
}

function canViewResult(exam: Exam) {
  // 只有学生已经提交或已完成考试，才能查看回顾
  // 未参加考试的学生不能查看回顾（即使考试已结束）
  return exam.studentExamStatus === 'SUBMITTED' ||
         exam.studentExamStatus === 'GRADED'
}

function handleViewResult(exam: Exam) {
  // 跳转到考试回顾页面
  router.push(`/exam/${exam.id}/review`)
}

async function loadExams() {
  if (isStudent.value) {
    myExamsLoading.value = true
    try {
      const res = await examApi.getMyExams()
      myExams.value = res.data
    } catch (error) {
      ElMessage.error('加载考试失败')
    } finally {
      myExamsLoading.value = false
    }
    return
  }

  await loadTeacherExams()
}

async function loadCourses() {
  try {
    let res
    if (hasPermission(['ADMIN', 'TEACHER'])) {
      // 管理员和教师查看自己管理的课程
      res = await courseApi.list()
    } else {
      // 学生查看已加入的课程（用于筛选）
      res = await courseApi.getMyCourses()
    }
    courses.value = res.data || []
    if (searchForm.courseId && !courses.value.some((course) => course.id === searchForm.courseId)) {
      searchForm.courseId = null
    }
  } catch (error) {
    ElMessage.error('加载课程失败')
  }
}

async function loadPapers() {
  // 学生不需要加载试卷（不创建考试）
  if (isStudent.value) return

  try {
    const res = await paperApi.list()
    papers.value = res.data
  } catch (error) {
    ElMessage.error('加载试卷失败')
  }
}

function handleCourseChange(value: number | null) {
  void value
  if (isStudent.value) return
  void reset()
}

function handleStatusChange(value: string) {
  searchForm.status = searchForm.status === value ? '' : value
  if (isStudent.value) return
  void reset()
}

function handleReset() {
  if (isStudent.value) {
    searchForm.courseId = null
    searchForm.status = ''
    return
  }
  searchForm.courseId = null
  searchForm.status = ''
  void reset()
}

function handleView(row: Exam) {
  currentExam.value = row
  viewDialogVisible.value = true
}

function handleCreate() {
  isEdit.value = false
  Object.assign(examForm, {
    id: 0,
    title: '',
    description: '',
    courseId: null,
    paperId: null,
    startedAt: '',
    endedAt: '',
    duration: 60,
    totalScore: 100,
    passScore: 60
  })
  editDialogVisible.value = true
}

function handleEdit(row: Exam) {
  isEdit.value = true
  Object.assign(examForm, {
    id: row.id,
    title: row.title,
    description: row.description,
    courseId: row.courseId,
    paperId: row.paperId,
    startedAt: row.startedAt,
    endedAt: row.endedAt,
    duration: row.duration,
    totalScore: row.totalScore,
    passScore: row.passScore
  })
  editDialogVisible.value = true
}

async function handlePublish(row: Exam) {
  try {
    await ElMessageBox.confirm('确定要发布该考试吗？发布后学生可以参加考试。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await examApi.publish(row.id)
    ElMessage.success('发布成功')
    void loadExams()
  } catch {
    // 取消
  }
}

async function handleCancel(row: Exam) {
  try {
    await ElMessageBox.confirm('确定要取消该考试吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await examApi.cancel(row.id)
    ElMessage.success('取消成功')
    void loadExams()
  } catch {
    // 取消
  }
}

async function handleDelete(row: Exam) {
  try {
    await ElMessageBox.confirm('确定要删除该考试吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await examApi.delete(row.id)
    ElMessage.success('删除成功')
    void loadExams()
  } catch {
    // 取消
  }
}

async function handleTakeExam(row: Exam) {
  try {
    // 先调用 start API 创建考试会话
    const res = await examApi.start(row.id)
    const sessionId = res.data.id
    // 带着 sessionId 跳转到考试页面
    router.push(`/exam/${row.id}/take?sessionId=${sessionId}`)
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, '开始考试失败'))
  }
}

function handleViewResults(row: Exam) {
  router.push(`/exam/${row.id}/results`)
}

async function handleSubmit() {
  if (!examFormRef.value) return

  await examFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = {
          title: examForm.title,
          description: examForm.description,
          courseId: examForm.courseId || undefined,
          paperId: examForm.paperId || undefined,
          startedAt: examForm.startedAt,
          endedAt: examForm.endedAt,
          duration: examForm.duration,
          totalScore: examForm.totalScore,
          passScore: examForm.passScore
        }

        if (isEdit.value) {
          await examApi.update(examForm.id, data)
          ElMessage.success('更新成功')
        } else {
          await examApi.create(data)
          ElMessage.success('创建成功，请在列表点击“发布”后学生才可见')
        }
        editDialogVisible.value = false
        void loadExams()
      } catch (error: unknown) {
        ElMessage.error(getErrorMessage(error, '操作失败'))
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  void loadExams()
  loadCourses()
  loadPapers()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;
@use '@/styles/views/base-list.scss';

.exam-list {
  .full-width {
    width: 100%;
  }

  .unit-label {
    margin-left: $spacing-sm;
  }

  .results-action {
    margin-top: $spacing-xl;
    text-align: right;
  }

  .table-actions {
    display: inline-flex;
    align-items: center;
    gap: $spacing-sm;
    flex-wrap: wrap;
  }

}
</style>
