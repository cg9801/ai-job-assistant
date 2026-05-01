<script setup>
import { ref } from 'vue'
import { uploadResume } from '../api'

const currentFile = ref(null)
const uploading = ref(false)
const result = ref(null)
const dragover = ref(false)

function onFileChange(e) {
  const file = e.target.files?.[0]
  if (file) selectFile(file)
}
function onDrop(e) {
  e.preventDefault()
  dragover.value = false
  const file = e.dataTransfer.files?.[0]
  if (file) selectFile(file)
}
function selectFile(file) {
  currentFile.value = file
  result.value = null
}

function formatSize(bytes) {
  return (bytes / 1024).toFixed(1) + ' KB'
}

async function doUpload() {
  if (!currentFile.value) return
  uploading.value = true
  try {
    const res = await uploadResume(currentFile.value)
    if (res.data.code === 200) {
      result.value = res.data.data
      window.__toast?.('简历上传成功！', 'success')
    } else {
      window.__toast?.(res.data.message || '上传失败', 'error')
    }
  } catch (e) {
    window.__toast?.(e.message, 'error')
  } finally {
    uploading.value = false
  }
}

function getSkills(skills) {
  if (!skills) return []
  return skills.split(',').map(s => s.trim()).filter(Boolean)
}
</script>

<template>
  <div>
    <h1>📄 简历管理</h1>
    <p class="subtitle" style="color:var(--muted);margin-bottom:28px;font-size:.9rem">
      上传简历文件，系统将自动解析内容并提取技能标签
    </p>

    <div class="card">
      <h2><span class="dot"></span>上传简历</h2>
      <div
        class="upload-zone"
        :class="{ dragover }"
        @dragover.prevent="dragover = true"
        @dragleave="dragover = false"
        @drop="onDrop"
      >
        <input type="file" accept=".pdf,.docx,.doc,.txt" @change="onFileChange">
        <div class="icon">📎</div>
        <p>点击或拖拽文件到此处</p>
        <p style="font-size:.8rem;margin-top:6px">支持 PDF、DOCX、TXT 格式</p>
      </div>

      <div v-if="currentFile" class="file-info">
        <span>📄</span>
        <span class="name">{{ currentFile.name }}</span>
        <span class="size">{{ formatSize(currentFile.size) }}</span>
      </div>

      <div class="btn-group">
        <button class="btn btn-primary" :disabled="!currentFile || uploading" @click="doUpload">
          <span v-if="uploading" class="spinner"></span>
          {{ uploading ? '解析中...' : '🚀 上传并解析' }}
        </button>
      </div>
    </div>

    <div v-if="result" class="card">
      <h2><span class="dot"></span>解析结果</h2>
      <div class="form-row">
        <div>
          <label>用户名</label>
          <div style="padding:8px 0;font-size:.93rem">{{ result.username }}</div>
        </div>
        <div>
          <label>文件名</label>
          <div style="padding:8px 0;font-size:.93rem">{{ result.fileName }}</div>
        </div>
      </div>
      <div class="form-group">
        <label>提取的技能标签</label>
        <div class="skill-tags">
          <span v-for="s in getSkills(result.skills)" :key="s" class="skill-tag matched">{{ s }}</span>
        </div>
      </div>
      <div class="form-group">
        <label>简历内容摘要</label>
        <div style="padding:12px 16px;background:var(--surface);border-radius:8px;font-size:.88rem;max-height:200px;overflow-y:auto;line-height:1.8;white-space:pre-wrap">
          {{ (result.content || '').substring(0, 1500) }}{{ (result.content || '').length > 1500 ? '...' : '' }}
        </div>
      </div>
    </div>
  </div>
</template>
