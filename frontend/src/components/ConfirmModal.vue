<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="confirm-overlay" @click.self="handleCancel">
        <div class="confirm-modal">
          <div class="confirm-icon" :class="iconClass">
            {{ icon }}
          </div>
          <h3 class="confirm-title">{{ title }}</h3>
          <p class="confirm-message">{{ message }}</p>
          <div class="confirm-actions">
            <button class="btn btn-secondary" @click="handleCancel">取消</button>
            <button :class="['btn', confirmButtonClass]" @click="handleConfirm">
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'warning' // warning, danger, info
  },
  title: {
    type: String,
    default: '确认操作'
  },
  message: {
    type: String,
    default: '确定要执行此操作吗？'
  },
  confirmText: {
    type: String,
    default: '确定'
  }
})

const emit = defineEmits(['confirm', 'cancel'])

const visible = ref(false)
let resolvePromise = null

const icon = computed(() => {
  const icons = {
    warning: '⚠️',
    danger: '🗑️',
    info: 'ℹ️'
  }
  return icons[props.type] || '⚠️'
})

const iconClass = computed(() => `icon-${props.type}`)

const confirmButtonClass = computed(() => {
  return props.type === 'danger' ? 'btn-danger' : 'btn-primary'
})

function show() {
  visible.value = true
  return new Promise((resolve) => {
    resolvePromise = resolve
  })
}

function handleConfirm() {
  visible.value = false
  emit('confirm')
  if (resolvePromise) resolvePromise(true)
}

function handleCancel() {
  visible.value = false
  emit('cancel')
  if (resolvePromise) resolvePromise(false)
}

defineExpose({ show })
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 1rem;
}

.confirm-modal {
  background: linear-gradient(145deg, #1e293b, #0f172a);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 1rem;
  padding: 2rem;
  text-align: center;
  max-width: 400px;
  width: 100%;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.confirm-icon {
  width: 4rem;
  height: 4rem;
  margin: 0 auto 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 2rem;
}

.icon-warning {
  background: rgba(245, 158, 11, 0.15);
}

.icon-danger {
  background: rgba(239, 68, 68, 0.15);
}

.icon-info {
  background: rgba(59, 130, 246, 0.15);
}

.confirm-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: #f8fafc;
}

.confirm-message {
  color: #94a3b8;
  margin-bottom: 1.5rem;
  line-height: 1.5;
}

.confirm-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
}

.confirm-actions .btn {
  min-width: 100px;
}

/* Transition */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.25s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .confirm-modal,
.modal-leave-to .confirm-modal {
  transform: scale(0.9);
}
</style>
