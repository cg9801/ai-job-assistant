<script setup>
import { ref, onMounted } from 'vue'

const toasts = ref([])
let id = 0

function add(msg, type = 'info') {
  const t = { id: ++id, msg, type }
  toasts.value.push(t)
  setTimeout(() => {
    toasts.value = toasts.value.filter(x => x.id !== t.id)
  }, 3000)
}

onMounted(() => {
  window.__toast = add
})
</script>

<template>
  <div class="toast-container">
    <div v-for="t in toasts" :key="t.id" class="toast" :class="t.type">{{ t.msg }}</div>
  </div>
</template>
