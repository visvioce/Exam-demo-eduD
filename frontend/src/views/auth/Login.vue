<template>
  <div class="login-container">
    <!-- 左侧黑色动态区域 -->
    <div class="login-left">
      <!-- 星空粒子 -->
      <div class="particles">
        <div 
          v-for="i in 8" 
          :key="i" 
          class="particle"
          :style="{ 
            '--delay': (i * 0.3) + 's',
            '--x': ((i * 13) % 80 + 10) + '%',
            '--y': ((i * 17) % 70 + 15) + '%'
          }"
        ></div>
      </div>
      
      <div class="left-content">
        <h1 class="brand-title">南方学院</h1>
        <p class="brand-subtitle">在线考试系统</p>

        <!-- 名人名言展示 -->
        <div class="quote-container">
          <transition :name="'quote-' + transitionType" mode="out-in">
            <div :key="currentQuoteIndex" class="quote-box">
              <p class="quote-text">
                <span 
                  v-for="(char, index) in quotes[currentQuoteIndex]!.text" 
                  :key="index" 
                  class="char"
                  :style="{ '--i': index }"
                >{{ char }}</span>
              </p>
              <p class="quote-author">
                <span class="dash">—— </span>
                <span 
                  v-for="(char, index) in quotes[currentQuoteIndex]!.author" 
                  :key="'a'+index" 
                  class="char author-char"
                  :style="{ '--i': quotes[currentQuoteIndex]!.text.length + index }"
                >{{ char }}</span>
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
const transitionType = ref(0)
let quoteTimer: ReturnType<typeof setInterval> | null = null

function nextQuote() {
  transitionType.value = (transitionType.value + 1) % 4
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

.login-container {
  display: flex;
  min-height: 100vh;
  background: #ffffff;
}

.login-left {
  flex: 0 0 45%;
  background: #000000;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  .particles {
    position: absolute;
    inset: 0;
    z-index: 1;

    .particle {
      position: absolute;
      width: 2px;
      height: 2px;
      background: rgba(255, 255, 255, 0.6);
      border-radius: 50%;
      left: var(--x);
      top: var(--y);
      animation: twinkle 3s ease-in-out infinite;
      animation-delay: var(--delay);
    }
  }

  @keyframes twinkle {
    0%, 100% { opacity: 0.2; transform: scale(1); }
    50% { opacity: 1; transform: scale(1.5); }
  }

  .left-content {
    position: relative;
    z-index: 2;
    text-align: center;
    color: #ffffff;

    .brand-title {
      font-size: 56px;
      font-weight: 700;
      margin: 0 0 16px 0;
      letter-spacing: 4px;
      line-height: 1.2;
      animation: breathe 4s ease-in-out infinite;
    }

    .brand-subtitle {
      font-size: 20px;
      margin: 0 0 60px 0;
      opacity: 0.8;
      letter-spacing: 2px;
      font-weight: 300;
      animation: breathe 4s ease-in-out infinite;
      animation-delay: -1s;
    }
  }

  @keyframes breathe {
    0%, 100% { 
      opacity: 1; 
      transform: translateY(0);
      text-shadow: 0 0 30px rgba(255, 255, 255, 0.3);
    }
    50% { 
      opacity: 0.85; 
      transform: translateY(-5px);
      text-shadow: 0 0 50px rgba(255, 255, 255, 0.5);
    }
  }

  .quote-container {
    min-height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;

    .quote-box {
      text-align: center;
      padding: 24px 32px;
      max-width: 420px;

      .quote-text {
        font-size: 22px;
        font-weight: 400;
        line-height: 2;
        margin: 0 0 20px 0;
        color: rgba(255, 255, 255, 0.95);
        letter-spacing: 2px;

        .char {
          display: inline-block;
          animation: char-bounce 2.5s ease-in-out infinite;
          animation-delay: calc(var(--i) * 0.05s);
        }
      }

      .quote-author {
        font-size: 14px;
        margin: 0;
        color: rgba(255, 255, 255, 0.5);

        .dash, .author-char {
          display: inline-block;
          animation: char-bounce 2.5s ease-in-out infinite;
        }
      }
    }
  }

  @keyframes char-bounce {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-4px); }
  }

  // 切换动画 - 简化合并
  .quote-0-enter-active, .quote-1-enter-active,
  .quote-2-enter-active, .quote-3-enter-active {
    animation: quote-in 1.5s cubic-bezier(0.2, 0, 0.2, 1) forwards;
  }
  .quote-0-leave-active, .quote-1-leave-active,
  .quote-2-leave-active, .quote-3-leave-active {
    animation: quote-out 1.2s cubic-bezier(0.2, 0, 0.2, 1) forwards;
  }

  @keyframes quote-in { 0% { opacity: 0; } 100% { opacity: 1; } }
  @keyframes quote-out { 0% { opacity: 1; } 100% { opacity: 0; } }

  .quote-1-enter-active { animation-name: slide-up-in; }
  .quote-1-leave-active { animation-name: slide-up-out; }
  @keyframes slide-up-in { 0% { opacity: 0; transform: translateY(30px); } 100% { opacity: 1; transform: translateY(0); } }
  @keyframes slide-up-out { 0% { opacity: 1; transform: translateY(0); } 100% { opacity: 0; transform: translateY(-30px); } }

  .quote-2-enter-active { animation-name: scale-in; }
  .quote-2-leave-active { animation-name: scale-out; }
  @keyframes scale-in { 0% { opacity: 0; transform: scale(0.9); } 100% { opacity: 1; transform: scale(1); } }
  @keyframes scale-out { 0% { opacity: 1; transform: scale(1); } 100% { opacity: 0; transform: scale(1.1); } }

  .quote-3-enter-active { animation-name: blur-in; }
  .quote-3-leave-active { animation-name: blur-out; }
  @keyframes blur-in { 0% { opacity: 0; filter: blur(20px); transform: scale(1.1); } 100% { opacity: 1; filter: blur(0); transform: scale(1); } }
  @keyframes blur-out { 0% { opacity: 1; filter: blur(0); transform: scale(1); } 100% { opacity: 0; filter: blur(20px); transform: scale(0.9); } }
}

.login-right {
  flex: 0 0 55%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  padding: 60px;
}

.login-content {
  width: 100%;
  max-width: 400px;

  .login-header {
    margin-bottom: 48px;

    h2 {
      font-size: 32px;
      font-weight: 600;
      color: #000000;
      margin: 0 0 12px 0;
      letter-spacing: -0.5px;
    }

    .login-subtitle {
      font-size: 14px;
      color: #666666;
      margin: 0;
    }
  }

  .login-form {
    .form-item {
      margin-bottom: 24px;

      :deep(.el-input__wrapper) {
        padding: 0 16px;
        border-radius: 8px;
        background: #ffffff;
        border: 1px solid #e0e0e0;
        box-shadow: none;
        transition: all 0.2s ease;

        &:hover { border-color: #999999; }
        &.is-focus { border-color: #000000; }

        .el-input__inner {
          height: 48px;
          line-height: 48px;
          color: #000000;
          font-size: 15px;
        }

        .el-input__inner::placeholder { color: #999999; }
        .el-input__prefix { color: #666666; }
      }

      :deep(.el-form-item__error) {
        color: #d32f2f;
        font-size: 12px;
        padding-top: 6px;
      }
    }

    .login-btn {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      background: #000000;
      border-color: #000000;
      border-radius: 8px;
      transition: all 0.2s ease;

      &:hover {
        background: #333333;
        border-color: #333333;
        transform: translateY(-1px);
      }

      &:active { transform: translateY(0); }
      &.is-loading { opacity: 0.8; }
    }
  }

  .login-footer {
    margin-top: 32px;
    text-align: center;

    .register-link {
      font-size: 14px;
      color: #666666;
      transition: color 0.2s ease;

      &:hover { color: #000000; }
    }
  }
}
</style>
