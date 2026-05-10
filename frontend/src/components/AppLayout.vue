<template>
  <div class="app-layout">
    <Sidebar />
    <main class="main-content">
      <header class="main-header">
        <div class="header-left">
          <h1 class="page-title">{{ pageTitle }}</h1>
        </div>
        <div class="header-right">
          <div class="user-menu" @click="toggleDropdown" ref="dropdownRef">
            <div class="user-avatar">
              {{ userInitial }}
            </div>
            <span class="user-name">{{ authStore.user?.nickname || authStore.user?.username }}</span>
            <span class="dropdown-arrow">▼</span>
            <Transition name="dropdown">
              <div v-if="showDropdown" class="dropdown-menu">
                <router-link to="/profile" class="dropdown-item">
                  <span>👤</span> 个人设置
                </router-link>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item logout" @click="handleLogout">
                  <span>🚪</span> 退出登录
                </button>
              </div>
            </Transition>
          </div>
        </div>
      </header>
      <div class="page-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Sidebar from './Sidebar.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const showDropdown = ref(false)
const dropdownRef = ref(null)

const pageTitle = computed(() => {
  const titles = {
    Dashboard: '仪表盘',
    Exercises: '运动记录',
    Goals: '运动目标',
    Stats: '数据统计',
    Profile: '个人设置'
  }
  return titles[route.name] || '运动管理'
})

const userInitial = computed(() => {
  const name = authStore.user?.nickname || authStore.user?.username || ''
  return name.charAt(0).toUpperCase()
})

function toggleDropdown() {
  showDropdown.value = !showDropdown.value
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

function handleClickOutside(e) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 260px;
}

.main-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 2rem;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 1rem;
  background: var(--bg-tertiary);
  border-radius: var(--radius-lg);
  cursor: pointer;
  position: relative;
  transition: all var(--transition-fast);
}

.user-menu:hover {
  background: var(--gray-600);
}

.user-avatar {
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary), var(--secondary));
  border-radius: 50%;
  font-weight: 600;
  font-size: 0.875rem;
}

.user-name {
  font-weight: 500;
}

.dropdown-arrow {
  font-size: 0.625rem;
  color: var(--text-muted);
  transition: transform var(--transition-fast);
}

.user-menu:hover .dropdown-arrow {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  min-width: 180px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  color: var(--text-primary);
  text-decoration: none;
  transition: all var(--transition-fast);
  cursor: pointer;
  background: none;
  border: none;
  width: 100%;
  font-size: 0.9375rem;
}

.dropdown-item:hover {
  background: var(--bg-tertiary);
}

.dropdown-item.logout {
  color: var(--danger);
}

.dropdown-divider {
  height: 1px;
  background: var(--border-color);
  margin: 0.25rem 0;
}

.page-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}

/* Dropdown Transition */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
  }
  
  .main-header {
    padding: 1rem;
  }
  
  .page-content {
    padding: 1rem;
  }
  
  .user-name {
    display: none;
  }
}
</style>
