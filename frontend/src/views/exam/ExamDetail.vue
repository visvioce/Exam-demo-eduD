<template>
  <div class="exam-detail">
    <div class="page-header">
      <el-page-header @back="goBack">
        <template #content>
          <span class="text-large font-600 mr-3">{{ exam?.title || '考试详情' }}</span>
        </template>
      </el-page-header>
    </div>

    <el-card v-loading="loading">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="考试名称">{{ exam?.title }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusColor(exam?.status)">{{ getStatusName(exam?.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDate(exam?.startedAt) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDate(exam?.endedAt) }}</el-descriptions-item>
        <el-descriptions-item label="考试时长">{{ exam?.duration }}分钟</el-descriptions-item>
        <el-descriptions-item label="总分/及格分">{{ exam?.totalScore }} / {{ exam?.passScore }}</el-descriptions-item>
        <el-descriptions-item label="考试说明" :span="2">{{ exam?.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 试卷信息 -->
      <h4 class="section-title">试卷信息</h4>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="试卷名称">{{ paper?.name || '考试题目' }}</el-descriptions-item>
        <el-descriptions-item label="试卷类型">{{ paper ? getTypeName(paper.type) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="考试时长">{{ exam?.duration ? exam.duration + '分钟' : '不限时' }}</el-descriptions-item>
        <el-descriptions-item label="题目数量">{{ displayQuestionRows.length }}道</el-descriptions-item>
      </el-descriptions>

      <!-- 题目列表 -->
      <h4 class="section-title">题目列表</h4>
      <el-table :data="displayQuestionRows" border v-if="displayQuestionRows.length">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="题目内容" min-width="300">
          <template #default="{ row }">
            <div>{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column label="题型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)" size="small">
              {{ getTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" />
      </el-table>

      <!-- 操作按钮 -->
      <div class="actions" v-if="canTakeExam">
        <el-button type="primary" size="large" @click="showInstructions">
          开始考试
        </el-button>
      </div>
    </el-card>

    <!-- 考试须知对话框 -->
    <el-dialog
      v-model="instructionsDialogVisible"
      title="考试须知"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="instructions-content">
        <el-alert
          type="warning"
          :closable="false"
          show-icon
          class="instructions-alert"
        >
          <template #title>
            <strong>请仔细阅读以下注意事项</strong>
          </template>
        </el-alert>

        <div class="instruction-item">
          <el-icon><Timer /></el-icon>
          <span>考试时长：{{ exam?.duration }} 分钟</span>
        </div>
        <div class="instruction-item">
          <el-icon><Document /></el-icon>
          <span>题目数量：{{ displayQuestionRows.length }} 道</span>
        </div>
        <div class="instruction-item">
          <el-icon><TrendCharts /></el-icon>
          <span>试卷总分：{{ exam?.totalScore }} 分</span>
        </div>
        <div class="instruction-item">
          <el-icon><Checked /></el-icon>
          <span>及格分数：{{ exam?.passScore }} 分</span>
        </div>

        <el-divider v-if="exam?.description" />

        <div v-if="exam?.description" class="exam-description">
          <h4>考试说明</h4>
          <p>{{ exam.description }}</p>
        </div>

        <el-divider />

        <div class="tips">
          <h4>温馨提示</h4>
          <ul>
            <li>系统会自动保存您的答案，请放心作答</li>
            <li>请勿刷新页面，以免影响答题进度</li>
            <li>考试时间到后将自动提交试卷</li>
            <li>请认真作答，诚信考试</li>
          </ul>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-checkbox v-model="instructionsConfirmed">我已阅读并理解以上内容</el-checkbox>
          <el-button
            type="primary"
            :disabled="!instructionsConfirmed"
            @click="confirmStartExam"
          >
            开始答题
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { examApi } from '@/api/exam'
import { paperApi } from '@/api/paper'
import { questionApi } from '@/api/question'
import { ElMessage } from 'element-plus'
import { formatDate, getStatusColor as formatStatusColor, getStatusName as formatStatusName } from '@/utils/format'
import { getErrorMessage } from '@/utils/error'
import { Timer, Document, TrendCharts, Checked } from '@element-plus/icons-vue'
import type { Exam, Paper, Question, QuestionForExam } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const exam = ref<Exam | null>(null)
const paper = ref<Paper | null>(null)
const questions = ref<Question[]>([])
const examQuestions = ref<QuestionForExam[]>([])

// 考试须知相关
const instructionsDialogVisible = ref(false)
const instructionsConfirmed = ref(false)

const examId = computed(() => Number(route.params.id))

const paperQuestions = computed(() => paper.value?.questions || [])
const isStudent = computed(() => authStore.user?.role === 'STUDENT')

const displayQuestionRows = computed(() => {
  if (isStudent.value) {
    return examQuestions.value.map((question) => ({
      id: question.id,
      content: question.content,
      type: question.type,
      score: Number(question.score ?? 0)
    }))
  }
  return paperQuestions.value.map((paperQuestion) => ({
    id: paperQuestion.questionId,
    content: getQuestionContent(paperQuestion.questionId),
    type: getQuestionType(paperQuestion.questionId),
    score: paperQuestion.score
  }))
})

const canTakeExam = computed(() => {
  if (!exam.value) return false
  if (!isStudent.value) return false
  if (exam.value.status !== 'PUBLISHED' && exam.value.status !== 'STARTED') return false
  const now = new Date().getTime()
  const start = new Date(exam.value.startedAt).getTime()
  const end = new Date(exam.value.endedAt).getTime()
  return now >= start && now <= end
})

function getStatusName(status?: string) {
  return status ? formatStatusName(status) : ''
}

function getStatusColor(status?: string) {
  return status ? formatStatusColor(status) : ''
}

function goBack() {
  router.back()
}

function getTypeName(type?: string) {
  if (!type) return ''
  const map: Record<string, string> = {
    SINGLE_CHOICE: '单选题',
    MULTIPLE_CHOICE: '多选题',
    TRUE_FALSE: '判断题',
    FILL_BLANK: '填空题',
    ESSAY: '简答题',
    MANUAL: '手动组卷',
    RANDOM: '随机组卷',
    MIXED: '混合组卷'
  }
  return map[type] || type
}

function getTypeColor(type?: string) {
  if (!type) return ''
  const map: Record<string, string> = {
    SINGLE_CHOICE: 'primary',
    MULTIPLE_CHOICE: 'success',
    TRUE_FALSE: 'warning',
    FILL_BLANK: 'info',
    ESSAY: 'danger'
  }
  return map[type] || ''
}

function getQuestionContent(questionId: number): string {
  const q = questions.value.find(item => item.id === questionId)
  return q ? q.content.substring(0, 100) + (q.content.length > 100 ? '...' : '') : '未知题目'
}

function getQuestionType(questionId: number): string {
  const q = questions.value.find(item => item.id === questionId)
  return q?.type || ''
}

async function loadExam() {
  loading.value = true
  try {
    const res = await examApi.getById(examId.value)
    exam.value = res.data

    if (isStudent.value) {
      await loadExamQuestions()
      return
    }

    if (exam.value?.paperId) {
      await loadPaper(exam.value.paperId)
    }
  } catch (error) {
    ElMessage.error('加载考试失败')
  } finally {
    loading.value = false
  }
}

async function loadExamQuestions() {
  try {
    const res = await examApi.getQuestions(examId.value)
    examQuestions.value = res.data || []
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, '加载题目失败'))
  }
}

async function loadPaper(paperId: number) {
  try {
    const res = await paperApi.getById(paperId)
    paper.value = res.data
    if (paper.value?.questions?.length) {
      await loadQuestions(paper.value.questions.map(q => q.questionId))
    }
  } catch (error) {
    ElMessage.error('加载试卷失败')
  }
}

async function loadQuestions(questionIds: number[]) {
  try {
    const res = await questionApi.list()
    questions.value = res.data.filter(q => questionIds.includes(q.id))
  } catch (error) {
    ElMessage.error('加载题目失败')
  }
}

// 显示考试须知对话框
function showInstructions() {
  instructionsConfirmed.value = false
  instructionsDialogVisible.value = true
}

// 确认开始考试
async function confirmStartExam() {
  if (!instructionsConfirmed.value) {
    ElMessage.warning('请先确认已阅读考试须知')
    return
  }

  instructionsDialogVisible.value = false
  await handleStartExam()
}

async function handleStartExam() {
  try {
    const res = await examApi.start(examId.value)
    // 跳转到考试页面
    router.push(`/exam/${examId.value}/take?sessionId=${res.data.id}`)
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, '开始考试失败'))
  }
}

onMounted(() => {
  loadExam()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.exam-detail {
  padding: $spacing-xl;

  .section-title {
    margin-top: $spacing-xl;
    margin-bottom: $spacing-sm;
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: $text-primary;
  }

  .instructions-alert {
    margin-bottom: $spacing-md;
  }

  .page-header {
    margin-bottom: $spacing-xl;

    h2 {
      font-size: $font-size-3xl;
      font-weight: $font-weight-medium;
      color: $text-primary;
      letter-spacing: -0.5px;
    }
  }

  .actions {
    margin-top: $spacing-2xl;
    text-align: center;

    .el-button {
      min-width: 140px;
    }
  }

  .instructions-content {
    .instruction-item {
      display: flex;
      align-items: center;
      gap: $spacing-md;
      padding: $spacing-md 0;
      border-bottom: 1px solid $border-color;

      &:last-of-type {
        border-bottom: none;
      }

      .el-icon {
        font-size: 20px;
        color: $text-tertiary;
      }

      span {
        color: $text-secondary;
      }
    }

    .exam-description {
      h4 {
        margin: 0 0 $spacing-sm 0;
        font-size: $font-size-base;
        font-weight: $font-weight-medium;
        color: $text-primary;
      }

      p {
        margin: 0;
        color: $text-secondary;
        line-height: $line-height-relaxed;
        white-space: pre-wrap;
      }
    }

    .tips {
      h4 {
        margin: 0 0 $spacing-md 0;
        font-size: $font-size-base;
        font-weight: $font-weight-medium;
        color: $text-primary;
      }

      ul {
        margin: 0;
        padding-left: $spacing-xl;
        color: $text-secondary;

        li {
          margin-bottom: $spacing-sm;
          line-height: $line-height-relaxed;
        }
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

// 响应式
@media (max-width: $breakpoint-md) {
  .exam-detail {
    padding: $spacing-md;

    .page-header {
      h2 {
        font-size: $font-size-2xl;
      }
    }

    .dialog-footer {
      flex-direction: column;
      align-items: stretch;
      gap: $spacing-md;
    }
  }
}
</style>
