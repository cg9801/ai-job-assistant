<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { apiBase, username, apiConnected, checkHealth } from './api'
import Toast from './components/Toast.vue'

const router = useRouter()
const route = useRoute()

const navItems = [
  { path: '/resume', icon: '📄', label: '简历管理' },
  { path: '/match', icon: '🎯', label: '智能匹配' },
  { path: '/jobs', icon: '💼', label: '职位库' },
  { path: '/chat', icon: '💬', label: 'AI 问答' },
]

let healthTimer = null
onMounted(() => {
  checkHealth()
  healthTimer = setInterval(checkHealth, 15000)
})
onUnmounted(() => clearInterval(healthTimer))

function go(path) {
  router.push(path)
}
</script>

<template>
  <div class="app-layout">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="logo">
        <div class="logo-icon">🎯</div>
        <span class="logo-text">AI 求职助手</span>
      </div>
      <nav class="nav">
        <div
          v-for="item in navItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: route.path === item.path }"
          @click="go(item.path)"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-label">{{ item.label }}</span>
        </div>
      </nav>
      <div class="sidebar-footer">Powered by Spring AI</div>
    </aside>

    <!-- Main -->
    <div class="main-area">
      <!-- Top bar -->
      <div class="api-bar">
        <label>后端地址</label>
        <input v-model="apiBase" type="text" class="api-input" placeholder="http://localhost:8080">
        <div class="api-status" :class="{ ok: apiConnected }"></div>
        <label class="ml">用户名</label>
        <input v-model="username" type="text" class="username-input" placeholder="用户名">
      </div>

      <!-- Router view -->
      <div class="page-container">
        <router-view />
      </div>
    </div>
  </div>
  <Toast />
</template>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
}

/* Sidebar */
.sidebar {
  width: 240px;
  background: var(--surface);
  border-right: 1px solid var(--border);
  padding: 24px 0;
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  z-index: 10;
}
.logo {
  padding: 0 24px 24px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  gap: 12px;
}
.logo-icon {
  width: 36px; height: 36px; border-radius: 10px;
  background: linear-gradient(135deg, var(--accent), var(--accent2));
  display: flex; align-items: center; justify-content: center;
  font-size: 1.1rem; color: #fff;
}
.logo-text {
  font-size: 1.3rem; font-weight: 700;
  background: linear-gradient(135deg, var(--accent), var(--accent2));
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.nav {
  flex: 1; padding: 16px 12px;
  display: flex; flex-direction: column; gap: 4px;
}
.nav-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 16px; border-radius: 8px; cursor: pointer;
  color: var(--muted); transition: all .2s; font-size: .93rem; user-select: none;
}
.nav-item:hover { background: rgba(108,92,231,.1); color: var(--text); }
.nav-item.active { background: rgba(108,92,231,.15); color: var(--accent2); font-weight: 600; }
.nav-icon { font-size: 1.15rem; width: 24px; text-align: center; }
.sidebar-footer {
  padding: 16px 20px; font-size: .75rem; color: var(--muted);
}

/* Main area */
.main-area {
  flex: 1; margin-left: 240px; display: flex; flex-direction: column;
}
.page-container {
  padding: 32px; max-width: 960px; width: 100%;
}

/* API bar */
.api-bar {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 20px; background: var(--surface);
  border-bottom: 1px solid var(--border);
  position: sticky; top: 0; z-index: 8;
}
.api-bar label { margin: 0; white-space: nowrap; font-size: .82rem; }
.api-input { max-width: 280px; padding: 6px 12px; font-size: .82rem; }
.username-input { max-width: 160px; padding: 6px 12px; font-size: .82rem; }
.ml { margin-left: 16px; }
.api-status {
  width: 8px; height: 8px; border-radius: 50%; background: var(--red);
}
.api-status.ok { background: var(--green); }

/* Responsive */
@media(max-width:768px) {
  .sidebar { width: 64px; }
  .logo-text, .nav-label { display: none; }
  .nav-item { justify-content: center; padding: 12px; }
  .main-area { margin-left: 64px; }
}
</style>
