import { ref } from 'vue'

// Biến toasts đặt bên ngoài function để nó trở thành Global State (Dùng chung cho toàn App)
const toasts = ref([])
let toastId = 0

export function useToast() {
  // type có thể là: 'success' (xanh), 'error' (đỏ), 'warning' (vàng)
  const addToast = (message, type = 'success', duration = 3000) => {
    const id = toastId++
    toasts.value.push({ id, message, type })

    // Tự động tắt sau [duration] mili giây
    setTimeout(() => {
      removeToast(id)
    }, duration)
  }

  const removeToast = (id) => {
    toasts.value = toasts.value.filter(toast => toast.id !== id)
  }

  return { toasts, addToast, removeToast }
}