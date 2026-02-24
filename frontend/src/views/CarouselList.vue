<template>
  <div class="carousel-list base-list-page">
    <div class="page-header">
      <h2>轮播图管理</h2>
      <el-button type="primary" @click="handleCreate" v-if="hasPermission(['ADMIN'])">
        <el-icon><Plus /></el-icon>
        添加轮播图
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="轮播图标题" clearable @input="handleKeywordInput" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="状态">
          <div class="filter-tabs">
            <span 
              v-for="item in statusOptions" 
              :key="item.value"
              :class="['tab-item', { active: searchForm.status === item.value }]"
              @click="handleStatusChange(item.value)"
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

    <!-- 轮播图卡片列表 -->
    <div class="carousel-grid" v-loading="loading">
      <el-card v-for="carousel in filteredCarousels" :key="carousel.id" class="carousel-card">
        <div class="card-content">
          <div class="carousel-preview">
            <img :src="carousel.imageUrl" :alt="carousel.title" class="preview-image" @error="handleImageError" />
          </div>
          <div class="carousel-info">
            <div class="carousel-title">{{ carousel.title }}</div>
            <div class="carousel-meta">
              <el-tag :type="carousel.status === 'ACTIVE' ? 'success' : 'info'" size="small">
                {{ carousel.status === 'ACTIVE' ? '活跃' : '禁用' }}
              </el-tag>
              <span class="sort-order">排序: {{ carousel.sortOrder }}</span>
            </div>
            <div class="carousel-link" v-if="carousel.linkUrl">
              <el-icon><Link /></el-icon>
              <span>{{ carousel.linkUrl }}</span>
            </div>
            <div class="carousel-description" v-if="carousel.description">
              {{ carousel.description }}
            </div>
          </div>
        </div>
        <div class="card-actions">
          <el-button type="primary" size="small" @click="handleEdit(carousel)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(carousel)">
            删除
          </el-button>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!loading && filteredCarousels.length === 0" description="暂无轮播图" />

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑轮播图' : '添加轮播图'" width="600px" class="base-dialog">
      <el-form :model="carouselForm" :rules="rules" ref="carouselFormRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="carouselForm.title" placeholder="请输入轮播图标题" />
        </el-form-item>
        <el-form-item label="图片URL" prop="imageUrl">
          <el-input v-model="carouselForm.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="跳转链接" prop="linkUrl">
          <el-input v-model="carouselForm.linkUrl" placeholder="请输入跳转链接（可选）" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="carouselForm.description" type="textarea" :rows="3" placeholder="请输入描述（可选）" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="carouselForm.sortOrder" :min="0" class="full-width" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="carouselForm.status" class="full-width">
                <el-option label="活跃" value="ACTIVE" />
                <el-option label="禁用" value="INACTIVE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { carouselApi } from '@/api/carousel'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Link } from '@element-plus/icons-vue'
import { getErrorMessage } from '@/utils/error'
import type { FormInstance, FormRules } from 'element-plus'
import type { Carousel } from '@/types'

const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const carousels = ref<Carousel[]>([])
const editDialogVisible = ref(false)
const isEdit = ref(false)
const carouselFormRef = ref<FormInstance>()

const statusOptions = [
  { label: '全部', value: '' },
  { label: '活跃', value: 'ACTIVE' },
  { label: '禁用', value: 'INACTIVE' }
]

const searchForm = reactive({
  keyword: '',
  status: ''
})

const carouselForm = reactive({
  id: 0,
  title: '',
  imageUrl: '',
  linkUrl: '',
  description: '',
  sortOrder: 0,
  status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE'
})

const rules = reactive<FormRules>({
  title: [{ required: true, message: '请输入轮播图标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请输入图片URL', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

const filteredCarousels = computed(() => {
  let result = [...carousels.value]

  if (searchForm.keyword) {
    const keyword = searchForm.keyword.toLowerCase()
    result = result.filter(c => c.title.toLowerCase().includes(keyword))
  }

  if (searchForm.status) {
    result = result.filter(c => c.status === searchForm.status)
  }

  result.sort((a, b) => a.sortOrder - b.sortOrder)

  return result
})

function hasPermission(roles: string[]) {
  return roles.includes(authStore.user?.role || '')
}

async function loadCarousels() {
  loading.value = true
  try {
    const res = await carouselApi.list()
    carousels.value = res.data
  } catch (error) {
    ElMessage.error('加载轮播图失败')
  } finally {
    loading.value = false
  }
}

function handleKeywordInput() {
  // 关键字输入时，使用计算属性自动过滤
}

function handleStatusChange(value: string) {
  searchForm.status = searchForm.status === value ? '' : value
  // 状态点击时，使用计算属性自动过滤
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = ''
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjEwMCIgeT0iMTAwIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMjAiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIiBmaWxsPSIjOWU5ZTllIj7lj4zor4fnu4XljLs8L3RleHQ+PC9zdmc+'
}

function handleCreate() {
  isEdit.value = false
  Object.assign(carouselForm, {
    id: 0,
    title: '',
    imageUrl: '',
    linkUrl: '',
    description: '',
    sortOrder: 0,
    status: 'ACTIVE'
  })
  editDialogVisible.value = true
}

function handleEdit(carousel: Carousel) {
  isEdit.value = true
  Object.assign(carouselForm, {
    id: carousel.id,
    title: carousel.title,
    imageUrl: carousel.imageUrl,
    linkUrl: carousel.linkUrl || '',
    description: carousel.description || '',
    sortOrder: carousel.sortOrder,
    status: carousel.status
  })
  editDialogVisible.value = true
}

async function handleDelete(carousel: Carousel) {
  try {
    await ElMessageBox.confirm('确定要删除该轮播图吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await carouselApi.delete(carousel.id)
    ElMessage.success('删除成功')
    loadCarousels()
  } catch {
  }
}

async function handleSubmit() {
  if (!carouselFormRef.value) return

  await carouselFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const data = {
          title: carouselForm.title,
          imageUrl: carouselForm.imageUrl,
          linkUrl: carouselForm.linkUrl || undefined,
          description: carouselForm.description || undefined,
          sortOrder: carouselForm.sortOrder,
          status: carouselForm.status
        }

        if (isEdit.value) {
          await carouselApi.update(carouselForm.id, data)
          ElMessage.success('更新成功')
        } else {
          await carouselApi.create(data)
          ElMessage.success('添加成功')
        }
        editDialogVisible.value = false
        loadCarousels()
      } catch (error: unknown) {
        ElMessage.error(getErrorMessage(error, '操作失败'))
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  loadCarousels()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;
@use '@/styles/views/base-list.scss';

.carousel-list {
  .full-width {
    width: 100%;
  }

  .carousel-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
    gap: $spacing-lg;
  }

  .carousel-card {
    border: 1px solid $border-color;
    border-radius: $radius-md;
    background: $bg-primary;
    transition: all $transition-fast;

    &:hover {
      transform: translateY(-2px);
    }

    .card-content {
      display: flex;
      gap: $spacing-lg;
      padding: $spacing-lg;
    }

    .carousel-preview {
      flex-shrink: 0;
      width: 120px;
      height: 90px;
      border-radius: $radius-sm;
      overflow: hidden;
      background: $bg-hover;
    }

    .preview-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .carousel-info {
      flex: 1;
      min-width: 0;
    }

    .carousel-title {
      font-size: $font-size-lg;
      font-weight: $font-weight-medium;
      color: $text-primary;
      margin-bottom: $spacing-sm;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .carousel-meta {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      margin-bottom: $spacing-sm;
    }

    .sort-order {
      font-size: $font-size-sm;
      color: $text-tertiary;
    }

    .carousel-link {
      display: flex;
      align-items: center;
      gap: $spacing-xs;
      font-size: $font-size-sm;
      color: $text-secondary;
      margin-bottom: $spacing-xs;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      .el-icon {
        font-size: $font-size-xs;
        flex-shrink: 0;
      }
    }

    .carousel-description {
      font-size: $font-size-sm;
      color: $text-tertiary;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .card-actions {
      display: flex;
      justify-content: flex-end;
      gap: $spacing-sm;
      padding: $spacing-md $spacing-lg;
      border-top: 1px solid $border-light;
    }
  }
}

// 响应式
@media (max-width: $breakpoint-md) {
  .carousel-list {
    .carousel-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style>
