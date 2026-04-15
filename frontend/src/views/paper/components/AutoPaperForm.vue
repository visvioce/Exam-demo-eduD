<template>
  <div class="auto-paper-form">
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

      <el-divider content-position="left">题目配置</el-divider>

      <div class="table-wrapper">
        <el-table :data="rows" stripe border class="config-table" table-layout="fixed" :fit="true">
          <el-table-column label="题型" min-width="116">
            <template #default="{ row }">
              <el-select v-model="row.type" placeholder="题型" class="full-select" size="small">
                <el-option label="单选题" value="SINGLE_CHOICE" />
                <el-option label="多选题" value="MULTIPLE_CHOICE" />
                <el-option label="判断题" value="TRUE_FALSE" />
                <el-option label="填空题" value="FILL_BLANK" />
                <el-option label="简答题" value="ESSAY" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="学科" min-width="152">
            <template #default="{ row }">
              <el-select v-model="row.subject" placeholder="学科" clearable allow-create filterable class="full-select" size="small">
                <el-option v-for="subject in subjects" :key="subject" :label="subject" :value="subject" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="难度" min-width="96">
            <template #default="{ row }">
              <el-select v-model="row.difficulty" placeholder="难度" clearable class="full-select" size="small">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="题数" min-width="92">
            <template #default="{ row }">
              <el-input-number v-model="row.count" :min="1" :max="100" controls-position="right" size="small" class="num-input" />
            </template>
          </el-table-column>
          <el-table-column label="分值" min-width="92">
            <template #default="{ row }">
              <el-input-number v-model="row.score" :min="1" :max="100" controls-position="right" size="small" class="num-input" />
            </template>
          </el-table-column>
          <el-table-column label="小计" min-width="72" align="center">
            <template #default="{ row }">
              <span class="subtotal">{{ row.count * row.score }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="68" align="center">
            <template #default="{ $index }">
              <DeleteActionButton aria-label="删除配置行" button-class="row-delete-btn" @click="removeRow($index)" />
            </template>
          </el-table-column>
          <template #empty>
            <div class="table-empty">
              <p>暂无配置，点击下方添加按钮添加</p>
            </div>
          </template>
        </el-table>
      </div>

      <el-button plain @click="addRow" class="add-row-btn">
        <el-icon><Plus /></el-icon>
        添加配置行
      </el-button>

      <div class="summary-card">
        <div class="summary-item">
          <span>总题数：</span>
          <span class="highlight">{{ totalQuestions }} 题</span>
        </div>
        <div class="summary-item">
          <span>总分：</span>
          <span class="highlight">{{ totalScore }} 分</span>
        </div>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Course } from '@/types'
import DeleteActionButton from '@/components/DeleteActionButton.vue'

interface PaperRow {
  type: string
  subject: string
  difficulty: string
  count: number
  score: number
}

interface SubmitData {
  name: string
  description: string
  courseId: number | null
  configs: PaperRow[]
  // 兼容后端的数据结构
  singleChoice: any
  multipleChoice: any
  trueFalse: any
  fillBlank: any
  essay: any
}

const props = defineProps<{
  courses: Course[]
  subjects: string[]
}>()

const emit = defineEmits<{
  (e: 'submit', data: SubmitData): void
}>()

const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  description: '',
  courseId: null as number | null
})

const rows = ref<PaperRow[]>([
  { type: '', subject: '', difficulty: '', count: 5, score: 2 }
])

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }]
})

const totalQuestions = computed(() => {
  return rows.value.reduce((sum, row) => sum + (row.type ? row.count : 0), 0)
})

const totalScore = computed(() => {
  return rows.value.reduce((sum, row) => sum + (row.type ? row.count * row.score : 0), 0)
})

function addRow() {
  rows.value.push({ type: '', subject: '', difficulty: '', count: 5, score: 2 })
}

function removeRow(index: number) {
  rows.value.splice(index, 1)
}

function handleSubmit() {
  if (!formRef.value) return

  formRef.value.validate(async (valid) => {
    if (valid) {
      // 过滤掉空行
      const validRows = rows.value.filter(row => row.type && row.count > 0)

      if (validRows.length === 0) {
        ElMessage.warning('请至少添加一行题目配置')
        return
      }

      // 转换成后端数据格式（保持兼容性）
      const singleChoice: any = validRows.find(r => r.type === 'SINGLE_CHOICE')
      const multipleChoice: any = validRows.find(r => r.type === 'MULTIPLE_CHOICE')
      const trueFalse: any = validRows.find(r => r.type === 'TRUE_FALSE')
      const fillBlank: any = validRows.find(r => r.type === 'FILL_BLANK')
      const essay: any = validRows.find(r => r.type === 'ESSAY')

      const data: SubmitData = {
        ...form,
        configs: validRows,
        singleChoice,
        multipleChoice,
        trueFalse,
        fillBlank,
        essay
      }

      emit('submit', data)
    }
  })
}

defineExpose({
  handleSubmit
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.auto-paper-form {
  :deep(.el-form-item__label) {
    color: $text-secondary;
    font-weight: $font-weight-medium;
  }

  .full-width {
    width: 100%;
  }

  .full-select {
    width: 100%;
  }

  .num-input {
    width: 100%;
  }

  .table-wrapper {
    margin-top: $spacing-sm;
    width: 100%;
    border-radius: 12px;
    background: $bg-primary;
    overflow: hidden;
  }

  .config-table {
    width: 100%;
    --el-table-border-color: #{$border-color};
    --el-table-header-bg-color: #{$bg-secondary};
    --el-table-row-hover-bg-color: #{$bg-hover};

    :deep(.el-table__inner-wrapper::before) {
      display: none;
    }

    :deep(.el-table__header-wrapper),
    :deep(.el-table__body-wrapper) {
      width: 100%;
    }

    :deep(.el-table__header th) {
      height: 42px;
      color: $text-tertiary;
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 0.2px;
      border-bottom-color: $border-color;

      .cell {
        white-space: nowrap;
      }
    }

    :deep(.el-table__body td) {
      padding: 10px 0;
      border-bottom-color: $border-light;
    }

    :deep(.el-table .cell) {
      padding: 0 10px;
    }

    :deep(.el-select) {
      .el-input__wrapper {
        border-radius: 8px;
        background: $bg-primary;
        box-shadow: 0 0 0 1px $border-color inset;

        &:hover {
          box-shadow: 0 0 0 1px #{$text-quaternary} inset;
        }

        &.is-focus {
          box-shadow: 0 0 0 1px #{$text-tertiary} inset;
        }
      }
    }

    :deep(.el-input-number) {
      width: 100%;

      .el-input__wrapper {
        padding: 0 8px;
        border-radius: 8px;
        box-shadow: 0 0 0 1px $border-color inset;
      }

      .el-input-number__increase,
      .el-input-number__decrease {
        background: transparent;
        border-left-color: $border-light;
      }
    }

    .subtotal {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 34px;
      height: 26px;
      padding: 0 $spacing-sm;
      font-weight: 600;
      color: $text-primary;
      font-size: 12px;
      border: 1px solid $border-color;
      border-radius: 999px;
      background: $bg-hover;
    }

  }

  .table-empty {
    padding: $spacing-xl;
    text-align: center;
    color: $text-tertiary;

    p {
      margin: 0;
    }
  }

  .add-row-btn {
    width: 100%;
    margin-top: 15px;
    border-color: $border-color;
    color: $text-secondary;
    border-radius: 10px;
    height: 38px;

    &:hover {
      color: $text-primary;
      border-color: $text-quaternary;
      background: $bg-hover;
    }
  }

  .summary-card {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-lg;
    margin-top: $spacing-md;
    padding: $spacing-lg;
    background: $bg-secondary;
    border: 1px solid $border-color;
    border-radius: 12px;

    .summary-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: $font-size-sm;
      color: $text-tertiary;

      .highlight {
        font-weight: 600;
        color: $text-primary;
        font-size: 18px;
      }
    }
  }

  @media (max-width: 768px) {
    .summary-card {
      justify-content: space-between;
      padding: $spacing-md;
    }
  }
}
</style>
