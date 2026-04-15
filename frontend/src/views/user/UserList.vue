<template>
  <div class="user-list base-list-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="handleCreate" v-if="isAdmin">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="80px">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="用户名/昵称" clearable @input="handleKeywordInput" class="search-control" />
        </el-form-item>
        <el-form-item label="角色">
          <div class="filter-tabs">
            <button
              type="button"
              v-for="item in roleOptions" 
              :key="item.value"
              :class="['tab-item', { active: searchForm.role === item.value }]"
              :aria-pressed="searchForm.role === item.value"
              @click="handleRoleChange(item.value)"
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

    <!-- 用户列表 -->
    <el-card class="table-card">
      <el-table :data="users" v-loading="loading" stripe table-layout="auto" :fit="true">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="150" show-overflow-tooltip />
        <el-table-column label="昵称" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" min-width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">{{ getRoleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getUserStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="140">
          <template #default="{ row }">
            <ActionButtons
              :show-view="false"
              @edit="handleEdit(row)"
              @delete="handleDelete(row)"
              :show-delete="row.id !== currentUserId"
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
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '添加用户'" width="500px" class="base-dialog">
      <el-form :model="userForm" :rules="getFormRules" ref="userFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="userForm.status" placeholder="请选择状态">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
            <el-option label="锁定" value="SUSPENDED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { formatDate, getRoleName, getUserStatusName } from '@/utils/format'
import { getErrorMessage } from '@/utils/error'
import { useListPage } from '@/composables/useListPage'
import type { FormInstance, FormRules } from 'element-plus'
import type { UserResponse } from '@/types'
import ActionButtons from '@/components/ActionButtons.vue'

const authStore = useAuthStore()

const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const userFormRef = ref<FormInstance>()

const isAdmin = computed(() => authStore.user?.role === 'ADMIN')
const currentUserId = computed(() => authStore.user?.id)

const roleOptions = [
  { label: '全部', value: '' },
  { label: '管理员', value: 'ADMIN' },
  { label: '教师', value: 'TEACHER' },
  { label: '学生', value: 'STUDENT' }
]

const searchForm = reactive({
  keyword: '',
  role: ''
})

const {
  data: users,
  loading,
  total,
  pagination,
  loadData: loadUsers,
  reset
} = useListPage<UserResponse>({
  fetchFn: (params) => userApi.page({
    ...params,
    keyword: searchForm.keyword || undefined,
    role: searchForm.role || undefined
  }),
  immediate: false
})

const userForm = reactive({
  id: 0,
  username: '',
  nickname: '',
  password: '',
  role: 'STUDENT',
  status: 'ACTIVE'
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
})

// 动态获取表单验证规则（编辑时密码非必填）
const getFormRules = computed<FormRules>(() => {
  if (isEdit.value) {
    return {
      username: rules.username,
      nickname: rules.nickname,
      role: rules.role,
      status: rules.status
      // 编辑时不包含 password 规则
    }
  }
  return rules
})

function getRoleType(role: string) {
  const map: Record<string, string> = {
    ADMIN: 'danger',
    TEACHER: 'warning',
    STUDENT: 'success'
  }
  return map[role] || 'info'
}

function getStatusType(status: string) {
  const map: Record<string, string> = {
    ACTIVE: 'success',
    INACTIVE: 'danger',
    SUSPENDED: 'warning'
  }
  return map[status] || 'info'
}

function handleKeywordInput() {
  void reset()
}

function handleRoleChange(value: string) {
  searchForm.role = searchForm.role === value ? '' : value
  void reset()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.role = ''
  void reset()
}

function handleCreate() {
  isEdit.value = false
  Object.assign(userForm, {
    id: 0,
    username: '',
    nickname: '',
    password: '',
    role: 'STUDENT',
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

function handleEdit(row: UserResponse) {
  isEdit.value = true
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    nickname: row.nickname,
    role: row.role,
    status: row.status
  })
  dialogVisible.value = true
}

async function handleDelete(row: UserResponse) {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userApi.delete(row.id)
    ElMessage.success('删除成功')
    void loadUsers()
  } catch {
    // 取消删除
  }
}

async function handleSubmit() {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await userApi.update(userForm.id, {
            nickname: userForm.nickname,
            role: userForm.role as 'ADMIN' | 'TEACHER' | 'STUDENT',
            status: userForm.status as 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
          })
          ElMessage.success('更新成功')
        } else {
          await userApi.create({
            username: userForm.username,
            nickname: userForm.nickname,
            password: userForm.password,
            role: userForm.role as 'ADMIN' | 'TEACHER' | 'STUDENT',
            status: userForm.status as 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
          })
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        void loadUsers()
      } catch (error: unknown) {
        ElMessage.error(getErrorMessage(error, '操作失败'))
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  void loadUsers()
})
</script>

<style scoped lang="scss">
@use '@/styles/views/base-list.scss';
</style>
