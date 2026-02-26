<template>
  <div class="student-dashboard">
    <section class="carousel-section" v-if="carousels.length > 0">
      <el-carousel height="280px" :interval="5000" arrow="hover">
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
    </section>

    <section class="my-course-section">
      <div class="section-header">
        <div>
          <h2>课程总览</h2>
          <p>显示全部可查看课程，已参加课程会标注</p>
        </div>
        <el-button text type="primary" @click="router.push('/course')">进入课程页</el-button>
      </div>

      <el-card shadow="never" class="course-panel" v-loading="loadingCourses">
        <el-row v-if="allCourses.length > 0" :gutter="16">
          <el-col v-for="course in allCourses" :key="course.id" :xs="24" :sm="12" :lg="8">
            <el-card shadow="never" class="course-card ui-interactive-surface" @click="router.push(`/course/${course.id}`)">
              <div class="course-card__cover">
                <img v-if="course.coverUrl" :src="course.coverUrl" :alt="course.name" />
                <div v-else class="course-card__cover-placeholder">课程封面</div>
              </div>
              <div class="course-card__head">
                <h3 class="ui-title-clamp-2">{{ course.name }}</h3>
                <div class="course-card__tags">
                  <el-tag size="small" :type="isJoined(course.id) ? 'success' : 'info'">
                    {{ isJoined(course.id) ? '已参加' : '未参加' }}
                  </el-tag>
                  <el-tag size="small" type="info">{{ course.code }}</el-tag>
                </div>
              </div>
              <div class="course-card__meta">
                <div><span class="label">教师：</span>{{ getTeacherName(course) }}</div>
                <div><span class="label">学分：</span>{{ course.credits }}</div>
                <div v-if="course.deadline"><span class="label">截止：</span>{{ formatDate(course.deadline) }}</div>
              </div>
              <div class="course-card__foot">
                <el-tag size="small" :type="course.status === 'ACTIVE' ? 'success' : 'info'">
                  {{ course.status === 'ACTIVE' ? '进行中' : '已结束' }}
                </el-tag>
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-else description="暂无课程" />
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { carouselApi } from '@/api/carousel'
import { courseApi } from '@/api/course'
import type { Carousel, Course } from '@/types'
import { formatDate } from '@/utils/format'

const router = useRouter()
const carousels = ref<Carousel[]>([])
const allCourses = ref<Course[]>([])
const myCourses = ref<Course[]>([])
const loadingCourses = ref(false)
const joinedIds = computed(() => new Set(myCourses.value.map((course) => course.id)))

function isJoined(courseId: number) {
  return joinedIds.value.has(courseId)
}

function getTeacherName(course: Course) {
  if (course.teacherName?.trim()) return course.teacherName
  return course.teacherId ? `教师#${course.teacherId}` : '-'
}

function handleCarouselClick(item: Carousel) {
  if (item.linkUrl) {
    window.open(item.linkUrl, '_blank')
  }
}

async function loadCarousels() {
  try {
    const res = await carouselApi.getActive()
    carousels.value = res.data || []
  } catch {
    carousels.value = []
  }
}

async function loadCourses() {
  loadingCourses.value = true
  try {
    const [myRes, activeRes] = await Promise.all([
      courseApi.getMyCourses(),
      courseApi.getActiveCourses()
    ])
    myCourses.value = myRes.data || []

    const merged = new Map<number, Course>()
    for (const course of myCourses.value) {
      merged.set(course.id, course)
    }
    for (const course of activeRes.data || []) {
      if (!merged.has(course.id)) {
        merged.set(course.id, course)
      }
    }

    const courses = Array.from(merged.values())
    allCourses.value = courses.sort((a, b) => {
      const joinedDelta = Number(isJoined(b.id)) - Number(isJoined(a.id))
      if (joinedDelta !== 0) return joinedDelta
      return a.name.localeCompare(b.name, 'zh-CN')
    })
  } catch {
    allCourses.value = []
    myCourses.value = []
  } finally {
    loadingCourses.value = false
  }
}

onMounted(() => {
  loadCarousels()
  loadCourses()
})
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;

.student-dashboard {
  padding: $spacing-xl;
  display: grid;
  gap: $spacing-2xl;
}

.carousel-section {
  border-radius: $radius-md;
  overflow: hidden;

  :deep(.el-carousel) {
    border-radius: $radius-md;
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
    transition: transform 0.4s ease;
  }

  &:hover .carousel-image {
    transform: scale(1.03);
  }

  .carousel-content {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    padding: $spacing-lg;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.65), transparent);
    color: #ffffff;

    .carousel-title {
      margin: 0 0 $spacing-xs;
      font-size: $font-size-xl;
      font-weight: $font-weight-medium;
    }

    .carousel-description {
      margin: 0;
      font-size: $font-size-sm;
      opacity: 0.92;
    }
  }
}

.my-course-section {
  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-md;

    h2 {
      margin: 0;
      color: $text-primary;
      font-size: $font-size-2xl;
      font-weight: $font-weight-medium;
    }

    p {
      margin: $spacing-xs 0 0;
      color: $text-tertiary;
      font-size: $font-size-sm;
    }
  }
}

.course-panel {
  border: none;
  background: transparent;
  box-shadow: none;
}

.course-card {
  border: 1px solid $border-color;
  cursor: pointer;
  margin-bottom: $spacing-md;

  &__head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: $spacing-md;

    .course-card__tags {
      display: flex;
      gap: $spacing-xs;
      align-items: center;
    }

    h3 {
      margin: 0;
      font-size: $font-size-lg;
      color: $text-primary;
      line-height: 1.35;
    }
  }

  &__cover {
    width: 100%;
    height: 140px;
    border-radius: $radius-sm;
    overflow: hidden;
    margin-bottom: $spacing-md;
    border: 1px solid $border-light;
    background: $bg-hover;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $text-tertiary;
    font-size: $font-size-sm;
  }

  &__meta {
    display: grid;
    gap: $spacing-xs;
    color: $text-secondary;
    font-size: $font-size-sm;
    margin-bottom: $spacing-md;

    .label {
      color: $text-tertiary;
    }
  }

  &__foot {
    border-top: 1px solid $border-light;
    padding-top: $spacing-md;
  }
}

@media (max-width: $breakpoint-sm) {
  .student-dashboard {
    padding: $spacing-md;
  }

  .my-course-section .section-header h2 {
    font-size: $font-size-xl;
  }
}
</style>
