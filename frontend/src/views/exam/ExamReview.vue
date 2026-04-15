<template>
  <div class="exam-review" v-loading="loading" element-loading-text="正在加载考试结果...">
    <!-- 考试信息头部 -->
    <div class="exam-header" v-if="!loading && exam">
      <div class="exam-info">
        <h2>{{ exam?.title }} - 考试回顾</h2>
        <div class="info-items">
          <span>总分：{{ exam?.totalScore }}分</span>
          <span>及格分：{{ exam?.passScore }}分</span>
          <span>题目数：{{ questions.length }}道</span>
          <span v-if="examResult" class="score-display">
            得分：<span :class="getScoreClass(examResult.totalScore, examResult.maxScore)">{{ examResult.totalScore }}</span> / {{ examResult.maxScore }}分
            <el-tag v-if="hasPendingGrading" type="warning" size="small" style="margin-left: 8px">
              部分题目待批阅
            </el-tag>
          </span>
        </div>
      </div>
      <div class="back-btn">
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>

    <!-- 题目区域 -->
    <div class="questions-area" v-if="!loading && questions.length > 0">
      <el-card v-for="(question, index) in questions" :key="question.id" class="question-card"
        :class="{ 'correct': isCorrect(index), 'wrong': isWrong(index) }">
        <template #header>
          <div class="question-header">
            <span class="question-index">第 {{ index + 1 }} 题</span>
            <el-tag :type="getTypeColor(question.type)" size="small">{{ getTypeName(question.type) }}</el-tag>
            <span class="question-score">（{{ questionScores[index] }}分）</span>
            <span v-if="getQuestionStatus(index)" class="question-status">
              <el-tag :type="getQuestionStatusType(index)" size="small">
                {{ getQuestionStatus(index) }}
              </el-tag>
            </span>
          </div>
        </template>

        <div class="question-content" v-html="sanitizeHtml(question.content)"></div>

        <!-- 单选题 -->
        <div v-if="question.type === 'SINGLE_CHOICE'" class="options">
          <div v-for="option in question.options" :key="option.id" class="option-item"
            :class="{ 'correct-answer': isCorrectAnswer(index, option.id), 'user-answer': isUserAnswer(index, option.id) }">
            <span class="option-label">{{ option.id }}.</span>
            <span>{{ option.text }}</span>
            <el-icon v-if="isCorrectAnswer(index, option.id)" class="correct-icon"><CircleCheck /></el-icon>
          </div>
        </div>

        <!-- 多选题 -->
        <div v-else-if="question.type === 'MULTIPLE_CHOICE'" class="options">
          <div v-for="option in question.options" :key="option.id" class="option-item"
            :class="{ 'correct-answer': isCorrectAnswer(index, option.id), 'user-answer': isUserAnswer(index, option.id) }">
            <span class="option-label">{{ option.id }}.</span>
            <span>{{ option.text }}</span>
            <el-icon v-if="isCorrectAnswer(index, option.id)" class="correct-icon"><CircleCheck /></el-icon>
          </div>
        </div>

        <!-- 判断题 -->
        <div v-else-if="question.type === 'TRUE_FALSE'" class="options">
          <div class="option-item" :class="{ 'correct-answer': isCorrectAnswer(index, 'A'), 'user-answer': isUserAnswer(index, '正确') }">
            <span>正确</span>
            <el-icon v-if="isCorrectAnswer(index, 'A')" class="correct-icon"><CircleCheck /></el-icon>
          </div>
          <div class="option-item" :class="{ 'correct-answer': isCorrectAnswer(index, 'B'), 'user-answer': isUserAnswer(index, '错误') }">
            <span>错误</span>
            <el-icon v-if="isCorrectAnswer(index, 'B')" class="correct-icon"><CircleCheck /></el-icon>
          </div>
        </div>

        <!-- 填空题 -->
        <div v-else-if="question.type === 'FILL_BLANK'" class="answer-display">
          <div class="user-answer-box">
            <span class="label">你的答案：</span>
            <div class="fill-blank-answers">
              <span v-if="!getUserAnswer(index)">未作答</span>
              <template v-else-if="Array.isArray(getUserAnswer(index))">
                <span v-for="(ans, i) in getUserAnswer(index)" :key="i" class="blank-item">
                  第{{ i + 1 }}空：{{ ans || '未填' }}
                </span>
              </template>
              <span v-else>{{ getUserAnswer(index) }}</span>
            </div>
          </div>
          <div class="correct-answer-box" v-if="question.correctAnswer">
            <span class="label">正确答案：</span>
            <div class="fill-blank-answers">
              <template v-if="Array.isArray(question.correctAnswer)">
                <span v-for="(ans, i) in question.correctAnswer" :key="i" class="blank-item">
                  第{{ i + 1 }}空：{{ ans }}
                </span>
              </template>
              <span v-else>{{ question.correctAnswer }}</span>
            </div>
          </div>
        </div>

        <!-- 简答题 -->
        <div v-else-if="question.type === 'ESSAY'" class="answer-display">
          <div class="user-answer-box">
            <span class="label">你的答案：</span>
            <span class="answer">{{ getUserAnswer(index) || '未作答' }}</span>
          </div>
          <div class="grading-status" v-if="isPendingGrading(index)">
            <el-tag type="warning">等待老师批阅</el-tag>
          </div>
          <div class="score-display" v-else-if="getQuestionScore(index) !== null">
            <span class="label">得分：</span>
            <span class="score">{{ getQuestionScore(index) }} / {{ questionScores[index] }}分</span>
          </div>
        </div>

        <!-- 解析 -->
        <div v-if="question.explanation" class="explanation-box">
          <el-divider content-position="left">解析</el-divider>
          <div class="explanation-content" v-html="sanitizeHtml(question.explanation)"></div>
        </div>
      </el-card>
    </div>

    <!-- 答题卡 -->
    <div class="answer-sheet" v-if="!loading && questions.length > 0">
      <el-card>
        <template #header>
          <div class="answer-sheet-header">
            <span>答题卡</span>
          </div>
        </template>
        <div class="answer-grid">
          <div
            v-for="(_, index) in questions"
            :key="index"
            class="answer-item"
            :class="{ correct: isCorrect(index), wrong: isWrong(index), pending: isPendingGrading(index) }"
            @click="scrollToQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>
        <div class="answer-legend">
          <span><span class="dot correct"></span> 正确</span>
          <span><span class="dot wrong"></span> 错误</span>
          <span><span class="dot pending"></span> 待批阅</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { examApi, examSessionApi } from '@/api/exam'
import { ElMessage } from 'element-plus'
import { CircleCheck } from '@element-plus/icons-vue'
import { sanitizeHtml } from '@/utils/sanitize'
import { getErrorMessage } from '@/utils/error'
// formatAnswerDisplay 已在模板中直接使用 formatAnswer 函数
import type { Exam, Question, ExamResultResponse, AnswerDetail } from '@/types'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const exam = ref<Exam | null>(null)
const questions = ref<Question[]>([])
const questionScores = ref<number[]>([])
const examResult = ref<ExamResultResponse | null>(null)
const answerDetails = ref<AnswerDetail[]>([])

const examId = computed(() => Number(route.params.id))

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

function goBack() {
  router.back()
}

function getScoreClass(score: number, maxScore: number) {
  void maxScore
  const passScore = exam.value?.passScore || 60
  return score >= passScore ? 'pass' : 'fail'
}

function getUserAnswer(index: number): string {
  const detail = answerDetails.value[index]
  if (!detail || !detail.answer) return ''
  if (Array.isArray(detail.answer)) {
    return detail.answer.join(', ')
  }
  return String(detail.answer)
}

function isCorrect(index: number): boolean {
  const detail = answerDetails.value[index]
  return detail?.isCorrect === true
}

function isWrong(index: number): boolean {
  const detail = answerDetails.value[index]
  const question = questions.value[index]
  // 简答题未评分不算错误
  if (question?.type === 'ESSAY') return false
  return detail?.isCorrect === false
}

function isPendingGrading(index: number): boolean {
  const detail = answerDetails.value[index]
  const question = questions.value[index]
  return question?.type === 'ESSAY' && detail?.gradingStatus === 'PENDING'
}

const hasPendingGrading = computed(() => {
  return answerDetails.value.some(detail => detail?.gradingStatus === 'PENDING')
})

function getQuestionStatus(index: number): string | null {
  const detail = answerDetails.value[index]
  const question = questions.value[index]

  if (question?.type === 'ESSAY') {
    if (detail?.gradingStatus === 'PENDING') return '待批阅'
    return '已评分'
  }

  if (detail?.isCorrect === true) return '正确'
  if (detail?.isCorrect === false) return '错误'
  return '未作答'
}

function getQuestionStatusType(index: number): string {
  const detail = answerDetails.value[index]
  const question = questions.value[index]

  if (question?.type === 'ESSAY') {
    return detail?.gradingStatus === 'PENDING' ? 'warning' : 'success'
  }

  if (detail?.isCorrect === true) return 'success'
  if (detail?.isCorrect === false) return 'danger'
  return 'info'
}

function getQuestionScore(index: number): number | null {
  const detail = answerDetails.value[index]
  return detail?.score ?? null
}

function isCorrectAnswer(index: number, optionId: string): boolean {
  const question = questions.value[index]
  if (!question?.correctAnswer) return false

  if (Array.isArray(question.correctAnswer)) {
    return question.correctAnswer.includes(optionId)
  }
  return String(question.correctAnswer) === optionId
}

function isUserAnswer(index: number, optionId: string): boolean {
  const detail = answerDetails.value[index]
  if (!detail?.answer) return false

  if (Array.isArray(detail.answer)) {
    return detail.answer.includes(optionId)
  }
  return String(detail.answer) === optionId
}

function scrollToQuestion(index: number) {
  const element = document.querySelectorAll('.question-card')[index]
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

async function loadData() {
  loading.value = true
  try {
    // 获取考试信息
    const examRes = await examApi.getById(examId.value)
    exam.value = examRes.data

    // 获取考试会话
    const sessionsRes = await examSessionApi.getMySessions()
    const session = sessionsRes.data.find((s: { examId: number }) => s.examId === examId.value)

    // 权限检查：学生必须已参加考试才能查看回顾
    if (!session) {
      ElMessage.warning('您尚未参加该考试，无法查看回顾')
      router.push('/exam')
      return
    }

    // 权限检查：考试未结束且学生未提交时，不能查看回顾
    const now = new Date().getTime()
    const end = new Date(exam.value.endedAt).getTime()
    if (now < end && session.status === 'IN_PROGRESS') {
      ElMessage.warning('考试尚未结束，请先完成考试')
      router.push('/exam')
      return
    }

    // 获取试卷信息以获取分值
    const paperRes = await examApi.getPaper(examId.value)
    const paper = paperRes.data

    // 获取完整题目信息（包含正确答案和解析）
    const questionsRes = await examApi.getReviewQuestions(examId.value)
    questions.value = questionsRes.data

    if (paper?.questions) {
      // 创建题目ID到分值的映射
      const scoreMap = new Map(paper.questions.map((p: { questionId: number; score: number }) => [p.questionId, p.score]))
      // 按照试卷顺序设置分值
      questionScores.value = questions.value.map(q => scoreMap.get(q.id) || 0)
    }

    // 获取考试结果
    const resultRes = await examSessionApi.getExamResult(session.id)
    examResult.value = resultRes.data
    answerDetails.value = resultRes.data.answers || []
  } catch (error) {
    ElMessage.error(getErrorMessage(error, '加载考试结果失败'))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.exam-review {
  max-width: 1280px;
  margin: 0 auto;
  padding: $spacing-xl;
  display: grid;
  grid-template-columns: 1fr 260px;
  gap: $spacing-lg;
  align-items: start;

  .exam-header {
    grid-column: 1 / -1;
    position: sticky;
    top: 0;
    z-index: 20;
    background: $bg-primary;
    border: 1px solid $border-color;
    border-radius: $radius-md;
    padding: $spacing-lg $spacing-xl;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;

    .exam-info {
      h2 {
        margin: 0 0 $spacing-sm 0;
        font-size: $font-size-xl;
        color: $text-primary;
      }

      .info-items {
        display: flex;
        flex-wrap: wrap;
        gap: $spacing-md $spacing-xl;
        font-size: $font-size-sm;
        color: $text-tertiary;

        .score-display {
          font-weight: 600;

          .pass {
            color: $success;
          }

          .fail {
            color: $danger;
          }
        }
      }
    }
  }

  .questions-area {
    min-width: 0;

    .question-card {
      margin-bottom: $spacing-lg;
      border: 1px solid $border-color;

      &.correct {
        border-left: 3px solid $success;
      }

      &.wrong {
        border-left: 3px solid $danger;
      }

      .question-header {
        display: flex;
        align-items: center;
        gap: $spacing-sm;

        .question-index {
          font-weight: 600;
        }

        .question-score {
          color: $text-tertiary;
        }

        .question-status {
          margin-left: auto;
        }
      }

      .question-content {
        margin-bottom: $spacing-md;
        font-size: $font-size-base;
        line-height: $line-height-relaxed;
      }

      .options {
        .option-item {
          display: flex;
          align-items: center;
          gap: $spacing-md;
          padding: $spacing-sm $spacing-md;
          margin-bottom: $spacing-sm;
          border-radius: $radius-sm;
          border: 1px solid $border-light;
          background: $bg-secondary;

          &.correct-answer {
            background: $success-bg;
            border-color: $success;
          }

          &.user-answer:not(.correct-answer) {
            background: $danger-bg;
            border-color: $danger;
          }

          .option-label {
            font-weight: 600;
            min-width: 20px;
          }

          .correct-icon {
            margin-left: auto;
            color: $success;
            font-size: $font-size-lg;
          }
        }
      }

      .answer-display {
        .user-answer-box,
        .correct-answer-box {
          margin-bottom: $spacing-sm;
          padding: $spacing-sm $spacing-md;
          background: $bg-secondary;
          border: 1px solid $border-light;
          border-radius: $radius-sm;

          .label {
            font-weight: 600;
            margin-right: $spacing-sm;
          }

          .answer {
            color: $text-secondary;
          }
        }

        .correct-answer-box {
          background: $success-bg;
          border-color: $success;
        }

        .grading-status {
          margin-top: $spacing-sm;
        }

        .score-display {
          margin-top: $spacing-sm;

          .label {
            font-weight: 600;
          }

          .score {
            color: $success;
            font-weight: 600;
          }
        }
      }

      .explanation-box {
        margin-top: $spacing-md;

        .explanation-content {
          background: $bg-secondary;
          border: 1px solid $border-light;
          padding: $spacing-md;
          border-radius: $radius-sm;
          color: $text-secondary;
          line-height: $line-height-relaxed;
        }
      }
    }
  }

  .answer-sheet {
    position: sticky;
    top: calc(#{$nav-height} + #{$spacing-lg});

    .answer-sheet-header {
      font-weight: 600;
      color: $text-primary;
    }

    .answer-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: $spacing-sm;
      margin-bottom: $spacing-md;

      .answer-item {
        aspect-ratio: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 1px solid $border-color;
        border-radius: $radius-sm;
        cursor: pointer;
        font-size: $font-size-sm;
        color: $text-secondary;
        background: $bg-primary;
        transition: all $transition-fast;

        &:hover {
          border-color: $text-primary;
          color: $text-primary;
        }

        &.correct {
          background: $success;
          color: #fff;
          border-color: $success;
        }

        &.wrong {
          background: $danger;
          color: #fff;
          border-color: $danger;
        }

        &.pending {
          background: $warning;
          color: #fff;
          border-color: $warning;
        }
      }
    }

    .answer-legend {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-md;
      font-size: $font-size-xs;
      color: $text-tertiary;

      span {
        display: flex;
        align-items: center;
        gap: $spacing-xs;
      }

      .dot {
        width: 10px;
        height: 10px;
        border-radius: 2px;

        &.correct {
          background: $success;
        }

        &.wrong {
          background: $danger;
        }

        &.pending {
          background: $warning;
        }
      }
    }
  }
}

@media (max-width: $breakpoint-lg) {
  .exam-review {
    grid-template-columns: 1fr;

    .answer-sheet {
      position: static;
      width: 100%;
    }
  }
}

@media (max-width: $breakpoint-md) {
  .exam-review {
    padding: $spacing-md;

    .exam-header {
      padding: $spacing-md;
      flex-direction: column;
      gap: $spacing-md;
    }
  }
}
</style>
