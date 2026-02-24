<template>
  <div class="manual-paper-form">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="试卷名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入试卷名称" />
      </el-form-item>
      <el-form-item label="所属课程" prop="courseId">
        <el-select v-model="form.courseId" placeholder="请选择课程" class="full-width">
          <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入试卷描述（可选）" />
      </el-form-item>
      <el-form-item label="题目" prop="questions">
        <div class="questions-editor">
          <div v-if="form.questions.length > 0" class="batch-operations">
            <el-button size="small" @click="batchSetScore">批量设置分值</el-button>
            <el-button size="small" @click="removeDuplicates" :disabled="!hasDuplicates">
              去除重复题目
              <el-badge v-if="duplicateCount > 0" :value="duplicateCount" type="warning" />
            </el-button>
            <el-button size="small" type="danger" @click="clearAllQuestions">清空所有</el-button>
            <span class="question-count">共 {{ form.questions.length }} 题，总分 {{ totalScore }}</span>
          </div>

          <div v-for="(pq, index) in form.questions" :key="index" class="question-row">
            <span class="question-index-label">{{ index + 1 }}.</span>
            <el-select v-model="pq.questionId" placeholder="选择题目" class="question-select" filterable @change="updateTotalScore">
              <el-option
                v-for="q in questions"
                :key="q.id"
                :label="`${q.id}. ${q.content.substring(0, 50)}... (${getTypeName(q.type)})`"
                :value="q.id"
              />
            </el-select>
            <el-input-number v-model="pq.score" :min="1" :max="100" @change="updateTotalScore" />
            <el-button type="danger" @click="removeQuestion(index)" :icon="Delete" circle size="small" />
          </div>
          <div class="add-buttons">
            <el-button type="primary" @click="showQuestionSelector" size="small">从题库选择</el-button>
          </div>
        </div>
      </el-form-item>
    </el-form>

    <!-- 题库选择对话框 -->
    <el-dialog v-model="questionSelectorVisible" title="从题库选择题目" width="95%" top="5vh">
      <el-card class="selector-filter-card">
        <el-form :inline="true" :model="questionFilters">
          <el-form-item label="题目类型">
            <el-select v-model="questionFilters.type" placeholder="全部" clearable class="filter-type">
              <el-option label="单选题" value="SINGLE_CHOICE" />
              <el-option label="多选题" value="MULTIPLE_CHOICE" />
              <el-option label="判断题" value="TRUE_FALSE" />
              <el-option label="填空题" value="FILL_BLANK" />
              <el-option label="简答题" value="ESSAY" />
            </el-select>
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="questionFilters.difficulty" placeholder="全部" clearable class="filter-difficulty">
              <el-option label="简单" value="EASY" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="困难" value="HARD" />
            </el-select>
          </el-form-item>
          <el-form-item label="学科">
            <el-input v-model="questionFilters.subject" placeholder="请输入学科" clearable class="filter-subject" />
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="questionFilters.keyword" placeholder="题目内容" clearable class="filter-keyword" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuestionSearch">搜索</el-button>
            <el-button @click="handleQuestionReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-table
        :data="questionTableData"
        @selection-change="handleSelectionChange"
        v-loading="questionsLoading"
        max-height="450"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="题目内容" min-width="300">
          <template #default="{ row }">
            <div class="question-preview-cell">
              <div class="question-type-tag">
                <el-tag :type="getTypeColor(row.type)" size="small">{{ getTypeName(row.type) }}</el-tag>
                <el-tag :type="getDifficultyColor(row.difficulty)" size="small" class="difficulty-tag">
                  {{ getDifficultyName(row.difficulty) }}
                </el-tag>
              </div>
              <div class="question-content-text">{{ row.content }}</div>

              <div v-if="row.options && row.options.length > 0" class="options-preview">
                <div v-for="opt in row.options" :key="opt.id" class="option-item">
                  <span class="option-label">{{ opt.id }}.</span>
                  <span>{{ opt.text }}</span>
                </div>
              </div>

              <div class="correct-answer-preview">
                <span class="label">正确答案：</span>
                <span class="answer" v-if="row.type === 'MULTIPLE_CHOICE' && Array.isArray(row.correctAnswer)">
                  {{ row.correctAnswer.join(', ') }}
                </span>
                <span class="answer" v-else>{{ row.correctAnswer }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="学科" width="100" />
        <el-table-column prop="score" label="默认分值" width="90">
          <template #default="{ row }">
            <el-input-number
              v-model="row.score"
              :min="1"
              :max="100"
              size="small"
              class="score-input"
            />
          </template>
        </el-table-column>
      </el-table>

      <div class="question-pagination">
        <el-pagination
          v-model:current-page="questionPagination.current"
          v-model:page-size="questionPagination.size"
          :page-sizes="[10, 20, 50]"
          :total="questionPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadQuestions"
          @current-change="loadQuestions"
        />
      </div>

      <template #footer>
        <div class="selected-summary">
          <span class="selected-summary__text">已选择 {{ selectedQuestions.length }} 道题目</span>
        </div>
        <el-button @click="questionSelectorVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSelectQuestions" :disabled="selectedQuestions.length === 0">
          确定添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, MessageBoxData } from 'element-plus'
import type { Paper, PaperQuestion, Question, Course } from '@/types'

const props = defineProps<{
  courses: Course[]
  questions: Question[]
  initialData?: Paper | null
}>()

const emit = defineEmits<{
  (e: 'submit', data: any): void
  (e: 'cancel'): void
}>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const questionsLoading = ref(false)
const questionSelectorVisible = ref(false)
const selectedQuestions = ref<Question[]>([])

const form = reactive({
  name: '',
  description: '',
  courseId: null as number | null,
  questions: [] as PaperQuestion[]
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }]
})

const questionFilters = reactive({
  type: '',
  difficulty: '',
  subject: '',
  keyword: ''
})

const questionPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const totalScore = computed(() => {
  return form.questions.reduce((sum, q) => sum + (q.score || 0), 0)
})

const duplicateCount = computed(() => {
  const questionIds = form.questions.map(q => q.questionId).filter(id => id !== 0)
  const uniqueIds = new Set(questionIds)
  return questionIds.length - uniqueIds.size
})

const hasDuplicates = computed(() => duplicateCount.value > 0)

const filteredQuestions = computed(() => {
  let filtered = props.questions

  if (questionFilters.type) {
    filtered = filtered.filter(q => q.type === questionFilters.type)
  }
  if (questionFilters.difficulty) {
    filtered = filtered.filter(q => q.difficulty === questionFilters.difficulty)
  }
  if (questionFilters.subject) {
    filtered = filtered.filter(q => q.subject?.includes(questionFilters.subject))
  }
  if (questionFilters.keyword) {
    filtered = filtered.filter(q => q.content?.includes(questionFilters.keyword))
  }

  return filtered
})

const questionTableData = computed(() => {
  const start = (questionPagination.current - 1) * questionPagination.size
  const end = start + questionPagination.size
  return filteredQuestions.value.slice(start, end)
})

watch(() => props.initialData, (val) => {
  if (val) {
    form.name = val.name
    form.description = val.description || ''
    form.courseId = val.courseId
    form.questions = val.questions ? [...val.questions] : []
  }
}, { immediate: true })

watch(filteredQuestions, (list) => {
  questionPagination.total = list.length
  const maxPage = Math.max(1, Math.ceil(list.length / questionPagination.size))
  if (questionPagination.current > maxPage) {
    questionPagination.current = maxPage
  }
}, { immediate: true })

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

function getDifficultyName(difficulty: string) {
  const map: Record<string, string> = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难'
  }
  return map[difficulty] || difficulty
}

function getDifficultyColor(difficulty: string) {
  const map: Record<string, string> = {
    EASY: 'success',
    MEDIUM: 'warning',
    HARD: 'danger'
  }
  return map[difficulty] || ''
}

function removeQuestion(index: number) {
  form.questions.splice(index, 1)
  updateTotalScore()
}

function updateTotalScore() {
}

function showQuestionSelector() {
  questionSelectorVisible.value = true
}

function handleQuestionSearch() {
  questionPagination.current = 1
  loadQuestions()
}

function handleQuestionReset() {
  questionFilters.type = ''
  questionFilters.difficulty = ''
  questionFilters.subject = ''
  questionFilters.keyword = ''
  questionPagination.current = 1
  loadQuestions()
}

function loadQuestions() {
  questionPagination.total = filteredQuestions.value.length
}

function handleSelectionChange(selection: Question[]) {
  selectedQuestions.value = selection
}

function confirmSelectQuestions() {
  selectedQuestions.value.forEach(q => {
    if (!form.questions.find(pq => pq.questionId === q.id)) {
      form.questions.push({ questionId: q.id, score: q.score })
    }
  })
  questionSelectorVisible.value = false
  ElMessage.success(`已添加 ${selectedQuestions.value.length} 道题目`)
}

function batchSetScore() {
  ElMessageBox.prompt('请输入统一分值', '批量设置分值', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^[1-9]\d*$/,
    inputErrorMessage: '请输入有效的正整数'
  }).then((result: MessageBoxData) => {
    const inputValue = typeof result === 'string' ? result : result.value
    const score = parseInt(inputValue)
    form.questions.forEach(q => {
      q.score = score
    })
    ElMessage.success(`已将所有题目分值设置为 ${score} 分`)
  }).catch(() => {
  })
}

function removeDuplicates() {
  const seen = new Set()
  const unique: PaperQuestion[] = []

  form.questions.forEach(q => {
    if (q.questionId !== 0 && !seen.has(q.questionId)) {
      seen.add(q.questionId)
      unique.push(q)
    } else if (q.questionId === 0) {
      unique.push(q)
    }
  })

  const removedCount = form.questions.length - unique.length
  form.questions = unique

  if (removedCount > 0) {
    ElMessage.success(`已移除 ${removedCount} 个重复题目`)
  } else {
    ElMessage.info('没有重复题目')
  }
}

function clearAllQuestions() {
  ElMessageBox.confirm('确定要清空所有题目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    form.questions = []
    ElMessage.success('已清空所有题目')
  }).catch(() => {
  })
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (form.questions.length === 0) {
        ElMessage.warning('请至少添加一道题目')
        return
      }

      submitting.value = true
      try {
        const data = {
          name: form.name,
          description: form.description,
          courseId: form.courseId ?? undefined,
          type: 'MANUAL' as const,
          totalScore: totalScore.value,
          questions: form.questions
        }
        emit('submit', data)
      } finally {
        submitting.value = false
      }
    }
  })
}

defineExpose({
  handleSubmit
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.manual-paper-form {
  .full-width {
    width: 100%;
  }

  .questions-editor {
    .batch-operations {
      display: flex;
      gap: $spacing-sm;
      align-items: center;
      margin-bottom: $spacing-md;
      padding: $spacing-md;
      background: $bg-secondary;
      border-radius: $radius-sm;

      .question-count {
        margin-left: auto;
        color: $text-tertiary;
        font-size: $font-size-sm;
      }
    }

    .question-row {
      display: flex;
      gap: $spacing-md;
      margin-bottom: $spacing-md;
      align-items: center;

      .question-index-label {
        font-weight: $font-weight-medium;
        color: $text-primary;
        min-width: 30px;
      }
    }

    .add-buttons {
      margin-top: $spacing-md;
    }
  }

  .question-select {
    flex: 1;
  }

  .filter-type {
    width: 120px;
  }

  .filter-difficulty {
    width: 100px;
  }

  .filter-subject {
    width: 150px;
  }

  .filter-keyword {
    width: 200px;
  }

  .difficulty-tag {
    margin-left: $spacing-xs;
  }

  .score-input {
    width: 80px;
  }

  .selected-summary {
    text-align: left;
    flex: 1;
  }

  .selected-summary__text {
    color: $text-tertiary;
  }

  .selector-filter-card {
    margin-bottom: $spacing-lg;
  }

  .question-preview-cell {
    .question-type-tag {
      margin-bottom: $spacing-xs;
    }

    .question-content-text {
      font-size: $font-size-sm;
      line-height: $line-height-relaxed;
      margin-bottom: $spacing-sm;
    }

    .options-preview {
      padding: $spacing-sm;
      background: $bg-secondary;
      border-radius: $radius-sm;
      margin-bottom: $spacing-sm;

      .option-item {
        padding: $spacing-xs 0;
        font-size: $font-size-sm;
      }
    }

    .correct-answer-preview {
      font-size: $font-size-sm;
      color: $text-secondary;
      padding: $spacing-sm;
      background: rgba(103, 194, 58, 0.1);
      border-radius: $radius-sm;
      margin-top: $spacing-sm;

      .label {
        font-weight: $font-weight-medium;
        color: $text-primary;
      }

      .answer {
        color: $black;
        font-weight: $font-weight-medium;
      }
    }
  }

  .question-pagination {
    margin-top: $spacing-lg;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
