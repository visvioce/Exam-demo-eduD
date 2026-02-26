<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside width="240px" class="sidebar">
      <div class="sidebar-content">
        <!-- Logo -->
        <div class="logo-container">
          <span class="logo-text">南方学院</span>
        </div>

        <!-- 菜单 -->
        <el-menu
          :default-active="activeMenu"
          :router="true"
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <el-menu-item index="/course" v-if="hasPermission(['ADMIN', 'TEACHER', 'STUDENT'])">
            <el-icon><Reading /></el-icon>
            <template #title>{{ isStudent ? '我的课程' : '课程管理' }}</template>
          </el-menu-item>

          <el-menu-item index="/question" v-if="hasPermission(['ADMIN', 'TEACHER'])">
            <el-icon><Document /></el-icon>
            <template #title>题库管理</template>
          </el-menu-item>

          <el-menu-item index="/paper" v-if="hasPermission(['ADMIN', 'TEACHER'])">
            <el-icon><Notebook /></el-icon>
            <template #title>试卷管理</template>
          </el-menu-item>

          <el-menu-item index="/exam">
            <el-icon><Edit /></el-icon>
            <template #title>{{ isStudent ? '我的考试' : '考试管理' }}</template>
          </el-menu-item>

          <el-menu-item index="/announcement">
            <el-icon><Bell /></el-icon>
            <template #title>{{ isStudent ? '公告查看' : '公告管理' }}</template>
          </el-menu-item>

          <el-menu-item index="/carousel" v-if="hasPermission(['ADMIN'])">
            <el-icon><Picture /></el-icon>
            <template #title>轮播图管理</template>
          </el-menu-item>

          <el-menu-item index="/aiconfig" v-if="hasPermission(['ADMIN', 'TEACHER'])">
            <el-icon><Setting /></el-icon>
            <template #title>AI配置</template>
          </el-menu-item>

          <el-menu-item index="/user" v-if="hasPermission(['ADMIN'])">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
        </el-menu>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="content-container">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumbTitle">{{ breadcrumbTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="32" :src="authStore.user?.avatar || ''" class="user-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content" :class="{ 'exam-take-mode': isExamTakePage }">
        <router-view v-slot="{ Component }">
          <component :is="Component" :key="route.path" />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  House,
  Reading,
  Document,
  Notebook,
  Edit,
  Bell,
  Setting,
  User,
  Picture
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)
const isExamTakePage = computed(() => /^\/exam\/\d+\/take(?:\/)?$/.test(route.path))
const isStudent = computed(() => authStore.user?.role === 'STUDENT')

// 根据当前路由生成面包屑标题
const breadcrumbTitle = computed(() => {
  const titleMap: Record<string, string> = {
    '/course': isStudent.value ? '我的课程' : '课程管理',
    '/question': '题库管理',
    '/paper': '试卷管理',
    '/exam': isStudent.value ? '我的考试' : '考试管理',
    '/announcement': isStudent.value ? '公告查看' : '公告管理',
    '/carousel': '轮播图管理',
    '/aiconfig': 'AI配置',
    '/user': '用户管理',
    '/profile': '个人中心'
  }

  for (const [path, title] of Object.entries(titleMap)) {
    if (route.path.startsWith(path) && route.path !== path) {
      return title
    }
  }

  return titleMap[route.path] || ''
})

function hasPermission(roles: string[]) {
  const userRole = authStore.user?.role
  return roles.includes(userRole || '')
}

async function handleCommand(command: string) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      authStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
    } catch {
      // 取消操作
    }
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.main-layout {
  height: 100vh;
  background: $bg-primary;
}

// ===== 侧边栏 - 纯白简约风格 =====
.sidebar {
  width: $nav-width;
  background: $bg-primary;
  border-right: 1px solid $border-color;
  overflow: hidden;

  .sidebar-content {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  // Logo 区域
  .logo-container {
    height: $nav-height;
    display: flex;
    align-items: center;
    padding: 0 $spacing-lg;
    border-bottom: 1px solid $border-color;

    .logo-text {
      font-size: $font-size-lg;
      font-weight: $font-weight-medium;
      color: $text-primary;
      letter-spacing: -0.5px;
    }
  }

  // 菜单
  .sidebar-menu {
    flex: 1;
    border: none;
    background: transparent;
    padding: $spacing-sm 0;

    .el-menu-item {
      margin: 0 $spacing-sm;
      border-radius: $radius-sm;
      color: $text-tertiary;
      font-size: $font-size-base;
      transition: all $transition-fast;
      position: relative;

      &:hover {
        background: $bg-hover;
        color: $text-primary;
      }

      &.is-active {
        background: $bg-hover;
        color: $text-primary;
        font-weight: $font-weight-medium;

        &::before {
          content: '';
          position: absolute;
          left: -$spacing-sm;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 60%;
          background-color: $text-primary;
          border-radius: 0 2px 2px 0;
        }
      }

      .el-icon {
        width: 20px;
        color: inherit;
      }
    }
  }
}

// ===== 内容容器 =====
.content-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
  background: $bg-page;
}

// ===== 头部导航 - 纯白风格 =====
.header {
  height: $nav-height;
  background: $bg-primary;
  border-bottom: 1px solid $border-color;
  padding: 0 $spacing-xl;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .header-left {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    flex: 1;
    min-width: 0;

    :deep(.el-breadcrumb) {
      min-width: 0;
      overflow: hidden;
      white-space: nowrap;

      .el-breadcrumb__inner {
        color: $text-tertiary;
        max-width: 220px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;

        &:hover {
          color: $text-primary;
        }

        &.is-link {
          color: $text-tertiary;
        }
      }

      .el-breadcrumb__separator {
        color: $text-tertiary;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;

    .user-dropdown {
      .user-info {
        display: flex;
        align-items: center;
        gap: $spacing-md;
        cursor: pointer;
        padding: $spacing-sm $spacing-md;
        border-radius: $radius-sm;
        transition: background-color $transition-fast;
        min-width: 0;
        max-width: 280px;

        &:hover {
          background: $bg-hover;
        }

        .user-avatar {
          background: $bg-hover;
          color: $text-tertiary;
          font-size: $font-size-sm;
          border: 1px solid $border-color;
          flex-shrink: 0;

          :deep(img) {
            object-fit: cover;
          }
        }

        .username {
          font-size: $font-size-sm;
          color: $text-secondary;
          font-weight: $font-weight-medium;
          letter-spacing: 0.01em;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    :deep(.el-dropdown-menu) {
      .el-dropdown-menu__item {
        color: $text-secondary;

        &:hover {
          background: $bg-hover;
          color: $text-primary;
        }
      }
    }
  }
}

// ===== 主内容区域 =====
.main-content {
  flex: 1;
  min-width: 0;
  background: $bg-page;
  padding: $spacing-xl;
  overflow-y: auto;
  overflow-x: hidden;

  // 内容卡片
  .content-card {
    background: $bg-primary;
    border: 1px solid $border-color;
    border-radius: $radius-md;
    padding: $spacing-xl;
  }

  &.exam-take-mode {
    padding: 0;
  }
}

// ===== 响应式设计 =====
@media (max-width: $breakpoint-md) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: $z-dropdown;
    border-right: 1px solid $border-color;
  }

  .header {
    padding: 0 $spacing-md;

    .header-right {
      .user-dropdown {
        .user-info {
          .username {
            display: none;
          }
        }
      }
    }
  }

  .main-content {
    padding: $spacing-md;
  }
}
</style>
