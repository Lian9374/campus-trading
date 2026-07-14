<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="auth-title gradient-text">加入我们</h2>
      <p class="auth-subtitle">注册校园二手，开启闲置交易之旅</p>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" @submit.prevent="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称" prefix-icon="UserFilled" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（至少6位）" prefix-icon="Lock" size="large"
            show-password />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号（选填）" prefix-icon="Phone" size="large" />
        </el-form-item>
        <el-form-item prop="campus">
          <el-input v-model="form.campus" placeholder="所在校区（选填）" prefix-icon="School" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <p class="auth-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  phone: '',
  campus: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度3-50个字符', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.register(form)
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e) {
    // error handled in interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #ecfdf5 0%, #d1fae5 50%, #f0fdf4 100%);
  position: relative;
  overflow: hidden;
}

.auth-page::before {
  content: '';
  position: absolute;
  top: -180px;
  right: -180px;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: rgba(16, 185, 129, 0.08);
}

.auth-page::after {
  content: '';
  position: absolute;
  bottom: -120px;
  left: -120px;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  background: rgba(5, 150, 105, 0.06);
}

.auth-card {
  width: 420px;
  padding: 40px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  position: relative;
  z-index: 1;
}

.auth-title {
  text-align: center;
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 8px;
}

.auth-subtitle {
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin-bottom: 36px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: var(--text-base);
  font-weight: 600;
  border-radius: var(--radius-sm);
}

.auth-footer {
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin-top: 16px;
}

.auth-footer a {
  color: var(--color-primary);
  font-weight: 600;
}

.auth-footer a:hover {
  color: var(--color-primary-dark);
}
</style>
