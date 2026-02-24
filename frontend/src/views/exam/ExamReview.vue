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
          <div class="option-item" :class="{ 'correct-answer': isCorrectAnswer(index, '正确'), 'user-answer': isUserAnswer(index, '正确') }">
            <span>正确</span>
            <el-icon v-if="isCorrectAnswer(index, '正确')" class="correct-icon"><CircleCheck /></el-icon>
          </div>
          <div class="option-item" :class="{ 'correct-answer': isCorrectAnswer(index, '错误'), 'user-answer': isUserAnswer(index, '错误') }">
            <span>错误</span>
            <el-icon v-if="isCorrectAnswer(index, '错误')" class="correct-icon"><CircleCheck /></el-icon>
          </div>
        </div>

        <!-- 填空题 -->
        <div v-else-if="question.type === 'FILL_BLANK'" class="answer-display">
          <div class="user-answer-box">
            <span class="label">你的答案：</span>
            <span class="answer">{{ getUserAnswer(index) || '未作答' }}</span>
          </div>
          <div class="correct-answer-box" v-if="question.correctAnswer">
            <span class="label">正确答案：</span>
            <span class="answer">{{ formatCorrectAnswer(question.correctAnswer) }}</span>
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

function formatCorrectAnswer(answer: unknown): string {
  if (Array.isArray(answer)) {
    return answer.join(', ')
  }
  return String(answer)
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
    const sessionsRes = await examSessionApi.getMySessions()
    const session = sessionsRes.data.find((s: { examId: number }) => s.examId === examId.value)
    if (session) {
      const resultRes = await examSessionApi.getExamResult(session.id)
      examResult.value = resultRes.data
      answerDetails.value = resultRes.data.answers || []
    }
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
.exam-review {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  gap: 20px;

  .exam-header {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    background: #fff;
    border-bottom: 1px solid #e4e7ed;
    padding: 15px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    z-index: 100;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .exam-info {
      h2 {
        margin: 0 0 10px 0;
        font-size: 20px;
      }

      .info-items {
        display: flex;
        gap: 20px;
        font-size: 14px;
        color: #606266;

        .score-display {
          font-weight: bold;

          .pass {
            color: #67c23a;
          }

          .fail {
            color: #f56c6c;
          }
        }
      }
    }
  }

  .questions-area {
    flex: 1;
    margin-top: 100px;
    margin-right: 280px;

    .question-card {
      margin-bottom: 20px;

      &.correct {
        border-left: 4px solid #67c23a;
      }

      &.wrong {
        border-left: 4px solid #f56c6c;
      }

      .question-header {
        display: flex;
        align-items: center;
        gap: 10px;

        .question-index {
          font-weight: bold;
        }

        .question-score {
          color: #909399;
        }

        .question-status {
          margin-left: auto;
        }
      }

      .question-content {
        margin-bottom: 15px;
        font-size: 15px;
        line-height: 1.6;
      }

      .options {
        .option-item {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 10px;
          margin-bottom: 8px;
          border-radius: 4px;
          background: #f5f7fa;

          &.correct-answer {
            background: #f0f9eb;
            border: 1px solid #67c23a;
          }

          &.user-answer:not(.correct-answer) {
            background: #fef0f0;
            border: 1px solid #f56c6c;
          }

          .option-label {
            font-weight: bold;
            min-width: 20px;
          }

          .correct-icon {
            margin-left: auto;
            color: #67c23a;
            font-size: 18px;
          }
        }
      }

      .answer-display {
        .user-answer-box,
        .correct-answer-box {
          margin-bottom: 10px;
          padding: 10px;
          background: #f5f7fa;
          border-radius: 4px;

          .label {
            font-weight: bold;
            margin-right: 8px;
          }

          .answer {
            color: #606266;
          }
        }

        .correct-answer-box {
          background: #f0f9eb;
        }

        .grading-status {
          margin-top: 10px;
        }

        .score-display {
          margin-top: 10px;

          .label {
            font-weight: bold;
          }

          .score {
            color: #67c23a;
            font-weight: bold;
          }
        }
      }

      .explanation-box {
        margin-top: 15px;
        padding-top: 10px;

        .explanation-content {
          background: #f5f7fa;
          padding: 15px;
          border-radius: 4px;
          color: #606266;
          line-height: 1.6;
        }
      }
    }
  }

  .answer-sheet {
    position: fixed;
    right: 20px;
    top: 100px;
    width: 250px;

    .answer-sheet-header {
      font-weight: bold;
    }

    .answer-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 8px;
      margin-bottom: 15px;

      .answer-item {
        aspect-ratio: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        cursor: pointer;
        font-size: 14px;
        transition: all 0.3s;

        &:hover {
          border-color: #409eff;
        }

        &.correct {
          background: #67c23a;
          color: #fff;
          border-color: #67c23a;
        }

        &.wrong {
          background: #f56c6c;
          color: #fff;
          border-color: #f56c6c;
        }

        &.pending {
          background: #e6a23c;
          color: #fff;
          border-color: #e6a23c;
        }
      }
    }

    .answer-legend {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      font-size: 12px;

      span {
        display: flex;
        align-items: center;
        gap: 5px;
      }

      .dot {
        width: 12px;
        height: 12px;
        border-radius: 2px;

        &.correct {
          background: #67c23a;
        }

        &.wrong {
          background: #f56c6c;
        }

        &.pending {
          background: #e6a23c;
        }
      }
    }
  }
}
</style>
