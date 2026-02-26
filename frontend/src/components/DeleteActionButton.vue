<template>
  <IconActionButton
    :icon="Close"
    :tooltip="tooltip"
    :aria-label="ariaLabel"
    :show-tooltip="showTooltip"
    :stop-propagation="stopPropagation"
    :button-class="buttonClass"
    type="danger"
    @click="(event) => emit('click', event)"
  />
</template>

<script setup lang="ts">
import { Close } from '@element-plus/icons-vue'
import IconActionButton from './IconActionButton.vue'

withDefaults(defineProps<{
  tooltip?: string
  ariaLabel?: string
  showTooltip?: boolean
  stopPropagation?: boolean
  buttonClass?: string
}>(), {
  tooltip: '删除',
  ariaLabel: '',
  showTooltip: false,
  stopPropagation: true,
  buttonClass: ''
})

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()
</script>
