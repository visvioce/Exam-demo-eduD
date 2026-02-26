<template>
  <div class="register-container">
    <div class="register-left">
      <div class="left-content">
        <p class="auth-kicker">Create Your Account</p>
        <h1 class="brand-title">开始你的学习旅程</h1>
        <p class="brand-subtitle">注册后默认身份为学生，可直接加入课程并参与考试。</p>
        <ul class="feature-list">
          <li>课程、试卷、考试流程一体化</li>
          <li>清晰的黑白极简交互风格</li>
          <li>支持自动组卷与在线答题</li>
        </ul>
      </div>
    </div>

    <div class="register-right">
      <div class="register-content">
        <div class="register-header">
          <h2>创建账户</h2>
          <p class="register-subtitle">加入我们的学习平台</p>
        </div>

        <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="register-form">
          <el-form-item prop="username" class="form-item">
            <el-input
              v-model="registerForm.username"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password" class="form-item">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword" class="form-item">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
          <el-form-item prop="nickname" class="form-item">
            <el-input
              v-model="registerForm.nickname"
              placeholder="昵称"
              prefix-icon="UserFilled"
              size="large"
            />
          </el-form-item>
          <div class="role-hint">注册后默认身份为学生</div>
          <el-form-item class="form-item">
            <el-button
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <router-link to="/login" class="login-link">
            已有账号？立即登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const validateConfirmPassword = (_rule: unknown, value: string, callback: (error?: string | Error) => void) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ]
})

async function handleRegister() {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await authStore.register({
          username: registerForm.username,
          password: registerForm.password,
          nickname: registerForm.nickname
        })
        if (success) {
          router.push('/login')
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

.register-left {
  flex: 0 0 44%;
  background: $black;
  color: #ffffff;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      linear-gradient(160deg, rgba(255, 255, 255, 0.08) 0%, transparent 45%),
      radial-gradient(circle at 88% 24%, rgba(255, 255, 255, 0.12), transparent 38%);
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
      font-size: clamp(34px, 4.4vw, 48px);
      line-height: 1.2;
      letter-spacing: 0.02em;
      font-weight: 640;
    }

    .brand-subtitle {
      margin: 0;
      font-size: $font-size-lg;
      line-height: 1.6;
      color: rgba(255, 255, 255, 0.74);
    }

    .feature-list {
      margin: $spacing-2xl 0 0;
      padding: 0;
      list-style: none;
      display: grid;
      gap: $spacing-md;

      li {
        position: relative;
        padding-left: $spacing-lg;
        font-size: $font-size-sm;
        color: rgba(255, 255, 255, 0.66);
        line-height: 1.6;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 9px;
          width: 6px;
          height: 6px;
          border-radius: 999px;
          background: rgba(255, 255, 255, 0.52);
        }
      }
    }
  }
}

.register-content {
  max-width: 440px;
}

.register-form {
  .role-hint {
    margin: -6px 0 14px;
    font-size: $font-size-xs;
    color: $text-tertiary;
  }

  :deep(.el-select) {
    width: 100%;

    .el-select__wrapper {
      height: 48px;
      border-radius: $radius-lg;
      border: 1px solid $border-color;
      box-shadow: none;

      &:hover {
        border-color: $text-tertiary;
      }

      &.is-focused {
        border-color: $text-primary;
      }
    }
  }
}

@media (max-width: 1024px) {
  .register-left {
    .left-content {
      max-width: 600px;
    }

    .feature-list {
      margin-top: $spacing-lg;

      li {
        margin: 0;
      }
    }
  }
}
</style>
