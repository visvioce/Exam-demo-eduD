<template>
  <div class="dashboard">
    <div class="carousel-section" v-if="carousels.length > 0">
      <el-carousel height="300px" :interval="5000" arrow="hover">
        <el-carousel-item v-for="item in carousels" :key="item.id">
          <div class="carousel-item" @click="handleCarouselClick(item)">
            <img :src="item.imageUrl" :alt="item.title" class="carousel-image" />
            <div class="carousel-content">
              <h3 class="carousel-title">{{ item.title }}</h3>
              <p v-if="item.description" class="carousel-description">{{ item.description }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="page-header">
      <h1>仪表盘</h1>
      <span class="page-date">{{ getCurrentDate() }}</span>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card ui-interactive-surface">
          <div class="stat-card__icon">
            <el-icon size="20"><User /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__value">{{ stats.userCount }}</div>
            <div class="stat-card__label">用户总数</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6">
        <div class="stat-card ui-interactive-surface">
          <div class="stat-card__icon">
            <el-icon size="20"><Reading /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__value">{{ stats.courseCount }}</div>
            <div class="stat-card__label">课程总数</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6">
        <div class="stat-card ui-interactive-surface">
          <div class="stat-card__icon">
            <el-icon size="20"><Document /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__value">{{ stats.questionCount }}</div>
            <div class="stat-card__label">题目总数</div>
          </div>
        </div>
      </el-col>

      <el-col :xs="12" :sm="6">
        <div class="stat-card ui-interactive-surface">
          <div class="stat-card__icon">
            <el-icon size="20"><Edit /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__value">{{ stats.examCount }}</div>
            <div class="stat-card__label">考试总数</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card class="quick-actions-card">
      <template #header>
        <span>快捷入口</span>
      </template>
      <el-row :gutter="12">
        <el-col :xs="12" :sm="6">
          <div class="action-item ui-interactive-surface" @click="router.push('/course')">
            <el-icon><Reading /></el-icon>
            <span>课程管理</span>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="action-item ui-interactive-surface" @click="router.push('/question')">
            <el-icon><Document /></el-icon>
            <span>题库管理</span>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="action-item ui-interactive-surface" @click="router.push('/exam')">
            <el-icon><Edit /></el-icon>
            <span>考试管理</span>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="action-item ui-interactive-surface" @click="router.push('/announcement')">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import { courseApi } from '@/api/course'
import { questionApi } from '@/api/question'
import { examApi } from '@/api/exam'
import { carouselApi } from '@/api/carousel'
import { User, Reading, Document, Edit, Bell } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import type { Carousel } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const carousels = ref<Carousel[]>([])
const stats = ref({
  userCount: 0,
  courseCount: 0,
  questionCount: 0,
  examCount: 0
})

function getCurrentDate() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekDay = weekDays[now.getDay()]
  return `${year}.${month}.${day} · ${weekDay}`
}

function handleCarouselClick(item: Carousel) {
  if (item.linkUrl) {
    window.open(item.linkUrl, '_blank')
  }
}

async function safeCount(loader: () => Promise<{ data: unknown[] }>) {
  try {
    const res = await loader()
    return Array.isArray(res.data) ? res.data.length : 0
  } catch {
    return 0
  }
}

async function loadStats() {
  const role = authStore.user?.role

  if (role === 'ADMIN') {
    const [userCount, courseCount, questionCount, examCount] = await Promise.all([
      safeCount(() => userApi.list()),
      safeCount(() => courseApi.list()),
      safeCount(() => questionApi.list()),
      safeCount(() => examApi.list())
    ])
    stats.value = { userCount, courseCount, questionCount, examCount }
    return
  }

  const [courseCount, questionCount, examCount] = await Promise.all([
    safeCount(() => courseApi.list()),
    safeCount(() => questionApi.list()),
    safeCount(() => examApi.list())
  ])
  stats.value = { userCount: 0, courseCount, questionCount, examCount }
}

async function loadCarousels() {
  try {
    const res = await carouselApi.getActive()
    carousels.value = res.data
  } catch {
    carousels.value = []
  }
}

onMounted(() => {
  loadStats()
  loadCarousels()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.dashboard {
  padding: $spacing-xl;

  .carousel-section {
    margin-bottom: $spacing-2xl;
    border-radius: $radius-md;
    overflow: hidden;
    box-shadow: none;

    :deep(.el-carousel) {
      border-radius: $radius-md;

      .el-carousel__arrow {
        background: rgba(0, 0, 0, 0.3);
        border-radius: $radius-full;

        &:hover {
          background: rgba(0, 0, 0, 0.5);
        }
      }

      .el-carousel__indicators .el-carousel__indicator {
        .el-carousel__button {
          background: rgba(255, 255, 255, 0.5);
          width: 12px;
          height: 4px;
          border-radius: 2px;
        }

        &.is-active .el-carousel__button {
          background: $primary;
        }
      }
    }
  }

  .carousel-item {
    position: relative;
    width: 100%;
    height: 100%;
    cursor: pointer;
    overflow: hidden;

    .carousel-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.5s ease;
    }

    &:hover .carousel-image {
      transform: scale(1.05);
    }

    .carousel-content {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: $spacing-xl;
      background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
      color: #ffffff;

      .carousel-title {
        font-size: $font-size-xl;
        font-weight: $font-weight-medium;
        margin: 0 0 $spacing-sm;
      }

      .carousel-description {
        font-size: $font-size-sm;
        opacity: 0.9;
        margin: 0;
      }
    }
  }

  .page-header {
    margin-bottom: $spacing-2xl;

    h1 {
      font-size: $font-size-3xl;
      font-weight: $font-weight-medium;
      color: $text-primary;
      margin: 0 0 $spacing-sm;
      letter-spacing: -0.5px;
    }

    .page-date {
      font-size: $font-size-sm;
      color: $text-tertiary;
    }
  }

  .stats-row {
    margin-bottom: $spacing-2xl;

    .stat-card {
      display: flex;
      align-items: center;
      gap: $spacing-lg;
      padding: $spacing-xl;
      border: 1px solid $border-color;
      border-radius: $radius-md;
      background: $bg-primary;

      &__icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 48px;
        height: 48px;
        background: $bg-hover;
        border-radius: $radius-md;
        color: $text-tertiary;

        .el-icon {
          font-size: 24px;
        }
      }

      &__content {
        .stat-card__value {
          font-size: $font-size-3xl;
          font-weight: $font-weight-medium;
          color: $text-primary;
          margin-bottom: $spacing-xs;
          line-height: 1.2;
        }

        .stat-card__label {
          font-size: $font-size-sm;
          color: $text-tertiary;
        }
      }
    }
  }

  .quick-actions-card {
    border: none;
    border-radius: 0;
    background: transparent;
    box-shadow: none;

    :deep(.el-card__header) {
      border-bottom: 1px solid $border-light;
      padding: $spacing-lg $spacing-xl;
      font-weight: $font-weight-medium;
      color: $text-primary;
      font-size: $font-size-lg;
    }

    :deep(.el-card__body) {
      padding: $spacing-xl;
    }

    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: $spacing-md;
      padding: $spacing-2xl $spacing-md;
      background: $bg-primary;
      border: 1px solid $border-color;
      border-radius: $radius-md;
      cursor: pointer;

      .el-icon {
        font-size: $font-size-3xl;
        color: $text-tertiary;
      }

      span {
        font-size: $font-size-sm;
        color: $text-secondary;
        font-weight: $font-weight-medium;
      }
    }
  }
}

@media (max-width: $breakpoint-sm) {
  .dashboard {
    padding: $spacing-md;

    .page-header h1 {
      font-size: $font-size-2xl;
    }

    .stats-row .stat-card {
      margin-bottom: $spacing-md;
      padding: $spacing-lg;

      &__icon {
        width: 40px;
        height: 40px;

        .el-icon {
          font-size: 20px;
        }
      }

      &__content .stat-card__value {
        font-size: $font-size-2xl;
      }
    }

    .quick-actions-card .action-item {
      padding: $spacing-xl $spacing-sm;

      .el-icon {
        font-size: $font-size-2xl;
      }
    }
  }
}
</style>
