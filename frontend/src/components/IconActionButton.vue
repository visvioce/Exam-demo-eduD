<template>
  <el-tooltip v-if="showTooltip" :content="tooltip" placement="top">
    <el-button
      link
      size="small"
      :type="type || undefined"
      :aria-label="ariaLabel || tooltip"
      :class="['icon-action-btn', buttonClass]"
      @click="handleClick"
    >
      <el-icon><component :is="icon" /></el-icon>
    </el-button>
  </el-tooltip>
  <el-button
    v-else
    link
    size="small"
    :type="type || undefined"
    :aria-label="ariaLabel || tooltip"
    :class="['icon-action-btn', buttonClass]"
    @click="handleClick"
  >
    <el-icon><component :is="icon" /></el-icon>
  </el-button>
</template>

<script setup lang="ts">
import type { Component } from 'vue'

const props = withDefaults(defineProps<{
  icon: Component
  tooltip?: string
  ariaLabel?: string
  showTooltip?: boolean
  stopPropagation?: boolean
  buttonClass?: string
  type?: '' | 'primary' | 'success' | 'warning' | 'info' | 'danger'
}>(), {
  tooltip: '',
  ariaLabel: '',
  showTooltip: false,
  stopPropagation: true,
  buttonClass: '',
  type: ''
})

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

function handleClick(event: MouseEvent) {
  if (props.stopPropagation) {
    event.stopPropagation()
  }
  emit('click', event)
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.icon-action-btn {
  width: 22px;
  min-width: 22px;
  height: 22px;
  padding: 0 !important;
  border: none !important;
  border-radius: $radius-sm !important;
  opacity: 0.75;
  transition: opacity $transition-fast;

  &:hover {
    opacity: 1;
  }

  &:not(.is-disabled):focus-visible {
    box-shadow: $focus-ring;
  }
}

.icon-action-btn :deep(.el-icon) {
  font-size: 13px;
}
</style>
