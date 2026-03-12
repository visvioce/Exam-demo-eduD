<template>
  <div class="exam-take" v-loading="loading" element-loading-text="正在加载考试内容...">
    <!-- 考试信息头部 -->
    <div class="exam-header" v-if="!loading && exam">
      <div class="exam-info">
        <h2>{{ exam?.title }}</h2>
        <div class="info-items">
          <span>总分：{{ exam?.totalScore }}分</span>
          <span>及格分：{{ exam?.passScore }}分</span>
          <span>题目数：{{ questions.length }}道</span>
        </div>
      </div>
      <div class="timer">
        <el-countdown
          :value="endTime"
          @finish="handleTimeUp"
          title="剩余时间"
        />
      </div>
    </div>

    <!-- 题目区域 -->
    <div class="questions-area" v-if="!loading && questions.length > 0">
      <el-card v-for="(question, index) in questions" :key="question.id" class="question-card">
        <template #header>
          <div class="question-header">
            <span class="question-index">第 {{ index + 1 }} 题</span>
            <el-tag :type="getTypeColor(question.type)" size="small">{{ getTypeName(question.type) }}</el-tag>
            <span class="question-score">（{{ questionScores[index] }}分）</span>
          </div>
        </template>

        <div class="question-content" v-html="sanitizeHtml(question.content)"></div>

        <!-- 单选题 -->
        <el-radio-group v-model="answers[index]" v-if="question.type === 'SINGLE_CHOICE'" class="options">
          <el-radio v-for="option in question.options" :key="option.id" :value="option.id" class="option-item">
            <span class="option-label">{{ option.id }}.</span>
            <span>{{ option.text }}</span>
          </el-radio>
        </el-radio-group>

        <!-- 多选题 -->
        <el-checkbox-group v-model="answers[index]" v-else-if="question.type === 'MULTIPLE_CHOICE'" class="options">
          <el-checkbox v-for="option in question.options" :key="option.id" :value="option.id" class="option-item">
            <span class="option-label">{{ option.id }}.</span>
            <span>{{ option.text }}</span>
          </el-checkbox>
        </el-checkbox-group>

        <!-- 判断题 -->
        <el-radio-group v-model="answers[index]" v-else-if="question.type === 'TRUE_FALSE'" class="options">
          <el-radio value="正确">正确</el-radio>
          <el-radio value="错误">错误</el-radio>
        </el-radio-group>

        <!-- 填空题 -->
        <el-input
          v-else-if="question.type === 'FILL_BLANK'"
          v-model="answers[index]"
          type="textarea"
          :rows="2"
          placeholder="请输入答案"
        />

        <!-- 简答题 -->
        <el-input
          v-else-if="question.type === 'ESSAY'"
          v-model="answers[index]"
          type="textarea"
          :rows="6"
          placeholder="请输入答案"
        />
      </el-card>
    </div>

    <!-- 答题卡 -->
    <div class="answer-sheet" v-if="!loading && questions.length > 0">
      <el-card>
        <template #header>
          <div class="answer-sheet-header">
            <span>答题卡</span>
            <div v-if="formattedLastSaveTime" class="auto-save-status">
              <el-icon v-if="isSaving" class="is-loading"><Loading /></el-icon>
              <el-icon v-else><Check /></el-icon>
              <span>{{ isSaving ? '保存中...' : `已保存 ${formattedLastSaveTime}` }}</span>
            </div>
          </div>
        </template>
        <div class="answer-grid">
          <div
            v-for="(_, index) in questions"
            :key="index"
            class="answer-item"
            :class="{ answered: isAnswered(index) }"
            @click="scrollToQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>
        <div class="answer-legend">
          <span><span class="dot answered"></span> 已答</span>
          <span><span class="dot"></span> 未答</span>
        </div>
        <div class="submit-area">
          <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
            提交试卷
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 提交确认对话框 -->
    <el-dialog v-model="confirmDialogVisible" title="确认提交" width="400px">
      <p>您还有 {{ unansweredCount }} 道题未作答，确定要提交吗？</p>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">继续作答</el-button>
        <el-button type="primary" @click="confirmSubmit">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { examApi, examSessionApi } from '@/api/exam'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { Check, Loading } from '@element-plus/icons-vue'
import { sanitizeHtml } from '@/utils/sanitize'
import { getErrorMessage } from '@/utils/error'
import type { Exam, Question, ExamSession } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const confirmDialogVisible = ref(false)

const exam = ref<Exam | null>(null)
const questions = ref<Question[]>([])
const questionScores = ref<number[]>([])
type AnswerValue = string | string[]
const answers = ref<AnswerValue[]>([])
const session = ref<ExamSession | null>(null)
const hasServerAnswers = ref(false)

// 自动保存相关
const autoSaveInterval = ref<ReturnType<typeof setInterval> | null>(null)
const lastSaveTime = ref<Date | null>(null)
const isSaving = ref(false)
const AUTO_SAVE_INTERVAL = 30000 // 30秒自动保存一次
const currentUserId = computed(() => authStore.user?.id ?? 'anonymous')
const LOCAL_STORAGE_KEY = computed(() => `exam_answers_${currentUserId.value}_${examId.value}`)

const examId = computed(() => Number(route.params.id))
const sessionId = computed(() => route.query.sessionId as string)

const endTime = computed(() => {
  if (!session.value?.startedAt || !exam.value?.duration) return Date.now()
  const startTime = new Date(session.value.startedAt).getTime()

  // 基于考试时长的结束时间
  const durationEnd = startTime + exam.value.duration * 60 * 1000

  // 基于考试结束时间的结束时间（如果设置了的话）
  if (exam.value.endedAt) {
    const examEnd = new Date(exam.value.endedAt).getTime()
    // 取两者中较小的值，确保不会超过考试结束时间
    return Math.min(durationEnd, examEnd)
  }

  // 如果没有设置考试结束时间，只使用时长计算
  return durationEnd
})

const unansweredCount = computed(() => {
  return answers.value.filter((_, i) => !isAnswered(i)).length
})

function getTypeName(type: string) {
  const map: Record<string, string> = {
    SINGLE_CHOICE: '单选题',
    MULTIPLE_CHOICE: '多选题',
    TRUE_FALSE: '判断题',
    FILL_BLANK: '填空题',
    ESSAY: '简答题'
  }
  return map[type] || type
}

function getTypeColor(type: string) {
  const map: Record<string, string> = {
    SINGLE_CHOICE: 'primary',
    MULTIPLE_CHOICE: 'success',
    TRUE_FALSE: 'warning',
    FILL_BLANK: 'info',
    ESSAY: 'danger'
  }
  return map[type] || ''
}

function normalizeAnswer(questionType: string, answer: unknown): AnswerValue {
  if (questionType !== 'MULTIPLE_CHOICE') {
    return typeof answer === 'string' ? answer : ''
  }

  if (Array.isArray(answer)) {
    return answer as string[]
  }

  if (typeof answer === 'string') {
    try {
      const parsed = JSON.parse(answer)
      if (Array.isArray(parsed)) {
        return parsed as string[]
      }
    } catch {
      return []
    }
  }

  return []
}

function isAnswered(index: number): boolean {
  const answer = answers.value[index]
  if (answer === undefined || answer === null) return false
  if (typeof answer === 'string') return answer.trim().length > 0
  if (Array.isArray(answer)) return answer.length > 0
  return true
}

function scrollToQuestion(index: number) {
  const cards = document.querySelectorAll('.question-card')
  if (cards[index]) {
    cards[index].scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

async function loadExam(): Promise<void> {
  loading.value = true
  hasServerAnswers.value = false
  try {
    // 加载考试信息
    const examRes = await examApi.getById(examId.value)
    exam.value = examRes.data

    // 如果没有 sessionId，先创建考试会话
    if (!sessionId.value) {
      const startRes = await examApi.start(examId.value)
      session.value = startRes.data
    } else {
      // 如果有 sessionId，加载考试会话详情
      const sessionRes = await examSessionApi.getById(Number(sessionId.value))
      session.value = sessionRes.data
    }

    // 加载考试题目
    if (exam.value?.paperId) {
      try {
        // 使用学生专用的获取考试题目接口
        const questionsRes = await examApi.getQuestions(examId.value)
        questions.value = questionsRes.data

        // 初始化答案数组和题目分数
        answers.value = questions.value.map(q => {
          if (q.type === 'MULTIPLE_CHOICE') return []
          return ''
        })

        // 初始化题目分数
        questionScores.value = questions.value.map(q => q.score || 0)

        // 检查是否有题目
        if (questions.value.length === 0) {
          ElMessage.warning('该考试暂无题目，请联系教师')
          setTimeout(() => {
            router.push('/exam')
          }, 2000)
          return
        }

        // 如果考试会话中有已保存的答案，加载它们（优先于本地存储）
        if (session.value?.answers && session.value.answers.length > 0) {
          session.value.answers.forEach((savedAnswer) => {
            const index = questions.value.findIndex(q => q.id === savedAnswer.questionId)
            if (index !== -1) {
              const question = questions.value[index]
              if (question) {
                answers.value[index] = normalizeAnswer(question.type, savedAnswer.answer)
              }
            }
          })
          hasServerAnswers.value = true
        }
      } catch (questionsError: unknown) {
        ElMessage.error(getErrorMessage(questionsError, '加载题目失败'))
        setTimeout(() => {
          router.push('/exam')
        }, 2000)
        return
      }
    } else {
      ElMessage.warning('该考试未配置试卷，请联系教师')
      setTimeout(() => {
        router.push('/exam')
      }, 2000)
      return
    }
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, '加载考试失败'))
    // 加载失败，跳回考试列表
    setTimeout(() => {
      router.push('/exam')
    }, 2000)
  } finally {
    loading.value = false
  }
}

function handleTimeUp() {
  ElMessage.warning('考试时间已到，系统将自动提交试卷')
  void confirmSubmit()
}

// 构建答案数据
function buildAnswerData() {
  return questions.value.map((q, index) => {
    let answerValue = answers.value[index]

    // 多选题答案需要序列化为JSON字符串
    if (q.type === 'MULTIPLE_CHOICE' && Array.isArray(answerValue)) {
      answerValue = JSON.stringify(answerValue.sort())
    }

    return {
      questionId: q.id,
      answer: answerValue || '',
      questionType: q.type
    }
  })
}

// 构建提交数据：只提交已作答题目，支持交卷时留空未答题
function buildSubmitAnswerData() {
  return buildAnswerData().filter((_, index) => isAnswered(index))
}

// 保存答案到 localStorage
function saveToLocal() {
  try {
    const data = {
      answers: answers.value,
      savedAt: new Date().toISOString()
    }
    localStorage.setItem(LOCAL_STORAGE_KEY.value, JSON.stringify(data))
  } catch {
    // 忽略本地存储异常（如隐私模式）
  }
}

// 从 localStorage 恢复答案
function restoreFromLocal() {
  try {
    const data = localStorage.getItem(LOCAL_STORAGE_KEY.value)
    if (data) {
      const parsed = JSON.parse(data)
      if (parsed.answers && Array.isArray(parsed.answers)) {
        // 恢复答案
        parsed.answers.forEach((savedAnswer: unknown, index: number) => {
          if (index < answers.value.length) {
            const question = questions.value[index]
            if (question) {
              answers.value[index] = normalizeAnswer(question.type, savedAnswer)
            }
          }
        })
        ElMessage.success(`已恢复 ${new Date(parsed.savedAt).toLocaleTimeString()} 保存的答案`)
        return true
      }
    }
  } catch {
    // 忽略本地数据损坏
  }
  return false
}

// 自动保存答案到服务器
async function autoSaveAnswers() {
  if (isSaving.value || submitting.value) return

  isSaving.value = true
  try {
    const answerData = buildAnswerData()
    await examApi.autoSave(examId.value, answerData)
    lastSaveTime.value = new Date()
    saveToLocal() // 同时保存到本地
  } catch {
    // 自动保存失败不提示错误，避免干扰考试
  } finally {
    isSaving.value = false
  }
}

// 启动自动保存
function startAutoSave() {
  // 无服务端缓存时再从本地恢复，避免覆盖服务端已保存答案
  if (!hasServerAnswers.value) {
    restoreFromLocal()
  }

  // 启动定时自动保存
  autoSaveInterval.value = setInterval(() => {
    autoSaveAnswers()
  }, AUTO_SAVE_INTERVAL)
}

// 停止自动保存
function stopAutoSave() {
  if (autoSaveInterval.value) {
    clearInterval(autoSaveInterval.value)
    autoSaveInterval.value = null
  }
}

// 格式化最后保存时间
const formattedLastSaveTime = computed(() => {
  if (!lastSaveTime.value) return null
  return lastSaveTime.value.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
})

async function handleSubmit() {
  if (unansweredCount.value > 0) {
    confirmDialogVisible.value = true
  } else {
    await confirmSubmit()
  }
}

async function confirmSubmit() {
  if (submitting.value) return

  confirmDialogVisible.value = false
  submitting.value = true

  // 提交前停止自动保存
  stopAutoSave()

  try {
    const answerData = buildSubmitAnswerData()
    await examApi.submit(examId.value, answerData)

    // 提交成功后清除本地存储
    localStorage.removeItem(LOCAL_STORAGE_KEY.value)

    ElMessage.success('提交成功')
    router.push('/exam')
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, '提交失败'))
    // 提交失败，重新启动自动保存
    startAutoSave()
  } finally {
    submitting.value = false
  }
}

// 防止意外关闭页面
function handleBeforeUnload(e: BeforeUnloadEvent) {
  if (answers.value.some((_, i) => isAnswered(i))) {
    // 仅提示确认；实际保存由自动保存与本地存储兜底
    e.preventDefault()
    e.returnValue = ''
  }
}

// 监听答案变化，保存到本地
watch(answers, () => {
  saveToLocal()
}, { deep: true })

onMounted(() => {
  loadExam().then(() => {
    // 加载完成后启动自动保存
    startAutoSave()
  })
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onUnmounted(() => {
  stopAutoSave()
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.exam-take {
  height: 100vh;
  min-height: 100vh;
  background: $bg-page;
  overflow-y: auto;
  overflow-x: hidden;

  // 顶部固定信息栏
  .exam-header {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: 72px;
    background: $bg-primary;
    padding: 0 40px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid $border-color;
    z-index: 100;

    .exam-info {
      h2 {
        margin: 0 0 8px 0;
        font-size: $font-size-xl;
        font-weight: $font-weight-medium;
        color: $text-primary;
      }

      .info-items {
        display: flex;
        gap: 24px;
        span {
          color: $text-tertiary;
          font-size: $font-size-sm;
        }
      }
    }

    .timer {
      background: $bg-secondary;
      padding: 12px 24px;
      border-radius: $radius-md;
      border: 1px solid $border-color;
      
      :deep(.el-countdown) {
        .el-countdown__title {
          color: $text-tertiary;
          font-size: $font-size-xs;
        }
        .el-countdown__value {
          color: $text-primary;
          font-size: $font-size-2xl;
          font-weight: $font-weight-medium;
        }
      }
    }
  }

  // 主内容区域
  .questions-area {
    margin-left: auto;
    margin-right: 320px;
    max-width: 960px;
    padding: 100px 40px 80px;

    .question-card {
      margin-bottom: $spacing-xl;
      border: 1px solid $border-color;
      border-radius: $radius-lg;
      background: $bg-primary;
      transition: box-shadow $transition-fast;

      &:hover {
        box-shadow: none;
      }

      :deep(.el-card__header) {
        padding: $spacing-lg $spacing-xl;
        border-bottom: 1px solid $border-light;
        background: $bg-secondary;
        border-radius: $radius-lg $radius-lg 0 0;
      }

      :deep(.el-card__body) {
        padding: $spacing-xl;
      }

      .question-header {
        display: flex;
        align-items: center;
        gap: $spacing-md;

        .question-index {
          font-size: $font-size-base;
          font-weight: $font-weight-medium;
          color: $text-primary;
        }

        .question-score {
          color: $text-tertiary;
          font-size: $font-size-sm;
        }
      }

      .question-content {
        margin-bottom: $spacing-xl;
        font-size: $font-size-base;
        line-height: $line-height-relaxed;
        color: $text-secondary;
      }

      .options {
        display: flex;
        flex-direction: column;
        gap: $spacing-md;

        .option-item {
          display: flex;
          align-items: flex-start;
          padding: $spacing-lg;
          border: 1px solid $border-color;
          border-radius: $radius-md;
          transition: all $transition-fast;
          background: $bg-primary;

          &:hover {
            background: $bg-hover;
            border-color: $gray-400;
          }

          .option-label {
            display: inline-block;
            min-width: 18px;
            font-weight: $font-weight-medium;
            margin-right: $spacing-md;
            color: $text-primary;
          }
        }

        :deep(.el-radio),
        :deep(.el-checkbox) {
          margin-right: 0;
          width: 100%;
          height: auto;
        }

        :deep(.el-radio__label),
        :deep(.el-checkbox__label) {
          display: inline-flex;
          align-items: flex-start;
          line-height: $line-height-relaxed;
          white-space: normal;
          word-break: break-word;
          padding-left: $spacing-sm;
          width: 100%;
        }
      }

      // 填空题和简答题样式
      :deep(.el-textarea__inner) {
        border-radius: $radius-md;
        border-color: $border-color;
        &:focus {
          border-color: $black;
        }
      }
    }
  }

  // 右侧答题卡
  .answer-sheet {
    position: fixed;
    right: 24px;
    top: 96px;
    width: 280px;
    max-height: calc(100vh - 120px);
    border: 1px solid $border-color;
    border-radius: $radius-lg;
    background: $bg-primary;
    overflow: hidden;

    :deep(.el-card__header) {
      padding: $spacing-lg $spacing-xl;
      border-bottom: 1px solid $border-light;
    }

    :deep(.el-card__body) {
      padding: $spacing-xl;
      max-height: calc(100vh - 200px);
      overflow-y: auto;
    }

    .answer-sheet-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      span {
        font-size: $font-size-base;
        font-weight: $font-weight-medium;
        color: $text-primary;
      }

      .auto-save-status {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: $font-size-xs;
        color: $text-tertiary;

        .el-icon {
          font-size: 12px;
        }

        .is-loading {
          animation: rotating 1s linear infinite;
        }
      }
    }

    .answer-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: $spacing-sm;

      .answer-item {
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 1px solid $border-color;
        border-radius: $radius-md;
        cursor: pointer;
        font-size: $font-size-sm;
        font-weight: $font-weight-normal;
        color: $text-secondary;
        transition: all $transition-fast;
        background: $bg-primary;

        &.answered {
          background: $black;
          color: #fff;
          border-color: $black;
        }

        &:hover {
          border-color: $black;
        }
      }
    }

    .answer-legend {
      margin-top: $spacing-lg;
      padding-top: $spacing-lg;
      border-top: 1px solid $border-light;
      display: flex;
      justify-content: center;
      gap: $spacing-xl;
      font-size: $font-size-sm;
      color: $text-tertiary;

      .dot {
        display: inline-block;
        width: 14px;
        height: 14px;
        border: 1px solid $border-color;
        border-radius: $radius-sm;
        vertical-align: middle;
        margin-right: $spacing-xs;

        &.answered {
          background: $black;
          border-color: $black;
        }
      }
    }

    .submit-area {
      margin-top: $spacing-xl;

      .el-button {
        width: 100%;
        height: 44px;
        font-size: $font-size-base;
        border-radius: $radius-md;
        background: $black;
        border-color: $black;

        &:hover {
          background: $gray-900;
          border-color: $gray-900;
        }
      }
    }
  }
}

// 响应式
@media (max-width: 1200px) {
  .exam-take {
    .questions-area {
      margin-right: 0;
      max-width: 100%;
    }

    .answer-sheet {
      display: none;
    }
  }
}

@media (max-width: 768px) {
  .exam-take {
    .exam-header {
      height: auto;
      flex-direction: column;
      padding: 16px 20px;
      gap: 12px;

      .exam-info {
        text-align: center;
        h2 {
          font-size: $font-size-lg;
          margin-bottom: 4px;
        }
        .info-items {
          gap: 12px;
          span {
            font-size: $font-size-xs;
          }
        }
      }
    }

    .questions-area {
      padding: 140px 16px 24px;
    }
  }
}

// 加载动画
@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
