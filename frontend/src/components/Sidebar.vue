<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="logo">
        <span class="logo-icon">🏃</span>
        <span class="logo-text">运动管理</span>
      </div>
    </div>
    
    <nav class="sidebar-nav">
      <router-link 
        v-for="item in menuItems" 
        :key="item.path"
        :to="item.path" 
        class="nav-item"
        :class="{ active: isActive(item.path) }"
      >
        <span class="nav-icon">{{ item.icon }}</span>
        <span class="nav-text">{{ item.label }}</span>
      </router-link>
    </nav>
    
    <div class="sidebar-footer">
      <div class="version">v1.0.0</div>
    </div>
  </aside>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()

const menuItems = [
  { path: '/', icon: '📊', label: '仪表盘' },
  { path: '/exercises', icon: '🏋️', label: '运动记录' },
  { path: '/goals', icon: '🎯', label: '运动目标' },
  { path: '/stats', icon: '📈', label: '数据统计' },
  { path: '/profile', icon: '👤', label: '个人设置' }
]

function isActive(path) {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}
</script>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 260px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  z-index: 200;
}

.sidebar-header {
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.logo-icon {
  font-size: 1.75rem;
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
  background: linear-gradient(135deg, var(--primary-light), var(--secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sidebar-nav {
  flex: 1;
  padding: 1rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.875rem;
  padding: 0.875rem 1rem;
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  text-decoration: none;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.nav-item:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.nav-item.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(16, 185, 129, 0.1));
  color: var(--primary-light);
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.nav-icon {
  font-size: 1.25rem;
  width: 1.5rem;
  text-align: center;
}

.nav-text {
  font-size: 0.9375rem;
}

.sidebar-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--border-color);
}

.version {
  font-size: 0.75rem;
  color: var(--text-muted);
  text-align: center;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
</style>
