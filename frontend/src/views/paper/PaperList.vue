<template>
  <div class="paper-list base-list-page">
    <div class="page-header">
      <h2>试卷管理</h2>
      <el-button type="primary" @click="handleCreate" v-if="hasPermission(['ADMIN', 'TEACHER'])">
        <el-icon><Plus /></el-icon>
        创建试卷
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px" class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="试卷名称" clearable @input="handleKeywordInput" class="keyword-input" />
        </el-form-item>
        <el-form-item label="组卷方式">
          <div class="filter-tabs">
            <button
              type="button"
              v-for="item in typeOptions" 
              :key="item.value"
              :class="['tab-item', { active: searchForm.type === item.value }]"
              :aria-pressed="searchForm.type === item.value"
              @click="handleTypeChange(item.value)"
            >
              {{ item.label }}
            </button>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <div class="filter-tabs">
            <button
              type="button"
              v-for="item in statusOptions" 
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

    <!-- 试卷列表 -->
    <el-card class="table-card">
      <el-table :data="papers" v-loading="loading" stripe table-layout="auto" :fit="true">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="试卷名称" min-width="200" />
        <el-table-column prop="description" label="描述" min-width="150">
          <template #default="{ row }">
            {{ row.description || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程" min-width="120" />
        <el-table-column prop="totalScore" label="总分" min-width="88" />
        <el-table-column prop="type" label="组卷方式" min-width="108">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="120">
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
          @size-change="loadPapers"
          @current-change="loadPapers"
        />
      </div>
    </el-card>

    <!-- 查看试卷对话框 -->
    <el-dialog v-model="viewDialogVisible" title="试卷详情" width="95%" top="5vh" class="base-dialog">
      <el-descriptions :column="2" border v-if="currentPaper">
        <el-descriptions-item label="试卷名称">{{ currentPaper.name }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ currentPaper.totalScore }}</el-descriptions-item>
        <el-descriptions-item label="组卷方式">{{ getTypeName(currentPaper.type) }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentPaper.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <h4 class="question-list-title">题目列表（共 {{ currentPaper?.questions?.length || 0 }} 题）</h4>

      <div v-if="currentPaper && currentPaper.questions" class="questions-detail-list">
        <el-card v-for="(pq, index) in currentPaper.questions" :key="index" class="question-detail-card">
          <div class="question-header">
            <div class="question-meta">
              <span class="question-number">第 {{ index + 1 }} 题</span>
              <el-tag :type="getTypeColor(getQuestionById(pq.questionId)?.type || '')" size="small">
                {{ getTypeName(getQuestionById(pq.questionId)?.type || '') }}
              </el-tag>
              <el-tag :type="getDifficultyColor(getQuestionById(pq.questionId)?.difficulty || '')" size="small">
                {{ getDifficultyName(getQuestionById(pq.questionId)?.difficulty || '') }}
              </el-tag>
              <span class="question-score-display">{{ pq.score }} 分</span>
            </div>
          </div>

          <div class="question-content-display">
            {{ getQuestionById(pq.questionId)?.content }}
          </div>

          <!-- 显示选项 -->
          <div v-if="getQuestionById(pq.questionId)?.options" class="options-display">
            <div v-for="opt in getQuestionById(pq.questionId)?.options" :key="opt.id" class="option-display-item">
              <span class="option-label">{{ opt.id }}.</span>
              <span>{{ opt.text }}</span>
            </div>
          </div>

          <!-- 显示正确答案 -->
          <div class="answer-display">
            <span class="answer-label">正确答案：</span>
            <span class="answer-value">
              {{ getQuestionAnswerDisplay(pq.questionId) }}
            </span>
          </div>

          <!-- 显示解析 -->
          <div v-if="getQuestionById(pq.questionId)?.explanation" class="explanation-display">
            <span class="explanation-label">解析：</span>
            <span>{{ getQuestionById(pq.questionId)?.explanation }}</span>
          </div>
        </el-card>
      </div>

      <el-empty v-else description="暂无题目" />
    </el-dialog>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑试卷' : '创建试卷'" width="800px" top="5vh" class="base-dialog">
      <el-tabs v-model="activeTab" v-if="!isEdit">
        <el-tab-pane label="手动组卷" name="manual">
          <ManualPaperForm
            ref="manualFormRef"
            :courses="courses"
            :questions="availableQuestions"
            @submit="handleManualSubmit"
          />
        </el-tab-pane>
        <el-tab-pane label="自动组卷" name="auto">
          <AutoPaperForm
            ref="autoFormRef"
            :courses="courses"
            :subjects="subjects"
            @submit="handleAutoSubmit"
          />
        </el-tab-pane>
      </el-tabs>

      <ManualPaperForm
        v-else
        ref="manualFormRef"
        :courses="courses"
        :questions="availableQuestions"
        :initial-data="editFormData"
        @submit="handleManualSubmit"
      />
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCurrentTabSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { paperApi } from '@/api/paper'
import { questionApi } from '@/api/question'
import { courseApi } from '@/api/course'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getErrorMessage } from '@/utils/error'
import { usePagedList } from '@/composables/usePagedList'
import type { Paper, Question, Course } from '@/types'
import ActionButtons from '@/components/ActionButtons.vue'
import ManualPaperForm from './components/ManualPaperForm.vue'
import AutoPaperForm from './components/AutoPaperForm.vue'

const authStore = useAuthStore()

const submitting = ref(false)
const courses = ref<Course[]>([])
const questions = ref<Question[]>([])
const subjects = ref<string[]>([])
const viewDialogVisible = ref(false)
const editDialogVisible = ref(false)
const isEdit = ref(false)
const currentPaper = ref<Paper | null>(null)
const activeTab = ref<'manual' | 'auto'>('manual')
const manualFormRef = ref()
const autoFormRef = ref()
const editFormData = ref<Paper | null>(null)

const typeOptions = [
  { label: '全部', value: '' },
  { label: '手动组卷', value: 'MANUAL' },
  { label: '自动组卷', value: 'AUTO' }
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已归档', value: 'ARCHIVED' }
]

const {
  records: papers,
  loading,
  searchForm,
  pagination,
  load: loadPapers,
  loadFromFirstPage,
  resetSearch,
  toggleSearch
} = usePagedList<Paper, { keyword: string; type: string; status: string }>({
  createSearchForm: () => ({
    keyword: '',
    type: '',
    status: ''
  }),
  fetchPage: async ({ current, size, keyword, type, status }) => {
    const res = await paperApi.page({
      current,
      size,
      keyword: keyword || undefined,
      type: type || undefined,
      status: status || undefined
    })
    return {
      records: res.data.records,
      total: res.data.total
    }
  },
  onError: () => {
    ElMessage.error('加载试卷失败')
  }
})

const availableQuestions = computed(() => questions.value)

function hasPermission(roles: string[]) {
  return roles.includes(authStore.user?.role || '')
}

function canEdit(paper: Paper) {
  const user = authStore.user
  if (!user) return false
  return (user.role === 'ADMIN' || user.role === 'TEACHER') && paper.teacherId === user.id
}

function getTypeName(type: string) {
  const map: Record<string, string> = {
    MANUAL: '手动组卷',
    AUTO: '自动组卷',
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
    MANUAL: 'primary',
    AUTO: 'success',
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

function getStatusName(status: string) {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    ARCHIVED: '已归档'
  }
  return map[status] || status
}

function getStatusColor(status: string) {
  const map: Record<string, string> = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    ARCHIVED: 'warning'
  }
  return map[status] || ''
}

function getQuestionById(questionId: number | undefined): Question | undefined {
  if (!questionId) return undefined
  return questions.value.find(item => item.id === questionId)
}

function getQuestionAnswerDisplay(questionId: number | undefined): string {
  const question = getQuestionById(questionId)
  if (!question || question.correctAnswer === undefined || question.correctAnswer === null) {
    return '-'
  }
  if (Array.isArray(question.correctAnswer)) {
    return question.correctAnswer.join(', ')
  }
  return String(question.correctAnswer)
}

async function loadCourses() {
  try {
    const res = await courseApi.list()
    courses.value = res.data
  } catch (error) {
    ElMessage.error('加载课程失败')
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

async function loadSubjects() {
  try {
    const res = await questionApi.getSubjects()
    subjects.value = res.data || []
  } catch {
    // 静默处理
  }
}

function handleKeywordInput() {
  void loadFromFirstPage()
}

function handleTypeChange(value: string) {
  void toggleSearch('type', value, '')
}

function handleStatusChange(value: string) {
  void toggleSearch('status', value, '')
}

function handleReset() {
  void resetSearch()
}

function handleView(row: Paper) {
  currentPaper.value = row
  viewDialogVisible.value = true
}

function handleCreate() {
  isEdit.value = false
  activeTab.value = 'manual'
  editFormData.value = null
  editDialogVisible.value = true
}

function handleEdit(row: Paper) {
  isEdit.value = true
  editFormData.value = { ...row }
  editDialogVisible.value = true
}

async function handleDelete(row: Paper) {
  try {
    await ElMessageBox.confirm('确定要删除该试卷吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await paperApi.delete(row.id)
    ElMessage.success('删除成功')
    void loadPapers()
  } catch {
    // 取消删除
  }
}

async function handleManualSubmit(data: any) {
  submitting.value = true
  try {
    if (isEdit.value) {
      await paperApi.update(editFormData.value!.id, data)
      ElMessage.success('更新成功')
    } else {
      await paperApi.create(data)
      ElMessage.success('创建成功')
    }
    editDialogVisible.value = false
    void loadPapers()
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, '操作失败'))
  } finally {
    submitting.value = false
  }
}

async function handleAutoSubmit(data: any) {
  submitting.value = true
  try {
    await paperApi.autoGenerate(data)
    ElMessage.success('自动组卷成功')
    editDialogVisible.value = false
    void loadPapers()
  } catch (error: unknown) {
    ElMessage.error(getErrorMessage(error, '自动组卷失败'))
  } finally {
    submitting.value = false
  }
}

function handleCurrentTabSubmit() {
  if (isEdit.value) {
    manualFormRef.value?.handleSubmit()
  } else if (activeTab.value === 'manual') {
    manualFormRef.value?.handleSubmit()
  } else {
    autoFormRef.value?.handleSubmit()
  }
}

onMounted(() => {
  void loadPapers()
  loadCourses()
  loadQuestions()
  loadSubjects()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;
@use '@/styles/views/base-list.scss';

.paper-list {
  .search-card {
    .search-form {
      :deep(.el-form-item) {
        display: flex;
        align-items: center;
        margin-bottom: $spacing-md;
      }

      :deep(.el-form-item__label) {
        white-space: nowrap;
        padding-right: $spacing-lg;
        color: $text-secondary;
        font-size: $font-size-sm;
      }

      :deep(.el-form-item__content) {
        flex: 1;
        display: flex;
        align-items: center;
      }
    }

    .keyword-input {
      width: 200px;
    }
  }

  .question-list-title {
    margin-top: $spacing-xl;
    margin-bottom: $spacing-sm;
  }

  .table-card {
    margin-bottom: $spacing-xl;

    :deep(.el-table) {
      .el-table__header th {
        background: $bg-secondary;
        font-weight: $font-weight-medium;
      }
    }

  }

  .questions-detail-list {
    .question-detail-card {
      margin-bottom: $spacing-lg;
      border: 1px solid $border-color;

      .question-header {
        .question-meta {
          display: flex;
          align-items: center;
          gap: $spacing-sm;
          margin-bottom: $spacing-md;

          .question-number {
            font-weight: $font-weight-medium;
            font-size: $font-size-lg;
            color: $text-primary;
          }

          .question-score-display {
            font-weight: $font-weight-medium;
            color: $text-tertiary;
            margin-left: auto;
          }
        }
      }

      .question-content-display {
        font-size: $font-size-base;
        line-height: $line-height-relaxed;
        margin-bottom: $spacing-lg;
        padding: $spacing-md;
        background: $bg-secondary;
        border-radius: $radius-sm;
      }

      .options-display {
        margin-bottom: $spacing-md;
        padding: $spacing-md;
        background: $bg-hover;
        border-radius: $radius-sm;

        .option-display-item {
          padding: $spacing-sm 0;
          font-size: $font-size-sm;
        }
      }

      .answer-display {
        padding: $spacing-md;
        background: rgba(103, 194, 58, 0.1);
        border-radius: $radius-sm;
        margin-bottom: $spacing-md;

        .answer-label {
          font-weight: $font-weight-medium;
          color: $text-primary;
        }

        .answer-value {
          font-weight: $font-weight-medium;
          color: $black;
        }
      }

      .explanation-display {
        padding: $spacing-md;
        background: $bg-secondary;
        border-radius: $radius-sm;
        font-size: $font-size-sm;
        color: $text-secondary;

        .explanation-label {
          font-weight: $font-weight-medium;
          color: $text-primary;
          margin-right: $spacing-sm;
        }
      }
    }
  }
}
</style>
