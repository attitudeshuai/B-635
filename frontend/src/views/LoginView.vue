<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-hero">
        <div class="hero-content">
          <div class="hero-icon">🏃</div>
          <h1 class="hero-title">运动管理系统</h1>
          <p class="hero-subtitle">记录运动、追踪目标、保持健康</p>
          <div class="hero-features">
            <div class="feature">
              <span class="feature-icon">📊</span>
              <span>数据统计</span>
            </div>
            <div class="feature">
              <span class="feature-icon">🎯</span>
              <span>目标追踪</span>
            </div>
            <div class="feature">
              <span class="feature-icon">📈</span>
              <span>趋势分析</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="login-form-container">
        <div class="form-wrapper">
          <div class="form-header">
            <h2>{{ isLogin ? '欢迎回来' : '创建账户' }}</h2>
            <p>{{ isLogin ? '登录您的账户继续' : '注册开始您的健康之旅' }}</p>
          </div>
          
          <form @submit.prevent="handleSubmit" class="login-form">
            <div class="form-group">
              <label class="form-label">用户名</label>
              <input 
                type="text" 
                v-model="form.username" 
                class="form-input"
                placeholder="请输入用户名"
                required
              />
            </div>
            
            <div v-if="!isLogin" class="form-group">
              <label class="form-label">邮箱</label>
              <input 
                type="email" 
                v-model="form.email" 
                class="form-input"
                placeholder="请输入邮箱"
                required
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">密码</label>
              <input 
                type="password" 
                v-model="form.password" 
                class="form-input"
                placeholder="请输入密码"
                required
              />
            </div>
            
            <div v-if="!isLogin" class="form-group">
              <label class="form-label">昵称（可选）</label>
              <input 
                type="text" 
                v-model="form.nickname" 
                class="form-input"
                placeholder="请输入昵称"
              />
            </div>
            
            <button type="submit" class="btn btn-primary btn-lg submit-btn" :disabled="loading">
              <span v-if="loading" class="spinner"></span>
              {{ isLogin ? '登 录' : '注 册' }}
            </button>
          </form>
          
          <div class="form-footer">
            <p>
              {{ isLogin ? '还没有账户？' : '已有账户？' }}
              <a href="#" @click.prevent="toggleMode">
                {{ isLogin ? '立即注册' : '立即登录' }}
              </a>
            </p>
          </div>
          
          <div v-if="isLogin" class="test-account">
            <p>测试账号：admin / 123456</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToastStore()

const isLogin = ref(true)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  email: '',
  nickname: ''
})

function toggleMode() {
  isLogin.value = !isLogin.value
  form.username = ''
  form.password = ''
  form.email = ''
  form.nickname = ''
}

async function handleSubmit() {
  loading.value = true
  
  try {
    if (isLogin.value) {
      const result = await authStore.login(form.username, form.password)
      if (result.success) {
        toast.success('登录成功，欢迎回来！')
        router.push('/')
      } else {
        toast.error(result.message || '登录失败')
      }
    } else {
      const result = await authStore.register({
        username: form.username,
        password: form.password,
        email: form.email,
        nickname: form.nickname
      })
      if (result.success) {
        toast.success('注册成功，请登录')
        isLogin.value = true
        form.password = ''
      } else {
        toast.error(result.message || '注册失败')
      }
    }
  } catch (error) {
    toast.error(error.response?.data?.message || '操作失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
}

.login-container {
  display: flex;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  margin: 2rem;
}

.login-hero {
  flex: 1;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #06b6d4 100%);
  padding: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.hero-content {
  text-align: center;
  color: white;
  position: relative;
  z-index: 1;
}

.hero-icon {
  font-size: 4rem;
  margin-bottom: 1.5rem;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.hero-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 0.75rem;
}

.hero-subtitle {
  font-size: 1rem;
  opacity: 0.9;
  margin-bottom: 2rem;
}

.hero-features {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.feature {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 0.75rem 1.5rem;
  background: rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-full);
  backdrop-filter: blur(10px);
}

.feature-icon {
  font-size: 1.25rem;
}

.login-form-container {
  flex: 1;
  padding: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
}

.form-header {
  text-align: center;
  margin-bottom: 2rem;
}

.form-header h2 {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.form-header p {
  color: var(--text-muted);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.submit-btn {
  margin-top: 0.5rem;
  width: 100%;
  position: relative;
}

.submit-btn .spinner {
  position: absolute;
  left: 1.5rem;
  width: 1.25rem;
  height: 1.25rem;
}

.form-footer {
  text-align: center;
  margin-top: 1.5rem;
  color: var(--text-muted);
}

.form-footer a {
  color: var(--primary);
  font-weight: 500;
}

.form-footer a:hover {
  color: var(--primary-light);
}

.test-account {
  text-align: center;
  margin-top: 1.5rem;
  padding: 0.75rem;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    margin: 1rem;
    min-height: auto;
  }
  
  .login-hero {
    padding: 2rem;
  }
  
  .hero-features {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .login-form-container {
    padding: 2rem;
  }
}
</style>
