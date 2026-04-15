<template>
  <StudentDashboard v-if="isStudent" />
  <TeacherDashboard v-else />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import StudentDashboard from './StudentDashboard.vue'
import TeacherDashboard from './TeacherDashboard.vue'

const authStore = useAuthStore()
const isStudent = computed(() => authStore.user?.role === 'STUDENT')
</script>
