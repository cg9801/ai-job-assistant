<script setup>
import { ref, nextTick } from 'vue'
import { sendChat } from '../api'

const input = ref('')
const loading = ref(false)
const messagesEl = ref(null)

const messages = ref([
  { role: 'ai', text: '你好！我是你的 AI 求职助手。你可以问我：\n• "我适合投什么岗位？"\n• "帮我分析一下我和 Java 开发岗位的匹配度"\n• "我还需要学习哪些技术？"\n• "给我一些面试准备建议"' }
])

const quickQuestions = [
  { icon: '🎯', text: '我适合投什么岗位？' },
  { icon: '📚', text: '我还需要学习哪些技术来提升竞争力？' },
  { icon: '🎤', text: '给我一些面试准备建议' },
  { icon: '📋', text: '帮我做一个求职规划' },
  { icon: '📊', text: '分析一下当前就业市场的热门技术方向' },
]

function scrollBottom() {
  nextTick(() => {
    if (messagesEl.value) messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  })
}

async function doSend(question) {
  const q = question || input.value.trim()
  if (!q || loading.value) return
  input.value = ''

  messages.value.push({ role: 'user', text: q })
  scrollBottom()

  loading.value = true
  try {
    const res = await sendChat(q)
    if (res.data.code === 200) {
      messages.value.push({ role: 'ai', text: res.data.data })
    } else {
      messages.value.push({ role: 'ai', text: '❌ ' + (res.data.message || '请求失败') })
    }
  } catch (e) {
    messages.value.push({ role: 'ai', text: '❌ ' + e.message })
  } finally {
    loading.value = false
    scrollBottom()
  }
}

function onKey(e) { if (e.key === 'Enter') doSend() }
</script>

<template>
  <div>
    <h1>💬 AI 求职问答</h1>
    <p class="subtitle" style="color:var(--muted);margin-bottom:28px;font-size:.9rem">
      基于你的简历和职位库，AI 提供个性化的求职建议
    </p>

    <div class="card" style="padding:0;overflow:hidden">
      <div class="chat-box">
        <div ref="messagesEl" class="chat-messages">
          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="chat-msg"
            :class="msg.role"
          >
            <div v-if="msg.role === 'ai'" class="label">🤖 AI 助手</div>
            <template v-if="msg.role === 'ai'">{{ msg.text }}</template>
            <template v-else>{{ msg.text }}</template>
          </div>
          <div v-if="loading" class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
        <div class="chat-input-area">
          <input
            v-model="input"
            type="text"
            placeholder="输入你的问题..."
            @keydown="onKey"
          >
          <button class="btn btn-primary" :disabled="loading" @click="doSend()">发送</button>
        </div>
      </div>
    </div>

    <div class="card" style="margin-top:12px">
      <h2><span class="dot"></span>快捷问题</h2>
      <div style="display:flex;flex-wrap:wrap;gap:8px">
        <button
          v-for="q in quickQuestions"
          :key="q.text"
          class="btn btn-sm btn-secondary"
          @click="doSend(q.text)"
        >{{ q.icon }} {{ q.text }}</button>
      </div>
    </div>
  </div>
</template>
