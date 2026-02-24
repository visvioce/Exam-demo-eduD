<template>
  <div class="action-buttons">
    <el-tooltip content="查看" placement="top" v-if="showView">
      <el-button 
        size="small" 
        @click="$emit('view')"
        class="action-btn view-btn"
      >
        <el-icon><View /></el-icon>
      </el-button>
    </el-tooltip>
    <el-tooltip content="编辑" placement="top" v-if="showEdit">
      <el-button 
        size="small" 
        type="primary" 
        @click="$emit('edit')"
        class="action-btn edit-btn"
      >
        <el-icon><Edit /></el-icon>
      </el-button>
    </el-tooltip>
    <el-tooltip content="删除" placement="top" v-if="showDelete">
      <el-button 
        size="small" 
        type="danger" 
        @click="$emit('delete')"
        class="action-btn delete-btn"
      >
        <el-icon><Delete /></el-icon>
      </el-button>
    </el-tooltip>
  </div>
</template>

<script setup lang="ts">
import { View, Edit, Delete } from '@element-plus/icons-vue'

defineProps({
  showView: {
    type: Boolean,
    default: true
  },
  showEdit: {
    type: Boolean,
    default: true
  },
  showDelete: {
    type: Boolean,
    default: true
  }
})

defineEmits(['view', 'edit', 'delete'])
</script>

<style scoped>
.action-buttons {
  display: inline-flex;
  gap: 8px;
  align-items: center;
}

.action-btn {
  padding: 5px 10px !important;
  border-radius: 4px !important;
  min-width: 32px;
}

.action-btn :deep(.el-icon) {
  font-size: 14px;
}
</style>
