<template>
  <div class="question-list base-list-page">
    <div class="page-header">
      <h2>题库管理</h2>
      <div class="header-actions">
        <el-button @click="handleCreate" v-if="hasPermission(['ADMIN', 'TEACHER'])">
          <el-icon><Plus /></el-icon>
          添加题目
        </el-button>
        <el-button type="success" @click="handleAiGenerate" v-if="hasPermission(['ADMIN', 'TEACHER'])">
          <el-icon><MagicStick /></el-icon>
          AI出题
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px">
        <el-form-item label="题目类型">
          <div class="filter-tabs">
            <span 
              v-for="item in typeOptions" 
              :key="item.value"
              :class="['tab-item', { active: searchForm.type === item.value }]"
              @click="filterTypeChange(item.value)"
            >
              {{ item.label }}
            </span>
          </div>
        </el-form-item>
        <el-form-item label="难度">
          <div class="filter-tabs">
            <span 
              v-for="item in difficultyOptions" 
              :key="item.value"
              :class="['tab-item', { active: searchForm.difficulty === item.value }]"
              @click="filterDifficultyChange(item.value)"
            >
              {{ item.label }}
            </span>
          </div>
        </el-form-item>
        <el-form-item label="学科">
          <el-select v-model="searchForm.subject" placeholder="全部学科" clearable @change="handleSubjectChange" style="width: 200px;" allow-create filterable>
            <el-option v-for="subject in subjects" :key="subject" :label="subject" :value="subject" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 题目列表 -->
    <el-card class="table-card">
      <el-table :data="questions" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="题目内容" min-width="300">
          <template #default="{ row }">
            <div class="question-content" v-html="sanitizeAndTruncate(row.content, 100)"></div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80">
          <template #default="{ row }">
            <el-tag :type="getDifficultyColor(row.difficulty)" size="small">
              {{ getDifficultyName(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subject" label="学科" width="100" />
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <ActionButtons
              @view="handleView(row)"
              @edit="handleEdit(row)"
              @delete="handleDelete(row)"
              :show-edit="canEdit(row)"
              :show-delete="canEdit(row)"
            />
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadQuestions"
          @current-change="loadQuestions"
        />
      </div>
    </el-card>

    <!-- 查看题目对话框 -->
    <el-dialog v-model="viewDialogVisible" title="题目详情" width="700px" class="base-dialog">
      <el-descriptions :column="2" border v-if="currentQuestion">
        <el-descriptions-item label="题目类型">{{ getTypeName(currentQuestion.type) }}</el-descriptions-item>
        <el-descriptions-item label="难度">{{ getDifficultyName(currentQuestion.difficulty) }}</el-descriptions-item>
        <el-descriptions-item label="学科">{{ currentQuestion.subject }}</el-descriptions-item>
        <el-descriptions-item label="分值">{{ currentQuestion.score }}</el-descriptions-item>
        <el-descriptions-item label="题目内容" :span="2">
          <div v-html="sanitizeHtml(currentQuestion.content)"></div>
        </el-descriptions-item>
        <el-descriptions-item label="选项" :span="2" v-if="currentQuestion.options && currentQuestion.options.length > 0">
          <div v-for="option in currentQuestion.options" :key="option.id" class="option-item">
            <span class="option-label">{{ option.id }}.</span>
            <span>{{ option.text }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="正确答案" :span="2">
          <div v-if="currentQuestion.type === 'SINGLE_CHOICE' || currentQuestion.type === 'TRUE_FALSE'">
            {{ currentQuestion.correctAnswer }}
          </div>
          <div v-else-if="currentQuestion.type === 'MULTIPLE_CHOICE'">
            {{ formatCorrectAnswer(currentQuestion.correctAnswer) }}
          </div>
          <div v-else v-html="sanitizeHtml(String(currentQuestion.correctAnswer ?? ''))"></div>
        </el-descriptions-item>
        <el-descriptions-item label="解析" :span="2" v-if="currentQuestion.explanation">
          <div v-html="currentQuestion.explanation"></div>
        </el-descriptions-item>
        <el-descriptions-item label="评分标准" :span="2" v-if="currentQuestion.scoringCriteria && currentQuestion.scoringCriteria.length > 0">
          <div v-for="(criterion, index) in currentQuestion.scoringCriteria" :key="index" class="criterion-item">
            <span>{{ criterion.point }}: {{ criterion.score }}分</span>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑题目' : '添加题目'" width="800px" top="5vh" class="base-dialog">
      <el-form :model="questionForm" :rules="rules" ref="questionFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="题目类型" prop="type">
              <el-select v-model="questionForm.type" placeholder="请选择类型" @change="handleTypeChange">
                <el-option label="单选题" value="SINGLE_CHOICE" />
                <el-option label="多选题" value="MULTIPLE_CHOICE" />
                <el-option label="判断题" value="TRUE_FALSE" />
                <el-option label="填空题" value="FILL_BLANK" />
                <el-option label="简答题" value="ESSAY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="questionForm.difficulty" placeholder="请选择难度">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学科" prop="subject">
              <el-select v-model="questionForm.subject" placeholder="请选择学科" allow-create filterable class="full-width">
                <el-option v-for="subject in subjects" :key="subject" :label="subject" :value="subject" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分值" prop="score">
              <el-input-number v-model="questionForm.score" :min="1" :max="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="questionForm.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>

        <!-- 选项（选择题/判断题） -->
        <el-form-item label="选项" v-if="showOptions">
          <div class="options-editor">
            <div v-for="(option, index) in questionForm.options" :key="index" class="option-row">
              <el-input v-model="option.id" class="option-id-input" placeholder="A" />
              <el-input v-model="option.text" placeholder="选项内容" class="option-text-input" />
              <el-button type="danger" @click="removeOption(index)" :icon="Delete" circle size="small" />
            </div>
            <el-button type="primary" @click="addOption" size="small">添加选项</el-button>
          </div>
        </el-form-item>

        <el-form-item label="正确答案" prop="correctAnswer">
          <!-- 单选题 -->
          <el-select v-model="questionForm.correctAnswer" placeholder="请选择正确答案" v-if="questionForm.type === 'SINGLE_CHOICE'">
            <el-option v-for="option in questionForm.options" :key="option.id" :label="option.id" :value="option.id" />
          </el-select>
          <!-- 多选题 -->
          <el-select v-model="questionForm.correctAnswer" multiple placeholder="请选择正确答案" v-else-if="questionForm.type === 'MULTIPLE_CHOICE'">
            <el-option v-for="option in questionForm.options" :key="option.id" :label="option.id" :value="option.id" />
          </el-select>
          <!-- 判断题 -->
          <el-radio-group v-model="questionForm.correctAnswer" v-else-if="questionForm.type === 'TRUE_FALSE'">
            <el-radio label="正确">正确</el-radio>
            <el-radio label="错误">错误</el-radio>
          </el-radio-group>
          <!-- 填空题/简答题 -->
          <el-input v-else v-model="questionForm.correctAnswer" type="textarea" :rows="3" placeholder="请输入正确答案" />
        </el-form-item>

        <el-form-item label="解析">
          <el-input v-model="questionForm.explanation" type="textarea" :rows="2" placeholder="请输入解析（可选）" />
        </el-form-item>

        <!-- 评分标准（简答题） -->
        <el-form-item label="评分标准" v-if="questionForm.type === 'ESSAY'">
          <div class="scoring-criteria-editor">
            <div v-for="(criterion, index) in questionForm.scoringCriteria" :key="index" class="criterion-row">
              <el-input v-model="criterion.point" placeholder="评分点" class="criterion-point-input" />
              <el-input-number v-model="criterion.score" :min="0" :max="questionForm.score" placeholder="分值" />
              <el-button type="danger" @click="removeCriterion(index)" :icon="Delete" circle size="small" />
            </div>
            <el-button type="primary" @click="addCriterion" size="small">添加评分点</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- AI出题对话框 -->
    <el-dialog
      v-model="aiDialogVisible"
      title="AI 智能出题"
      width="1100px"
      top="3vh"
      @close="handleCloseAiDialog"
      class="ai-dialog base-dialog"
    >
      <div class="ai-dialog-content">
        <!-- 左侧：题目预览区域 -->
        <div class="ai-preview-panel">
          <div class="panel-header">
            <el-icon><Document /></el-icon>
            <span>题目预览</span>
            <el-badge :value="generatedQuestions.length" :hidden="generatedQuestions.length === 0" />
          </div>
          <div class="ai-preview">
            <el-empty v-if="generatedQuestions.length === 0 && !generating" description="等待AI生成题目..." :image-size="80" />
            <div v-else class="questions-preview">
              <el-card v-for="(q, index) in generatedQuestions" :key="index" class="question-preview-card">
                <div class="question-header">
                  <span class="question-number">第 {{ index + 1 }} 题</span>
                  <el-tag size="small">{{ getTypeName(q.type) }}</el-tag>
                  <el-tag size="small" type="info">{{ getDifficultyName(q.difficulty ?? 'MEDIUM') }}</el-tag>
                  <el-tag size="small" type="warning">{{ q.score ?? 10 }}分</el-tag>
                </div>
                <div class="question-preview-content" v-html="sanitizeHtml(q.content)"></div>
                <div v-if="q.options && q.options?.length > 0" class="preview-options">
                  <div v-for="opt in q.options" :key="opt.id" class="preview-option">
                    <span class="option-label">{{ opt.id }}.</span>
                    <span>{{ opt.text }}</span>
                  </div>
                </div>
                <div class="preview-answer">
                  <span class="answer-label">正确答案：</span>
                  <span v-if="Array.isArray(q.correctAnswer)">{{ q.correctAnswer.join(', ') }}</span>
                  <span v-else>{{ q.correctAnswer }}</span>
                </div>
                <div v-if="q.explanation" class="preview-explanation">
                  <span class="explanation-label">解析：</span>
                  <span v-html="sanitizeHtml(q.explanation)"></span>
                </div>
                <el-button type="primary" size="small" @click="handleEditAiQuestion(index)">编辑本题</el-button>
              </el-card>
            </div>
          </div>
        </div>

        <!-- 右侧：配置和思考过程 -->
        <div class="ai-config-panel">
          <el-form :model="aiForm" :rules="aiRules" ref="aiFormRef" label-width="80px" size="default">
            <div class="panel-header">
              <el-icon><Setting /></el-icon>
              <span>出题配置</span>
            </div>
            <el-form-item label="学科" prop="subject">
              <el-select v-model="aiForm.subject" placeholder="请选择学科" allow-create filterable class="full-width">
                <el-option v-for="subject in subjects" :key="subject" :label="subject" :value="subject" />
              </el-select>
            </el-form-item>
            <el-form-item label="类型" prop="type">
              <el-select v-model="aiForm.type" placeholder="请选择类型" class="full-width">
                <el-option label="单选题" value="SINGLE_CHOICE" />
                <el-option label="多选题" value="MULTIPLE_CHOICE" />
                <el-option label="判断题" value="TRUE_FALSE" />
                <el-option label="填空题" value="FILL_BLANK" />
                <el-option label="简答题" value="ESSAY" />
              </el-select>
            </el-form-item>
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="aiForm.difficulty" placeholder="请选择难度" class="full-width">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </el-form-item>
            <el-form-item label="数量" prop="count">
              <el-input-number v-model="aiForm.count" :min="1" :max="20" class="full-width" />
            </el-form-item>
            <el-form-item label="要求">
              <el-input
                v-model="aiForm.requirements"
                type="textarea"
                :rows="3"
                placeholder="额外要求（可选）"
              />
            </el-form-item>

            <!-- 操作按钮 -->
            <div class="ai-action-buttons">
              <el-button type="success" @click="handleGenerate" :loading="generating">
                <el-icon><MagicStick /></el-icon>
                {{ generating ? '生成中...' : '生成题目' }}
              </el-button>
              <el-button 
                class="save-all-btn"
                :disabled="generatedQuestions.length === 0" 
                @click="handleSaveAllQuestions" 
                :loading="saving"
              >
                <el-icon><Check /></el-icon>
                全部保存
              </el-button>
            </div>

            <!-- AI思考过程区域 -->
            <div class="panel-header thinking-header">
              <el-icon v-if="generating" class="is-loading"><MagicStick /></el-icon>
              <span>AI 思考过程</span>
            </div>
            <div class="ai-thinking-area">
              <el-card v-if="streamContent" class="thinking-card">
                <pre class="thinking-content">{{ streamContent }}</pre>
              </el-card>
              <el-empty v-else description="点击生成按钮查看" :image-size="50" />
            </div>
          </el-form>
        </div>
      </div>
      <template #footer>
        <div class="ai-footer">
          <el-button @click="aiDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { questionApi } from '@/api/question'
import { aiApi, type GenerateQuestionRequest, type GeneratedQuestion } from '@/api/ai'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, MagicStick, Check, Document, Setting } from '@element-plus/icons-vue'
import { getErrorMessage } from '@/utils/error'
import { sanitizeHtml, sanitizeAndTruncate } from '@/utils/sanitize'
import type { FormInstance, FormRules } from 'element-plus'
import type { Question, QuestionOption, ScoringCriterion } from '@/types'
import ActionButtons from '@/components/ActionButtons.vue'

const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const questions = ref<Question[]>([])
const subjects = ref<string[]>([])  // 学科列表
const viewDialogVisible = ref(false)
const editDialogVisible = ref(false)
const aiDialogVisible = ref(false)
const isEdit = ref(false)
const questionFormRef = ref<FormInstance>()
const aiFormRef = ref<FormInstance>()
const currentQuestion = ref<Question | null>(null)
const generating = ref(false)
const saving = ref(false)
const generatedQuestions = ref<GeneratedQuestion[]>([])
const streamContent = ref('')  // 流式生成的内容
const closeStream = ref<(() => void) | null>(null)  // 取消SSE连接的函数

const typeOptions = [
  { label: '全部', value: '' },
  { label: '单选题', value: 'SINGLE_CHOICE' },
  { label: '多选题', value: 'MULTIPLE_CHOICE' },
  { label: '判断题', value: 'TRUE_FALSE' },
  { label: '填空题', value: 'FILL_BLANK' },
  { label: '简答题', value: 'ESSAY' }
]

const difficultyOptions = [
  { label: '全部', value: '' },
  { label: '简单', value: 'EASY' },
  { label: '中等', value: 'MEDIUM' },
  { label: '困难', value: 'HARD' }
]

const searchForm = reactive({
  type: '',
  difficulty: '',
  subject: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const questionForm = reactive({
  id: 0,
  type: 'SINGLE_CHOICE',
  difficulty: 'MEDIUM',
  subject: '',
  content: '',
  score: 10,
  options: [] as QuestionOption[],
  correctAnswer: '' as string | string[],
  explanation: '',
  scoringCriteria: [] as ScoringCriterion[]
})

const aiForm = reactive({
  subject: '',
  type: 'SINGLE_CHOICE',
  difficulty: 'MEDIUM',
  count: 1,
  requirements: ''
})

const rules = reactive<FormRules>({
  type: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  subject: [{ required: true, message: '请输入学科', trigger: 'blur' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }],
  correctAnswer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }]
})

const aiRules = reactive<FormRules>({
  subject: [{ required: true, message: '请输入学科', trigger: 'blur' }],
  type: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  count: [{ required: true, message: '请输入数量', trigger: 'blur' }]
})

const showOptions = computed(() => {
  return ['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(questionForm.type)
})

function hasPermission(roles: string[]) {
  return roles.includes(authStore.user?.role || '')
}

function canEdit(question: Question) {
  if (authStore.user?.role === 'ADMIN') return true
  if (authStore.user?.role === 'TEACHER' && question.teacherId === authStore.user?.id) return true
  return false
}

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

function formatCorrectAnswer(answer: unknown): string {
  if (Array.isArray(answer)) {
    return answer.join(', ')
  }
  if (answer === null || answer === undefined) {
    return ''
  }
  return String(answer)
}

async function loadQuestions() {
  loading.value = true
  try {
    const res = await questionApi.page({
      current: pagination.current,
      size: pagination.size,
      type: searchForm.type || undefined,
      difficulty: searchForm.difficulty || undefined,
      subject: searchForm.subject || undefined
    })
    questions.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载题目失败')
  } finally {
    loading.value = false
  }
}

async function loadSubjects() {
  try {
    const res = await questionApi.getSubjects()
    subjects.value = res.data || []
  } catch {
    // 静默处理错误
  }
}

function filterTypeChange(value: string) {
  searchForm.type = value
  pagination.current = 1
  loadQuestions()
}

function filterDifficultyChange(value: string) {
  searchForm.difficulty = value
  pagination.current = 1
  loadQuestions()
}

function handleSubjectChange() {
  pagination.current = 1
  loadQuestions()
}

function handleReset() {
  searchForm.type = ''
  searchForm.difficulty = ''
  searchForm.subject = ''
  pagination.current = 1
  loadQuestions()
}

function handleView(row: Question) {
  currentQuestion.value = row
  viewDialogVisible.value = true
}

function handleCreate() {
  isEdit.value = false
  Object.assign(questionForm, {
    id: 0,
    type: 'SINGLE_CHOICE',
    difficulty: 'MEDIUM',
    subject: '',
    content: '',
    score: 10,
    options: [
      { id: 'A', text: '' },
      { id: 'B', text: '' },
      { id: 'C', text: '' },
      { id: 'D', text: '' }
    ],
    correctAnswer: '',
    explanation: '',
    scoringCriteria: []
  })
  editDialogVisible.value = true
}

function handleEdit(row: Question) {
  isEdit.value = true
  Object.assign(questionForm, {
    id: row.id,
    type: row.type,
    difficulty: row.difficulty,
    subject: row.subject,
    content: row.content,
    score: row.score,
    options: row.options ? [...row.options] : [],
    correctAnswer: row.correctAnswer || '',
    explanation: row.explanation || '',
    scoringCriteria: row.scoringCriteria ? [...row.scoringCriteria] : []
  })
  editDialogVisible.value = true
}

async function handleDelete(row: Question) {
  try {
    await ElMessageBox.confirm('确定要删除该题目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await questionApi.delete(row.id)
    ElMessage.success('删除成功')
    loadQuestions()
  } catch {
    // 取消删除
  }
}

function handleTypeChange() {
  // 切换类型时重置选项和答案
  if (['SINGLE_CHOICE', 'MULTIPLE_CHOICE'].includes(questionForm.type)) {
    questionForm.options = [
      { id: 'A', text: '' },
      { id: 'B', text: '' },
      { id: 'C', text: '' },
      { id: 'D', text: '' }
    ]
    questionForm.correctAnswer = questionForm.type === 'MULTIPLE_CHOICE' ? [] : ''
  } else if (questionForm.type === 'TRUE_FALSE') {
    questionForm.options = [
      { id: 'A', text: '正确' },
      { id: 'B', text: '错误' }
    ]
    questionForm.correctAnswer = ''
  } else {
    questionForm.options = []
    questionForm.correctAnswer = ''
  }
  questionForm.scoringCriteria = []
}

function addOption() {
  const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  const nextId = letters[questionForm.options.length] || String(questionForm.options.length + 1)
  questionForm.options.push({ id: nextId, text: '' })
}

function removeOption(index: number) {
  questionForm.options.splice(index, 1)
}

function addCriterion() {
  questionForm.scoringCriteria.push({ point: '', score: 0 })
}

function removeCriterion(index: number) {
  questionForm.scoringCriteria.splice(index, 1)
}

async function handleSubmit() {
  if (!questionFormRef.value) return

  await questionFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data: Record<string, unknown> = {
          type: questionForm.type,
          difficulty: questionForm.difficulty,
          subject: questionForm.subject,
          content: questionForm.content,
          score: questionForm.score,
          correctAnswer: questionForm.correctAnswer,
          explanation: questionForm.explanation
        }

        if (showOptions.value) {
          data.options = questionForm.options
        }

        if (questionForm.type === 'ESSAY' && questionForm.scoringCriteria.length > 0) {
          data.scoringCriteria = questionForm.scoringCriteria
        }

        if (isEdit.value) {
          await questionApi.update(questionForm.id, data)
          ElMessage.success('更新成功')
        } else {
          await questionApi.create(data)
          ElMessage.success('创建成功')
        }
        editDialogVisible.value = false
        loadQuestions()
      } catch (error: unknown) {
        ElMessage.error(getErrorMessage(error, '操作失败'))
      } finally {
        submitting.value = false
      }
    }
  })
}

// AI出题
async function handleAiGenerate() {
  aiDialogVisible.value = true
  generatedQuestions.value = []
  streamContent.value = ''

  // 检查用户是否配置了AI
  try {
    const response = await aiApi.getConfigs()
    if (!response || !response.data || response.data.length === 0) {
      ElMessage.warning('您还没有配置AI，请先在「个人中心」配置AI后再使用AI出题功能')
      aiDialogVisible.value = false
      return
    }
  } catch (error) {
    ElMessage.warning('检查AI配置失败，请确保已配置AI')
    aiDialogVisible.value = false
    return
  }
}

// 关闭AI对话框，取消SSE连接
function handleCloseAiDialog() {
  if (closeStream.value) {
    closeStream.value()
    closeStream.value = null
  }
  aiDialogVisible.value = false
}

async function handleGenerate() {
  if (!aiFormRef.value) return

  await aiFormRef.value.validate(async (valid) => {
    if (valid) {
      generating.value = true
      streamContent.value = ''
      generatedQuestions.value = []

      const request: GenerateQuestionRequest = {
        subject: aiForm.subject,
        type: aiForm.type,
        difficulty: aiForm.difficulty,
        count: aiForm.count,
        requirements: aiForm.requirements || undefined
      }

      // 使用流式API，并保存取消函数
      closeStream.value = aiApi.generateQuestionsStream(request, {
        onStart: () => {
          streamContent.value = '正在连接AI服务...\n'
        },
        onChunk: (content) => {
          streamContent.value += content
        },
        onComplete: (response) => {
          generating.value = false
          generatedQuestions.value = response.questions
          closeStream.value = null
          ElMessage.success(`AI已生成 ${response.questions.length} 道题目`)
        },
        onError: (error) => {
          generating.value = false
          closeStream.value = null
          ElMessage.error(error || 'AI生成失败，请检查AI配置是否正确')
        }
      })
    }
  })
}

function handleEditAiQuestion(index: number) {
  const q = generatedQuestions.value[index]
  if (!q) return
  aiDialogVisible.value = false

  // 将AI生成的题目数据填入编辑表单
  isEdit.value = true
  Object.assign(questionForm, {
    id: 0,
    type: q.type,
    difficulty: q.difficulty,
    subject: aiForm.subject,
    content: q.content,
    score: Number(q.score),
    options: q.options ? [...q.options] : [],
    correctAnswer: q.correctAnswer || '',
    explanation: q.explanation || '',
    scoringCriteria: []
  })

  editDialogVisible.value = true
}

async function handleSaveAllQuestions() {
  saving.value = true
  let successCount = 0
  let failCount = 0

  for (const q of generatedQuestions.value) {
    try {
      const data: Record<string, unknown> = {
        type: q.type,
        difficulty: q.difficulty || aiForm.difficulty,
        subject: aiForm.subject,
        content: q.content,
        score: Number(q.score ?? 10),
        correctAnswer: q.correctAnswer,
        explanation: q.explanation
      }

      if (q.options && q.options.length > 0) {
        data.options = q.options
      }

      await questionApi.create(data)
      successCount++
    } catch (error) {
      failCount++
      // 题目保存失败，继续处理下一题
    }
  }

  if (successCount > 0) {
    ElMessage.success(`成功保存 ${successCount} 道题目`)
    loadQuestions()
    aiDialogVisible.value = false
  }

  if (failCount > 0) {
    ElMessage.warning(`${failCount} 道题目保存失败`)
  }

  saving.value = false
}

onMounted(() => {
  loadQuestions()
  loadSubjects()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;
@use '@/styles/views/base-list.scss';

.question-list {
  .header-actions {
    display: flex;
    gap: $spacing-md;
  }

  .full-width {
    width: 100%;
  }

  .table-card {
    .question-content {
      white-space: pre-wrap;
      word-break: break-all;
      color: $text-secondary;
    }
  }

  .option-item {
    padding: $spacing-sm 0;
    .option-label {
      font-weight: $font-weight-medium;
      margin-right: $spacing-md;
      color: $text-primary;
    }
  }

  .options-editor {
    .option-id-input {
      width: 60px;
    }

    .option-text-input {
      flex: 1;
    }

    .option-row {
      display: flex;
      gap: $spacing-md;
      margin-bottom: $spacing-md;
      align-items: center;
    }
  }

  .scoring-criteria-editor {
    .criterion-point-input {
      flex: 1;
    }

    .criterion-row {
      display: flex;
      gap: $spacing-md;
      margin-bottom: $spacing-md;
      align-items: center;
    }
  }

  .criterion-item {
    padding: $spacing-sm 0;
    color: $text-secondary;
  }

  // AI 对话框左右布局
  .ai-dialog-content {
    display: flex;
    gap: $spacing-xl;
    min-height: 600px;
    max-height: 70vh;

    // 左侧预览面板
    .ai-preview-panel {
      flex: 1.2;
      display: flex;
      flex-direction: column;
      border: 1px solid $border-color;
      border-radius: $radius-md;
      background: $bg-secondary;
      overflow: hidden;

      .panel-header {
        display: flex;
        align-items: center;
        gap: $spacing-sm;
        padding: $spacing-md $spacing-lg;
        background: $bg-primary;
        border-bottom: 1px solid $border-color;
        font-weight: $font-weight-medium;
        color: $text-primary;
      }

      .ai-preview {
        flex: 1;
        overflow-y: auto;
        padding: $spacing-md;

        .questions-preview {
          display: flex;
          flex-direction: column;
          gap: $spacing-md;

          .question-preview-card {
            border: 1px solid $border-color;
            border-radius: $radius-md;
            padding: $spacing-md;
            background: $bg-primary;

            .question-header {
              display: flex;
              gap: $spacing-xs;
              align-items: center;
              margin-bottom: $spacing-sm;
              flex-wrap: wrap;

              .question-number {
                font-weight: $font-weight-medium;
                color: $text-primary;
              }
            }

            .question-preview-content {
              margin-bottom: $spacing-sm;
              line-height: $line-height-relaxed;
              color: $text-secondary;
              font-size: $font-size-sm;
            }

            .preview-options {
              margin-bottom: $spacing-sm;

              .preview-option {
                padding: $spacing-xs 0;
                color: $text-secondary;
                font-size: $font-size-sm;

                .option-label {
                  font-weight: $font-weight-medium;
                  margin-right: $spacing-sm;
                  color: $text-tertiary;
                }
              }
            }

            .preview-answer {
              margin-bottom: $spacing-sm;
              padding: $spacing-sm;
              background: $bg-hover;
              border-radius: $radius-sm;
              font-size: $font-size-sm;

              .answer-label {
                font-weight: $font-weight-medium;
                color: $text-tertiary;
                margin-right: $spacing-sm;
              }
            }

            .preview-explanation {
              padding: $spacing-sm;
              background: $bg-secondary;
              border-radius: $radius-sm;
              font-size: $font-size-sm;
              margin-bottom: $spacing-sm;

              .explanation-label {
                font-weight: $font-weight-medium;
                color: $text-tertiary;
                margin-right: $spacing-sm;
              }
            }
          }
        }
      }
    }

    // 右侧配置面板
    .ai-config-panel {
      flex: 0.8;
      display: flex;
      flex-direction: column;
      border: 1px solid $border-color;
      border-radius: $radius-md;
      background: $bg-primary;
      padding: $spacing-lg;
      overflow-y: auto;

      .panel-header {
        display: flex;
        align-items: center;
        gap: $spacing-sm;
        padding-bottom: $spacing-md;
        margin-bottom: $spacing-md;
        border-bottom: 1px solid $border-light;
        font-weight: $font-weight-medium;
        color: $text-primary;
      }

      .ai-action-buttons {
        display: flex;
        flex-direction: row;
        gap: $spacing-sm;
        margin-top: $spacing-md;
        margin-bottom: $spacing-lg;

        .el-button {
          flex: 1;
        }

        .save-all-btn {
          background: #fff;
          color: #333;
          border: 2px solid #333;
          font-weight: 600;
          box-shadow: none;
          transition: all 0.15s ease;

          &:hover:not(:disabled) {
            background: #fff;
            color: #333;
            border-color: #333;
            box-shadow: none;
            transform: translate(-1px, -1px);
          }

          &:active:not(:disabled) {
            box-shadow: none;
            transform: translate(2px, 2px);
          }

          &:disabled {
            background: #f5f5f5;
            color: #ccc;
            border-color: #ddd;
            box-shadow: none;
            cursor: not-allowed;
          }
        }
      }

      .thinking-header {
        margin-top: $spacing-lg;
      }

      .ai-thinking-area {
        flex: 1;
        min-height: 150px;
        max-height: 200px;
        overflow-y: auto;

        .thinking-card {
          background: $bg-secondary;
          border: 1px solid $border-color;
          border-radius: $radius-md;

          .thinking-content {
            max-height: 180px;
            overflow-y: auto;
            background: $gray-900;
            color: $gray-400;
            padding: $spacing-md;
            border-radius: $radius-sm;
            font-family: 'Courier New', Consolas, monospace;
            font-size: $font-size-xs;
            line-height: $line-height-normal;
            white-space: pre-wrap;
            word-wrap: break-word;
            margin: 0;
          }
        }
      }
    }
  }

  .ai-footer {
    display: flex;
    gap: $spacing-md;
    justify-content: flex-end;
  }
}

// 响应式
@media (max-width: $breakpoint-md) {
  .question-list {
    .header-actions {
      width: 100%;
    }
  }
}

// AI对话框响应式
@media (max-width: 1200px) {
  :deep(.ai-dialog) {
    .el-dialog {
      width: 95% !important;
    }
  }

  .ai-dialog-content {
    flex-direction: column;
    min-height: 500px;

    .ai-preview-panel,
    .ai-config-panel {
      flex: none;
    }

    .ai-preview-panel {
      max-height: 300px;
    }
  }
}
</style>
