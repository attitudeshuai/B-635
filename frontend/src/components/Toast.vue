<template>
  <Transition name="toast">
    <div v-if="toast.visible" :class="['toast', `toast-${toast.type}`]">
      <span class="toast-icon">{{ icons[toast.type] }}</span>
      <span class="toast-message">{{ toast.message }}</span>
      <button class="toast-close" @click="toast.hide">×</button>
    </div>
  </Transition>
</template>

<script setup>
import { useToastStore } from '../stores/toast'

const toast = useToastStore()

const icons = {
  success: '✓',
  error: '✕',
  warning: '⚠',
  info: 'ℹ'
}
</script>

<style scoped>
.toast {
  position: fixed;
  top: 1.5rem;
  right: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-lg);
  z-index: 2000;
  min-width: 280px;
  max-width: 400px;
}

.toast-icon {
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 0.875rem;
  font-weight: bold;
}

.toast-success .toast-icon {
  background: rgba(16, 185, 129, 0.2);
  color: var(--success);
}

.toast-error .toast-icon {
  background: rgba(239, 68, 68, 0.2);
  color: var(--danger);
}

.toast-warning .toast-icon {
  background: rgba(245, 158, 11, 0.2);
  color: var(--warning);
}

.toast-info .toast-icon {
  background: rgba(59, 130, 246, 0.2);
  color: var(--info);
}

.toast-message {
  flex: 1;
  font-size: 0.9375rem;
}

.toast-close {
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  color: var(--text-muted);
  font-size: 1.25rem;
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.toast-close:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

/* Transitions */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100px);
}
</style>
