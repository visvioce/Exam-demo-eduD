<template>
  <div class="login-container">
    <div class="login-left">
      <div class="left-content">
        <p class="auth-kicker">South College</p>
        <h1 class="brand-title">南方职业学院</h1>
        <p class="brand-subtitle">在线考试系统</p>

        <div class="quote-container">
          <transition name="quote-fade" mode="out-in">
            <div :key="currentQuoteIndex" class="quote-box">
              <p class="quote-text">“{{ quotes[currentQuoteIndex]!.text }}”</p>
              <p class="quote-author">
                —— {{ quotes[currentQuoteIndex]!.author }}
              </p>
            </div>
          </transition>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区域 -->
    <div class="login-right">
      <div class="login-content">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p class="login-subtitle">登录您的账户</p>
        </div>

        <el-form
          :model="loginForm"
          :rules="rules"
          ref="loginFormRef"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username" class="form-item">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>

          <el-form-item prop="password" class="form-item">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item class="form-item">
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <router-link to="/register" class="register-link">
            没有账号？立即注册
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const quotes = [
  { text: '学而不思则罔，思而不学则殆', author: '孔子' },
  { text: '书山有路勤为径，学海无涯苦作舟', author: '韩愈' },
  { text: '业精于勤，荒于嬉；行成于思，毁于随', author: '韩愈' },
  { text: '三人行，必有我师焉', author: '孔子' },
  { text: '路漫漫其修远兮，吾将上下而求索', author: '屈原' },
  { text: '千里之行，始于足下', author: '老子' },
  { text: '不积跬步，无以至千里', author: '荀子' },
  { text: '学而时习之，不亦说乎', author: '孔子' },
  { text: '吾生也有涯，而知也无涯', author: '庄子' },
  { text: '非学无以广才，非志无以成学', author: '诸葛亮' },
  { text: '锲而不舍，金石可镂', author: '荀子' },
  { text: '博学而笃志，切问而近思', author: '孔子' },
  { text: '教育的本质是一棵树摇动另一棵树', author: '雅斯贝尔斯' },
  { text: '教育就是培养习惯', author: '叶圣陶' },
  { text: '没有爱就没有教育', author: '陶行知' },
  { text: '教育不是注满一桶水，而是点燃一把火', author: '叶芝' },
  { text: '教育的根是苦的，但其果实是甜的', author: '亚里士多德' },
  { text: '学习的目的是成长，而非仅仅积累知识', author: '杜威' },
  { text: '从未犯错的人永远不会尝试新事物', author: '爱因斯坦' },
  { text: '学习不是偶然获得的，必须带着渴望去追求', author: '亚当斯' }
]

const currentQuoteIndex = ref(0)
let quoteTimer: ReturnType<typeof setInterval> | null = null

function nextQuote() {
  currentQuoteIndex.value = (currentQuoteIndex.value + 1) % quotes.length
}

onMounted(() => {
  quoteTimer = setInterval(nextQuote, 10000)
})

onUnmounted(() => {
  if (quoteTimer) clearInterval(quoteTimer)
})

async function handleLogin() {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await authStore.login(loginForm)
        if (success) {
          router.push((route.query.redirect as string) || '/dashboard')
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
@use '@/styles/design-tokens.scss' as *;
@use '@/styles/views/auth-form-shared.scss';

.login-left {
  flex: 0 0 44%;
  background: $black;
  color: #ffffff;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      radial-gradient(circle at 18% 24%, rgba(255, 255, 255, 0.12), transparent 42%),
      radial-gradient(circle at 88% 78%, rgba(255, 255, 255, 0.08), transparent 36%);
    opacity: 0.7;
  }

  .left-content {
    position: relative;
    z-index: 1;
    max-width: 460px;

    .auth-kicker {
      margin: 0 0 $spacing-lg;
      font-size: $font-size-sm;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: rgba(255, 255, 255, 0.68);
    }

    .brand-title {
      margin: 0 0 $spacing-md;
      font-size: clamp(36px, 4.8vw, 52px);
      letter-spacing: 0.04em;
      line-height: 1.15;
      font-weight: 650;
    }

    .brand-subtitle {
      margin: 0;
      font-size: $font-size-lg;
      color: rgba(255, 255, 255, 0.74);
    }
  }

  .quote-container {
    margin-top: $spacing-3xl;
    padding-top: $spacing-xl;
    border-top: 1px solid rgba(255, 255, 255, 0.2);
    min-height: 132px;

    .quote-box {
      display: grid;
      gap: $spacing-md;

      .quote-text {
        margin: 0;
        font-size: clamp(18px, 2vw, 22px);
        line-height: 1.75;
        color: rgba(255, 255, 255, 0.96);
        letter-spacing: 0.01em;
      }

      .quote-author {
        margin: 0;
        font-size: $font-size-sm;
        color: rgba(255, 255, 255, 0.62);
      }
    }
  }
}

.quote-fade-enter-active,
.quote-fade-leave-active {
  transition: opacity 0.35s ease;
}

.quote-fade-enter-from,
.quote-fade-leave-to {
  opacity: 0;
}

.login-content {
  max-width: 420px;
}

@media (max-width: 1024px) {
  .login-left {
    .left-content {
      max-width: 600px;
    }
  }
}

</style>
