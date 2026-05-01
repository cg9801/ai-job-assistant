<script>
import { ref } from 'vue'

const toasts = ref([])
let id = 0

function add(msg, type = 'info') {
  const t = { id: ++id, msg, type }
  toasts.value.push(t)
  setTimeout(() => {
    toasts.value = toasts.value.filter(x => x.id !== t.id)
  }, 3000)
}

// Expose globally
if (typeof window !== 'undefined') {
  window.__toast = add
}

export default {
  name: 'Toast',
  setup() {
    return { toasts }
  }
}
</script>

<template>
  <div class="toast-container">
    <div v-for="t in toasts" :key="t.id" class="toast" :class="t.type">{{ t.msg }}</div>
  </div>
</template>
