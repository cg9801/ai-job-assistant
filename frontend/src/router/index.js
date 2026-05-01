import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/resume' },
  { path: '/resume', name: 'Resume', component: () => import('../views/ResumeView.vue'), meta: { icon: '📄', title: '简历管理' } },
  { path: '/match', name: 'Match', component: () => import('../views/MatchView.vue'), meta: { icon: '🎯', title: '智能匹配' } },
  { path: '/jobs', name: 'Jobs', component: () => import('../views/JobsView.vue'), meta: { icon: '💼', title: '职位库' } },
  { path: '/chat', name: 'Chat', component: () => import('../views/ChatView.vue'), meta: { icon: '💬', title: 'AI 问答' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
