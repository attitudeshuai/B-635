<template>
  <div class="profile-page">
    <div class="profile-grid">
      <div class="card profile-card">
        <div class="profile-header">
          <div class="avatar">
            {{ userInitial }}
          </div>
          <div class="profile-info">
            <h2>{{ authStore.user?.nickname || authStore.user?.username }}</h2>
            <p class="email">{{ authStore.user?.email }}</p>
          </div>
        </div>
        
        <form @submit.prevent="updateProfile" class="profile-form">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <input type="text" :value="authStore.user?.username" class="form-input" disabled />
          </div>
          
          <div class="form-group">
            <label class="form-label">昵称</label>
            <input type="text" v-model="profileForm.nickname" class="form-input" placeholder="请输入昵称" />
          </div>
          
          <div class="form-group">
            <label class="form-label">邮箱</label>
            <input type="email" v-model="profileForm.email" class="form-input" placeholder="请输入邮箱" />
          </div>
          
          <button type="submit" class="btn btn-primary" :disabled="loading">
            {{ loading ? '保存中...' : '保存更改' }}
          </button>
        </form>
      </div>
      
      <div class="card password-card">
        <h3 class="card-title">修改密码</h3>
        
        <form @submit.prevent="changePassword" class="password-form">
          <div class="form-group">
            <label class="form-label">当前密码</label>
            <input type="password" v-model="passwordForm.oldPassword" class="form-input" required />
          </div>
          
          <div class="form-group">
            <label class="form-label">新密码</label>
            <input type="password" v-model="passwordForm.newPassword" class="form-input" minlength="6" required />
          </div>
          
          <div class="form-group">
            <label class="form-label">确认新密码</label>
            <input type="password" v-model="passwordForm.confirmPassword" class="form-input" required />
          </div>
          
          <button type="submit" class="btn btn-secondary" :disabled="passwordLoading">
            {{ passwordLoading ? '修改中...' : '修改密码' }}
          </button>
        </form>
      </div>
    </div>
    
    <div class="card danger-zone">
      <h3 class="card-title text-danger">危险操作</h3>
      <p class="text-muted mb-4">退出登录将清除本地登录状态，需要重新登录才能继续使用。</p>
      <button class="btn btn-danger" @click="handleLogout">退出登录</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useToastStore } from '../stores/toast'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToastStore()

const loading = ref(false)
const passwordLoading = ref(false)

const profileForm = reactive({
  nickname: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const userInitial = computed(() => {
  const name = authStore.user?.nickname || authStore.user?.username || ''
  return name.charAt(0).toUpperCase()
})

onMounted(() => {
  if (authStore.user) {
    profileForm.nickname = authStore.user.nickname || ''
    profileForm.email = authStore.user.email || ''
  }
})

async function updateProfile() {
  loading.value = true
  try {
    const result = await authStore.updateUser({
      nickname: profileForm.nickname,
      email: profileForm.email
    })
    if (result.success) {
      toast.success('个人信息已更新')
    } else {
      toast.error(result.message || '更新失败')
    }
  } catch (error) {
    toast.error(error.response?.data?.message || '更新失败')
  } finally {
    loading.value = false
  }
}

async function changePassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    toast.error('两次输入的密码不一致')
    return
  }
  
  if (passwordForm.newPassword.length < 6) {
    toast.error('新密码长度至少为6位')
    return
  }
  
  passwordLoading.value = true
  try {
    const result = await authStore.changePassword(passwordForm.oldPassword, passwordForm.newPassword)
    if (result.success) {
      toast.success('密码修改成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      toast.error(result.message || '密码修改失败')
    }
  } catch (error) {
    toast.error(error.response?.data?.message || '密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
  toast.info('已退出登录')
}
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.profile-card,
.password-card {
  padding: 2rem;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid var(--border-color);
}

.avatar {
  width: 5rem;
  height: 5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  border-radius: 50%;
  font-size: 2rem;
  font-weight: 700;
}

.profile-info h2 {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
}

.profile-info .email {
  color: var(--text-muted);
}

.card-title {
  font-size: 1.25rem;
  margin-bottom: 1.5rem;
}

.profile-form,
.password-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.danger-zone {
  padding: 2rem;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
