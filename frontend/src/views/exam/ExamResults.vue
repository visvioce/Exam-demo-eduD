<template>
  <div class="exam-results">
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="text-large font-600 mr-3">{{ exam?.title || '考试结果' }}</span>
        </template>
      </el-page-header>
    </div>

    <!-- 教师视图：查看所有学生的考试结果 -->
    <template v-if="!isStudent">
      <el-card v-loading="loading">
        <template #header>
          <div class="card-header">
            <span>学生考试记录</span>
            <div class="header-actions">
              <el-button type="warning" @click="handleAutoGrade" :loading="autoGrading">
                自动阅卷
              </el-button>
              <el-button type="primary" @click="openBatchGradeDialog" :disabled="!canBatchGrade">
                阅卷
              </el-button>
              <el-button @click="loadSessions">刷新</el-button>
            </div>
          </div>
        </template>

        <el-table :data="sessions" stripe table-layout="auto" :fit="true">
          <el-table-column prop="id" label="记录ID" width="80" />
          <el-table-column label="学生" min-width="120">
            <template #default="{ row }">
              {{ getStudentName(row.studentId) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startedAt" label="开始时间" min-width="168">
            <template #default="{ row }">
              {{ formatDate(row.startedAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="submittedAt" label="提交时间" min-width="168">
            <template #default="{ row }">
              {{ formatDate(row.submittedAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" min-width="86">
            <template #default="{ row }">
              <span v-if="row.score !== null">{{ row.score }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="totalScore" label="总分" min-width="86" />
          <el-table-column prop="gradingStatus" label="评分状态" min-width="110">
            <template #default="{ row }">
              <el-tag :type="getGradingStatusColor(row.gradingStatus)">
                {{ getGradingStatusName(row.gradingStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="170">
            <template #default="{ row }">
              <ActionButtons
                :show-edit="false"
                :show-delete="false"
                @view="handleViewDetail(row)"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 批量阅卷对话框 -->
      <el-dialog v-model="gradeDialogVisible" title="批量阅卷" width="1100px">
        <el-table :data="batchGradeRows" v-loading="gradingLoading" table-layout="auto" :fit="true">
          <el-table-column label="学生" min-width="130">
            <template #default="{ row }">
              {{ row.studentName }}
            </template>
          </el-table-column>
          <el-table-column prop="questionContent" label="题目内容" min-width="280" show-overflow-tooltip />
          <el-table-column label="学生答案" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              {{ formatAnswer(row.studentAnswer) }}
            </template>
          </el-table-column>
          <el-table-column label="给分" min-width="140">
            <template #default="{ row }">
              <el-input-number v-model="row.score" :min="0" :max="row.maxScore" />
              <span class="score-max">/ {{ row.maxScore }}</span>
            </template>
          </el-table-column>
          <el-table-column label="评语" min-width="180">
            <template #default="{ row }">
              <el-input v-model="row.comment" placeholder="评语（可选）" />
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="!gradingLoading && batchGradeRows.length === 0" description="当前考试没有待阅卷的简答题" />

        <div class="submit-grades">
          <el-button type="primary" @click="handleSubmitBatchGrades" :loading="submittingGrades" :disabled="batchGradeRows.length === 0">
            提交批量评分
          </el-button>
        </div>
      </el-dialog>

      <!-- 详情对话框 -->
      <el-dialog v-model="detailDialogVisible" title="考试详情" width="900px">
        <el-table :data="detailResult?.answers || []" v-loading="detailLoading" stripe table-layout="auto" :fit="true">
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column label="题目内容" min-width="320">
            <template #default="{ row }">
              {{ row.questionContent || '未知题目' }}
            </template>
          </el-table-column>
          <el-table-column label="学生答案" min-width="200">
            <template #default="{ row }">
              {{ formatAnswer(row.answer) }}
            </template>
          </el-table-column>
          <el-table-column label="得分" min-width="84">
            <template #default="{ row }">
              {{ row.score ?? '-' }}
            </template>
          </el-table-column>
          <el-table-column label="满分" prop="maxScore" min-width="84" />
          <el-table-column label="评语" min-width="180">
            <template #default="{ row }">
              {{ row.teacherComment || '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>
    </template>

    <!-- 学生视图：查看自己的考试结果 -->
    <template v-else>
      <el-card v-loading="loading">
        <template #header>
          <span>我的考试结果</span>
        </template>

        <template v-if="mySession">
          <el-result :icon="getExamStatusIcon(mySession.score, mySession.totalScore, mySession.gradingStatus)">
            <template #title>
              <div>
                <span class="score-main">
                  {{ mySession.score !== null ? mySession.score : '0' }} / {{ mySession.totalScore }}
                </span>
                <span class="score-status">
                  <el-tag :type="getPassOrFail(mySession.score ?? 0, mySession.totalScore, mySession.gradingStatus)">
                    {{ getGradeStatusText(mySession.score ?? 0, mySession.totalScore, mySession.gradingStatus) }}
                  </el-tag>
                </span>
              </div>
              <div v-if="mySession.gradingStatus === 'PENDING'" class="pending-tip">
                <el-icon class="is-loading"><Loading /></el-icon>
                主观题评分中，最终分数待定
              </div>
            </template>
            <template #sub-title>
              <p>提交时间: {{ formatDate(mySession.submittedAt) }}</p>
            </template>
          </el-result>

          <el-divider />

          <h4>答题详情</h4>
          <el-table :data="examResult?.answers || []" stripe table-layout="auto" :fit="true">
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column label="题目内容" min-width="300">
              <template #default="{ row }">
                {{ row.questionContent || '未知题目' }}
              </template>
            </el-table-column>
            <el-table-column label="学生答案" min-width="200">
              <template #default="{ row }">
                {{ formatAnswer(row.answer) }}
              </template>
            </el-table-column>
            <el-table-column label="评语" min-width="200">
              <template #default="{ row }">
                {{ row.teacherComment || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="得分" min-width="84">
              <template #default="{ row }">
                <span v-if="row.score !== null">{{ row.score }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="row.isCorrect ? 'success' : 'danger'" v-if="row.isCorrect !== undefined">
                  {{ row.isCorrect ? '正确' : '错误' }}
                </el-tag>
                <el-tag type="warning" v-else>待评分</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </template>

        <el-empty v-else description="暂无考试记录" />
      </el-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { examApi, examSessionApi } from '@/api/exam'
import { courseApi } from '@/api/course'
import { paperApi } from '@/api/paper'
import { questionApi } from '@/api/question'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { formatDate, getSessionStatusName as getStatusName, getSessionStatusColor as getStatusColor, getGradingStatusName, getGradingStatusColor } from '@/utils/format'
import { getErrorMessage } from '@/utils/error'
import type { Exam, ExamSession, Question, Paper, ExamResultResponse, User } from '@/types'
import ActionButtons from '@/components/ActionButtons.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const gradingLoading = ref(false)
const submittingGrades = ref(false)
const autoGrading = ref(false)
const gradeDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const detailLoading = ref(false)

const exam = ref<Exam | null>(null)
const paper = ref<Paper | null>(null)
const sessions = ref<ExamSession[]>([])
const mySession = ref<ExamSession | null>(null)
const examResult = ref<ExamResultResponse | null>(null)
const detailResult = ref<ExamResultResponse | null>(null)
const questions = ref<Question[]>([])
const studentNameMap = ref<Record<number, string>>({})

const examId = computed(() => Number(route.params.id))
const isStudent = computed(() => authStore.user?.role === 'STUDENT')

// 批量阅卷题目（仅简答题）
type BatchGradeRow = {
  examSessionId: number
  studentId: number
  studentName: string
  questionId: number
  questionContent: string
  studentAnswer: string | string[] | null
  score: number
  maxScore: number
  comment: string
}
const batchGradeRows = ref<BatchGradeRow[]>([])
const canBatchGrade = computed(() => sessions.value.some((session) => session.status === 'SUBMITTED'))

function goBack() {
  router.back()
}

function getExamStatusIcon(score: number | null | undefined, _totalScore: number, gradingStatus?: string) {
  // 如果正在评分中，显示warning图标
  if (gradingStatus === 'PENDING') return 'warning'
  if (score === null || score === undefined) return 'warning'

  // 使用考试的及格分数，而不是硬编码60
  const passScore = exam.value?.passScore || 60
  return score >= passScore ? 'success' : 'error'
}

function getPassOrFail(score: number | null | undefined, _totalScore: number, gradingStatus?: string) {
  // 如果正在评分中，显示warning
  if (gradingStatus === 'PENDING') return 'warning'
  if (score === null || score === undefined) return 'warning'

  // 使用考试的及格分数，而不是硬编码60%
  const passScore = exam.value?.passScore || 60
  return score >= passScore ? 'success' : 'danger'
}

function getGradeStatusText(score: number | null | undefined, totalScore: number, gradingStatus?: string): string {
  // 如果正在评分中，显示"评分中"
  if (gradingStatus === 'PENDING') return '评分中'

  // 如果已经完成评分
  if (gradingStatus === 'COMPLETED') {
    const percentage = (score ?? 0) / totalScore * 100
    return percentage >= (exam.value?.passScore || 60) ? '及格' : '不及格'
  }

  return '未知'
}

function getQuestionContent(questionId: number): string {
  const q = questions.value.find(item => item.id === questionId)
  return q ? q.content : '未知题目'
}

function getStudentName(studentId: number): string {
  return studentNameMap.value[studentId] || `学生#${studentId}`
}

function formatAnswer(answer: unknown): string {
  if (answer === null || answer === undefined) return '-'
  if (Array.isArray(answer)) return answer.join(', ')
  return String(answer)
}

async function loadExam() {
  loading.value = true
  try {
    const res = await examApi.getById(examId.value)
    exam.value = res.data

    // 加载试卷信息（用于获取题目分数）
    if (exam.value?.paperId) {
      const paperRes = await paperApi.getById(exam.value.paperId)
      paper.value = paperRes.data
    }

  } catch (error) {
    ElMessage.error('加载考试失败')
  } finally {
    loading.value = false
  }
}

async function loadStudentNames(courseId: number) {
  try {
    const res = await courseApi.getMembers(courseId)
    const members = (res.data || []) as User[]
    const map: Record<number, string> = {}
    members.forEach((member) => {
      map[member.id] = member.nickname || member.username || `学生#${member.id}`
    })
    studentNameMap.value = map
  } catch {
    // 忽略成员查询失败，使用 ID 兜底显示
  }
}

async function loadSessions() {
  if (isStudent.value) {
    try {
      const res = await examSessionApi.getMySessions()
      const sessionIdFromQuery = Number(route.query.sessionId || 0)
      const targetSession = sessionIdFromQuery
        ? res.data.find((s: ExamSession) => s.id === sessionIdFromQuery)
        : res.data.find((s: ExamSession) => s.examId === examId.value)

      if (targetSession) {
        mySession.value = targetSession
        const resultRes = await examSessionApi.getExamResult(targetSession.id)
        examResult.value = resultRes.data
      }
    } catch (error) {
      ElMessage.error('加载考试记录失败')
    }
  } else {
    try {
      const res = await examSessionApi.getByExamId(examId.value)
      sessions.value = res.data || []
      const map: Record<number, string> = {}
      sessions.value.forEach((session) => {
        if (session.studentName && session.studentName.trim()) {
          map[session.studentId] = session.studentName
        }
      })
      if (Object.keys(map).length > 0) {
        studentNameMap.value = map
      } else if (exam.value?.courseId) {
        await loadStudentNames(exam.value.courseId)
      }
    } catch (error) {
      ElMessage.error('加载考试记录失败')
    }
  }
}

async function loadQuestions() {
  try {
    const res = await questionApi.list()
    questions.value = res.data
  } catch (error) {
    ElMessage.error('加载题目失败')
  }
}

async function handleViewDetail(row: ExamSession) {
  detailDialogVisible.value = true
  detailLoading.value = true
  try {
    const res = await examSessionApi.getExamResult(row.id)
    detailResult.value = res.data
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, '加载详情失败'))
  } finally {
    detailLoading.value = false
  }
}

async function openBatchGradeDialog() {
  gradeDialogVisible.value = true
  await loadBatchGradeRows()
}

async function loadBatchGradeRows() {
  gradingLoading.value = true
  try {
    await loadQuestions()

    const submittedSessions = sessions.value.filter((session) => session.status === 'SUBMITTED')
    if (submittedSessions.length === 0) {
      batchGradeRows.value = []
      return
    }

    const resultList = await Promise.allSettled(
      submittedSessions.map((session) => examSessionApi.getExamResult(session.id))
    )

    const rows: BatchGradeRow[] = []
    let failedCount = 0

    resultList.forEach((result, index) => {
      const session = submittedSessions[index]
      if (!session) {
        failedCount++
        return
      }
      if (result.status !== 'fulfilled') {
        failedCount++
        return
      }

      const answerRows = (result.value.data.answers || [])
        .filter((answer) => answer.questionType === 'ESSAY')
        .filter((answer) => answer.gradingStatus === 'PENDING' || answer.score === null || answer.score === undefined)
        .map((answer) => ({
          examSessionId: session.id,
          studentId: session.studentId,
          studentName: getStudentName(session.studentId),
          questionId: answer.questionId,
          questionContent: answer.questionContent || getQuestionContent(answer.questionId),
          studentAnswer: answer.answer,
          score: answer.score ?? 0,
          maxScore: answer.maxScore || getQuestionMaxScore(answer.questionId),
          comment: answer.teacherComment || ''
        }))

      rows.push(...answerRows)
    })

    batchGradeRows.value = rows
    if (failedCount > 0) {
      ElMessage.warning(`有 ${failedCount} 条考试记录加载失败，请刷新后重试`)
    }
  } catch (error) {
    ElMessage.error('加载题目失败')
  } finally {
    gradingLoading.value = false
  }
}

function getQuestionMaxScore(questionId: number): number {
  // 从试卷中获取题目分数
  if (paper.value?.questions) {
    const paperQuestion = paper.value.questions.find(q => q.questionId === questionId)
    if (paperQuestion) {
      return paperQuestion.score
    }
  }
  // 默认返回10分
  return 10
}

async function handleSubmitBatchGrades() {
  if (batchGradeRows.value.length === 0) {
    ElMessage.warning('当前没有可提交的简答题评分')
    return
  }

  submittingGrades.value = true
  try {
    const sessionGradesMap = new Map<number, { questionId: number; score: number; comment?: string }[]>()
    batchGradeRows.value.forEach((row) => {
      const grades = sessionGradesMap.get(row.examSessionId) || []
      grades.push({
        questionId: row.questionId,
        score: row.score,
        comment: row.comment
      })
      sessionGradesMap.set(row.examSessionId, grades)
    })

    let successCount = 0
    let failCount = 0
    for (const [examSessionId, grades] of sessionGradesMap) {
      try {
        await examSessionApi.gradeSubjectiveAnswers({
          examSessionId,
          grades
        })
        successCount++
      } catch {
        failCount++
      }
    }

    if (successCount > 0) {
      ElMessage.success(`批量阅卷完成：成功 ${successCount} 份${failCount > 0 ? `，失败 ${failCount} 份` : ''}`)
      await loadSessions()
      if (failCount === 0) {
        gradeDialogVisible.value = false
      } else {
        await loadBatchGradeRows()
      }
    } else {
      ElMessage.error('批量阅卷失败，请重试')
    }
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, '评分失败'))
  } finally {
    submittingGrades.value = false
  }
}

async function handleAutoGrade() {
  autoGrading.value = true
  try {
    const res = await examSessionApi.autoGradeByExam(examId.value)
    const processed = res.data ?? 0
    if (processed > 0) {
      ElMessage.success(`自动阅卷完成，共处理 ${processed} 份答卷`)
    } else {
      ElMessage.warning('没有可自动阅卷的已提交答卷')
    }
    await loadSessions()
    if (gradeDialogVisible.value) {
      await loadBatchGradeRows()
    }
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, '自动阅卷失败'))
  } finally {
    autoGrading.value = false
  }
}

onMounted(() => {
  loadExam()
  loadSessions()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.exam-results {
  padding: $spacing-xl;

  .page-header {
    margin-bottom: $spacing-xl;

    h2 {
      font-size: $font-size-3xl;
      font-weight: $font-weight-medium;
      color: $text-primary;
      letter-spacing: -0.5px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  .section-title {
    margin-top: $spacing-xl;
  }

  .submit-grades {
    margin-top: $spacing-xl;
    text-align: right;
  }

  .score-max {
    margin-left: $spacing-xs;
    color: $text-tertiary;
    font-size: $font-size-sm;
  }

  .score-main {
    font-size: 24px;
    font-weight: $font-weight-medium;
  }

  .score-status {
    margin-left: $spacing-lg;
  }

  .pending-tip {
    margin-top: $spacing-sm;
    font-size: $font-size-sm;
    color: $text-tertiary;
  }
}

// 响应式
@media (max-width: $breakpoint-md) {
  .exam-results {
    padding: $spacing-md;

    .page-header {
      h2 {
        font-size: $font-size-2xl;
      }
    }
  }
}
</style>
