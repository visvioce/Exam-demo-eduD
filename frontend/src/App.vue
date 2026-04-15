<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 应用启动时恢复用户信息
onMounted(async () => {
  // 如果有 token 但没有用户信息，尝试获取
  if (authStore.token && !authStore.user) {
    await authStore.getCurrentUser()
  }
})
</script>
