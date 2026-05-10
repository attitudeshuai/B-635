import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useToastStore = defineStore('toast', () => {
  const message = ref('')
  const type = ref('info') // success, error, warning, info
  const visible = ref(false)

  function show(msg, t = 'info') {
    message.value = msg
    type.value = t
    visible.value = true
    
    setTimeout(() => {
      visible.value = false
    }, 3000)
  }

  function success(msg) {
    show(msg, 'success')
  }

  function error(msg) {
    show(msg, 'error')
  }

  function warning(msg) {
    show(msg, 'warning')
  }

  function info(msg) {
    show(msg, 'info')
  }

  function hide() {
    visible.value = false
  }

  return {
    message,
    type,
    visible,
    show,
    success,
    error,
    warning,
    info,
    hide
  }
})
