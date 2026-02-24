<template>
  <div class="register-container">
    <!-- 左侧黑色装饰区域 -->
    <div class="register-left">
      <div class="left-content">
        <h1 class="brand-title">南方学院</h1>
        <p class="brand-subtitle">在线考试系统</p>

        <!-- 装饰元素 -->
        <div class="decorative-elements">
          <div class="circle circle-1"></div>
          <div class="circle circle-2"></div>
          <div class="circle circle-3"></div>
        </div>
      </div>
    </div>

    <!-- 右侧注册表单区域 -->
    <div class="register-right">
      <div class="register-content">
        <!-- 标题 -->
        <div class="register-header">
          <h2>创建账户</h2>
          <p class="register-subtitle">加入我们的学习平台</p>
        </div>

        <!-- 注册表单 -->
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

        <!-- 底部链接 -->
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

// 左侧黑色装饰区域
.register-left {
  flex: 0 0 45%;
  background: $black;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

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
    }

    .brand-subtitle {
      font-size: 20px;
      margin: 0;
      opacity: 0.8;
      letter-spacing: 2px;
      font-weight: 300;
    }
  }

  // 装饰圆形元素
  .decorative-elements {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    z-index: 1;

    .circle {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.03);
    }

    .circle-1 {
      width: 400px;
      height: 400px;
      top: -200px;
      right: -100px;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }

    .circle-2 {
      width: 300px;
      height: 300px;
      bottom: -150px;
      left: -50px;
      border: 1px solid rgba(255, 255, 255, 0.08);
    }

    .circle-3 {
      width: 200px;
      height: 200px;
      top: 50%;
      right: 10%;
      border: 1px solid rgba(255, 255, 255, 0.05);
    }
  }
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

// 移动端适配
@media (max-width: 968px) {
  .register-container {
    flex-direction: column;
  }

  .register-left {
    flex: 0 0 auto;
    min-height: 35vh;
    padding: 50px 20px;

    .left-content {
      .brand-title {
        font-size: 36px;
        letter-spacing: 2px;
      }

      .brand-subtitle {
        font-size: 16px;
      }
    }

    .decorative-elements {
      .circle-1 {
        width: 250px;
        height: 250px;
        top: -100px;
        right: -50px;
      }

      .circle-2 {
        width: 200px;
        height: 200px;
        bottom: -80px;
        left: -30px;
      }

      .circle-3 {
        width: 150px;
        height: 150px;
      }
    }
  }

  .register-right {
    flex: 1;
    padding: 40px 20px;
  }

  .register-content {
    .register-header {
      margin-bottom: 32px;

      h2 {
        font-size: 28px;
      }
    }
  }
}
</style>
