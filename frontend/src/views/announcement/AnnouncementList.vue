<template>
  <div class="announcement-list">
    <div class="page-header">
      <h2>公告管理</h2>
      <el-button type="primary" @click="handleCreate" v-if="hasPermission(['ADMIN', 'TEACHER'])">
        <el-icon><Plus /></el-icon>
        发布公告
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="公告标题" clearable @input="handleKeywordInput" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="类型">
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
        <el-form-item label="状态">
          <div class="filter-tabs">
            <span 
              v-for="item in statusOptions" 
              :key="item.value"
              :class="['tab-item', { active: searchForm.status === item.value }]"
              @click="filterStatusChange(item.value)"
            >
              {{ item.label }}
            </span>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 公告列表 -->
    <el-card class="table-card">
      <el-table :data="announcements" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityColor(row.priority)" size="small">
              {{ getPriorityName(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布人" width="120">
          <template #default="{ row }">
            {{ getPublisherDisplayName(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="publishedAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.publishedAt) }}
          </template>
        </el-table-column>
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
          @size-change="loadAnnouncements"
          @current-change="loadAnnouncements"
        />
      </div>
    </el-card>

    <!-- 查看公告对话框 -->
    <el-dialog v-model="viewDialogVisible" title="公告详情" width="700px">
      <el-descriptions :column="2" border v-if="currentAnnouncement">
        <el-descriptions-item label="标题" :span="2">{{ currentAnnouncement.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ getTypeName(currentAnnouncement.type) }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ getPriorityName(currentAnnouncement.priority) }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ getPublisherDisplayName(currentAnnouncement) }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ formatDate(currentAnnouncement.publishedAt) }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <div v-html="sanitizeHtml(currentAnnouncement.content)" class="announcement-content"></div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="700px">
      <el-form :model="announcementForm" :rules="rules" ref="announcementFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="announcementForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="announcementForm.type" placeholder="请选择类型" class="full-width">
                <el-option label="系统公告" value="SYSTEM" />
                <el-option label="考试公告" value="EXAM" />
                <el-option label="课程公告" value="COURSE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="announcementForm.priority" placeholder="请选择优先级" class="full-width">
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="announcementForm.status">
            <el-radio label="DRAFT">保存为草稿</el-radio>
            <el-radio label="PUBLISHED">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="announcementForm.content" type="textarea" :rows="8" placeholder="请输入公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { announcementApi } from '@/api/announcement'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/format'
import { getErrorMessage } from '@/utils/error'
import { sanitizeHtml } from '@/utils/sanitize'
import type { FormInstance, FormRules } from 'element-plus'
import type { Announcement } from '@/types'
import ActionButtons from '@/components/ActionButtons.vue'

const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const announcements = ref<Announcement[]>([])
const viewDialogVisible = ref(false)
const editDialogVisible = ref(false)
const isEdit = ref(false)
const announcementFormRef = ref<FormInstance>()
const currentAnnouncement = ref<Announcement | null>(null)

const typeOptions = [
  { label: '全部', value: '' },
  { label: '系统公告', value: 'SYSTEM' },
  { label: '考试公告', value: 'EXAM' },
  { label: '课程公告', value: 'COURSE' }
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' }
]

const searchForm = reactive({
  keyword: '',
  type: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const announcementForm = reactive({
  id: 0,
  title: '',
  content: '',
  type: 'SYSTEM',
  priority: 'MEDIUM',
  status: 'PUBLISHED'
})

const rules = reactive<FormRules>({
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
})

function hasPermission(roles: string[]) {
  return roles.includes(authStore.user?.role || '')
}

function canEdit(announcement: Announcement) {
  if (authStore.user?.role === 'ADMIN') return true
  if (authStore.user?.role === 'TEACHER' && announcement.publisherId === authStore.user?.id) return true
  return false
}

function getTypeName(type: string) {
  const map: Record<string, string> = {
    SYSTEM: '系统公告',
    EXAM: '考试公告',
    COURSE: '课程公告'
  }
  return map[type] || type
}

function getTypeColor(type: string) {
  const map: Record<string, string> = {
    SYSTEM: 'danger',
    EXAM: 'warning',
    COURSE: 'success'
  }
  return map[type] || ''
}

function getPriorityName(priority?: string) {
  if (!priority) return '中'
  const map: Record<string, string> = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高'
  }
  return map[priority] || priority
}

function getPriorityColor(priority?: string) {
  if (!priority) return 'info'
  const map: Record<string, string> = {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'danger'
  }
  return map[priority] || ''
}

function getPublisherDisplayName(announcement: Announcement | null): string {
  if (!announcement) return '-'
  if (announcement.publisherName && announcement.publisherName.trim()) {
    return announcement.publisherName
  }
  if (announcement.publisherId) {
    return `用户#${announcement.publisherId}`
  }
  return '-'
}

async function loadAnnouncements() {
  loading.value = true
  try {
    const res = await announcementApi.page({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status || undefined,
      type: searchForm.type || undefined
    })
    announcements.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('加载公告失败')
  } finally {
    loading.value = false
  }
}

function handleKeywordInput() {
  pagination.current = 1
  loadAnnouncements()
}

function filterTypeChange(value: string) {
  void value
  pagination.current = 1
  loadAnnouncements()
}

function filterStatusChange(value: string) {
  void value
  pagination.current = 1
  loadAnnouncements()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.type = ''
  searchForm.status = ''
  pagination.current = 1
  loadAnnouncements()
}

function handleView(row: Announcement) {
  currentAnnouncement.value = row
  viewDialogVisible.value = true
}

function handleCreate() {
  isEdit.value = false
  Object.assign(announcementForm, {
    id: 0,
    title: '',
    content: '',
    type: 'SYSTEM',
    priority: 'MEDIUM',
    status: 'PUBLISHED'
  })
  editDialogVisible.value = true
}

function handleEdit(row: Announcement) {
  isEdit.value = true
  Object.assign(announcementForm, {
    id: row.id,
    title: row.title,
    content: row.content,
    type: row.type,
    priority: row.priority || 'MEDIUM',
    status: row.status || 'DRAFT'
  })
  editDialogVisible.value = true
}

async function handleDelete(row: Announcement) {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await announcementApi.delete(row.id)
    ElMessage.success('删除成功')
    loadAnnouncements()
  } catch {
    // 取消删除
  }
}

async function handleSubmit() {
  if (!announcementFormRef.value) return

  await announcementFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = {
          title: announcementForm.title,
          content: announcementForm.content,
          type: announcementForm.type as 'SYSTEM' | 'EXAM' | 'COURSE',
          priority: announcementForm.priority as 'LOW' | 'MEDIUM' | 'HIGH',
          status: announcementForm.status as 'DRAFT' | 'PUBLISHED'
        }

        if (isEdit.value) {
          await announcementApi.update(announcementForm.id, data)
          ElMessage.success('更新成功')
        } else {
          await announcementApi.create(data)
          ElMessage.success('发布成功')
        }
        editDialogVisible.value = false
        loadAnnouncements()
      } catch (error: unknown) {
        ElMessage.error(getErrorMessage(error, '操作失败'))
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.announcement-list {
  padding: $spacing-xl;

  .full-width {
    width: 100%;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-xl;

    h2 {
      margin: 0;
      font-size: $font-size-3xl;
      font-weight: $font-weight-medium;
      color: $text-primary;
      letter-spacing: -0.5px;
    }
  }

  .search-card {
    margin-bottom: $spacing-xl;
    border: 1px solid $border-color;
    border-radius: $radius-md;
    background: $bg-primary;

    .filter-tabs {
      display: flex;
      gap: $spacing-lg;

      .tab-item {
        cursor: pointer;
        color: $text-tertiary;
        font-size: $font-size-sm;
        padding: $spacing-xs 0;
        position: relative;
        transition: color 0.2s ease;
        user-select: none;

        &:hover {
          color: $text-secondary;
        }

        &.active {
          color: $text-primary;
          font-weight: 500;

          &::after {
            content: '';
            position: absolute;
            bottom: -2px;
            left: 0;
            right: 0;
            height: 2px;
            background: $text-primary;
            border-radius: 1px;
          }
        }
      }
    }

    // 一行一个筛选项，但标签和内容在同一行
    :deep(.el-form-item) {
      display: flex;
      align-items: center;
      flex-wrap: nowrap;
      margin-bottom: $spacing-md;
    }

    :deep(.el-form-item__label) {
      white-space: nowrap;
      padding-right: $spacing-sm;
    }

    :deep(.el-form-item__content) {
      flex: 1;
    }
  }

  .table-card {
    border: 1px solid $border-color;
    border-radius: $radius-md;
    background: $bg-primary;

    .pagination {
      margin-top: $spacing-xl;
      padding: $spacing-lg;
      display: flex;
      justify-content: flex-end;
      border-top: 1px solid $border-light;
    }
  }

  .announcement-content {
    white-space: pre-wrap;
    line-height: $line-height-relaxed;
    color: $text-secondary;
  }
}

// 对话框样式优化
:deep(.el-dialog) {
  .el-dialog__header {
    border-bottom: 1px solid $border-light;
    padding: $spacing-lg $spacing-xl;
  }

  .el-dialog__body {
    padding: $spacing-xl;
  }

  .el-dialog__footer {
    border-top: 1px solid $border-light;
    padding: $spacing-lg $spacing-xl;
  }
}

// 响应式
@media (max-width: $breakpoint-md) {
  .announcement-list {
    padding: $spacing-md;

    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: $spacing-md;

      h2 {
        font-size: $font-size-2xl;
      }
    }
  }
}
</style>
