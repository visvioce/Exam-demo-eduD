<template>
  <div class="action-buttons">
    <IconActionButton
      v-if="showView"
      :icon="View"
      tooltip="查看"
      show-tooltip
      aria-label="查看"
      button-class="view-btn"
      @click="$emit('view')"
    />
    <IconActionButton
      v-if="showEdit"
      :icon="Edit"
      tooltip="编辑"
      show-tooltip
      aria-label="编辑"
      type="primary"
      @click="$emit('edit')"
    />
    <IconActionButton
      v-if="showDelete"
      :icon="Close"
      tooltip="删除"
      show-tooltip
      aria-label="删除"
      type="danger"
      @click="$emit('delete')"
    />
  </div>
</template>

<script setup lang="ts">
import { View, Edit, Close } from '@element-plus/icons-vue'
import IconActionButton from './IconActionButton.vue'

withDefaults(defineProps<{
  showView?: boolean
  showEdit?: boolean
  showDelete?: boolean
}>(), {
  showView: true,
  showEdit: true,
  showDelete: true
})

defineEmits<{
  (e: 'view'): void
  (e: 'edit'): void
  (e: 'delete'): void
}>()
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.action-buttons {
  display: inline-flex;
  gap: $spacing-xs;
  align-items: center;
}

.view-btn {
  color: $text-tertiary;

  &:hover {
    color: $text-primary;
  }
}
</style>
