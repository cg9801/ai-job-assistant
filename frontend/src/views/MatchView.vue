<script setup>
import { ref } from 'vue'
import { matchJobs, analyzeMatch } from '../api'

const topK = ref(5)
const loading = ref(false)
const results = ref(null)
const analyzeLoading = ref(false)
const analyzeJobId = ref('')
const analyzeResult = ref(null)

function scoreLevel(score) {
  return score >= 70 ? 'high' : score >= 40 ? 'mid' : 'low'
}

function splitSkills(s) {
  return s ? s.split(',').map(x => x.trim()).filter(Boolean) : []
}

async function doMatch() {
  loading.value = true
  results.value = null
  analyzeResult.value = null
  try {
    const res = await matchJobs(topK.value)
    if (res.data.code === 200 && res.data.data?.length) {
      results.value = res.data.data
    } else {
      window.__toast?.(res.data.message || '未找到匹配结果，请先上传简历', 'error')
      results.value = []
    }
  } catch (e) {
    window.__toast?.(e.message, 'error')
    results.value = []
  } finally {
    loading.value = false
  }
}

async function doAnalyze(jobId) {
  const id = jobId || analyzeJobId.value
  if (!id) { window.__toast?.('请输入职位 ID', 'error'); return }
  analyzeLoading.value = true
  analyzeResult.value = null
  try {
    const res = await analyzeMatch(id)
    if (res.data.code === 200) {
      analyzeResult.value = res.data.data
    } else {
      window.__toast?.(res.data.message || '分析失败', 'error')
    }
  } catch (e) {
    window.__toast?.(e.message, 'error')
  } finally {
    analyzeLoading.value = false
  }
}
</script>

<template>
  <div>
    <h1>🎯 智能匹配</h1>
    <p class="subtitle" style="color:var(--muted);margin-bottom:28px;font-size:.9rem">
      基于 RAG 语义检索，为你推荐最匹配的职位
    </p>

    <div class="card">
      <h2><span class="dot"></span>匹配设置</h2>
      <div class="form-row">
        <div>
          <label>推荐数量 (Top K)</label>
          <input v-model.number="topK" type="number" min="1" max="20">
        </div>
        <div style="display:flex;align-items:flex-end">
          <button class="btn btn-primary" :disabled="loading" @click="doMatch">
            <span v-if="loading" class="spinner"></span>
            🔍 开始匹配
          </button>
        </div>
      </div>
    </div>

    <!-- Results -->
    <div v-if="loading" class="card" style="text-align:center;padding:40px">
      <span class="spinner"></span>
      <p style="margin-top:12px;color:var(--muted)">正在匹配中，请稍候...</p>
    </div>

    <template v-if="results?.length">
      <div v-for="(r, i) in results" :key="i" class="match-card">
        <div class="match-header">
          <div>
            <div class="match-title">{{ r.job?.title || '未知岗位' }}</div>
            <div class="match-company">{{ r.job?.company }} · {{ r.job?.location }} · {{ r.job?.salaryRange }}</div>
          </div>
          <div class="match-score" :class="scoreLevel(r.matchScore)">{{ r.matchScore }}分</div>
        </div>
        <div class="match-bar">
          <div class="fill" :class="scoreLevel(r.matchScore)" :style="{ width: r.matchScore + '%' }"></div>
        </div>
        <div class="match-meta">
          <span v-if="r.job?.education" class="tag">🎓 {{ r.job.education }}</span>
          <span v-if="r.job?.experience" class="tag">💼 {{ r.job.experience }}</span>
          <span v-if="r.job?.id" class="tag">🆔 #{{ r.job.id }}</span>
        </div>
        <template v-if="splitSkills(r.matchedSkills).length">
          <div style="margin-top:10px;font-size:.82rem;color:var(--green)">✅ 匹配技能</div>
          <div class="skill-tags">
            <span v-for="s in splitSkills(r.matchedSkills)" :key="s" class="skill-tag matched">{{ s }}</span>
          </div>
        </template>
        <template v-if="splitSkills(r.missingSkills).length">
          <div style="margin-top:6px;font-size:.82rem;color:var(--red)">❌ 缺失技能</div>
          <div class="skill-tags">
            <span v-for="s in splitSkills(r.missingSkills)" :key="s" class="skill-tag missing">{{ s }}</span>
          </div>
        </template>
        <div v-if="r.advice" class="match-advice">💡 {{ r.advice }}</div>
        <div v-if="r.job?.id" style="margin-top:12px">
          <button class="btn btn-sm btn-secondary" @click="doAnalyze(r.job.id)">🧠 深度分析此职位</button>
        </div>
      </div>
    </template>

    <!-- Analyze -->
    <div class="card" style="margin-top:20px">
      <h2><span class="dot"></span>深度分析</h2>
      <div class="form-row">
        <div>
          <label>职位 ID</label>
          <input v-model="analyzeJobId" type="number" placeholder="输入职位 ID">
        </div>
        <div style="display:flex;align-items:flex-end">
          <button class="btn btn-primary" :disabled="analyzeLoading" @click="doAnalyze()">
            <span v-if="analyzeLoading" class="spinner"></span>
            🧠 深度分析
          </button>
        </div>
      </div>
    </div>

    <div v-if="analyzeLoading" class="card" style="text-align:center;padding:40px">
      <span class="spinner"></span>
      <p style="margin-top:12px;color:var(--muted)">AI 深度分析中，请稍候...</p>
    </div>

    <div v-if="analyzeResult" class="card">
      <div class="analyze-header">
        <div>
          <h2 style="margin:0">{{ analyzeResult.job?.title || '未知岗位' }}</h2>
          <div style="color:var(--muted);font-size:.85rem;margin-top:4px">
            {{ analyzeResult.job?.company }} · {{ analyzeResult.job?.location }}
          </div>
        </div>
        <div class="match-score" :class="scoreLevel(analyzeResult.matchScore)" style="font-size:2rem">
          {{ analyzeResult.matchScore }}分
        </div>
      </div>
      <div class="match-bar" style="height:8px">
        <div class="fill" :class="scoreLevel(analyzeResult.matchScore)" :style="{ width: analyzeResult.matchScore + '%' }"></div>
      </div>

      <div v-if="splitSkills(analyzeResult.matchedSkills).length" class="analyze-section">
        <h3>✅ 匹配的技术栈</h3>
        <div class="skill-tags">
          <span v-for="s in splitSkills(analyzeResult.matchedSkills)" :key="s" class="skill-tag matched">{{ s }}</span>
        </div>
      </div>

      <div v-if="splitSkills(analyzeResult.missingSkills).length" class="analyze-section">
        <h3>❌ 缺失的技术栈</h3>
        <div class="skill-tags">
          <span v-for="s in splitSkills(analyzeResult.missingSkills)" :key="s" class="skill-tag missing">{{ s }}</span>
        </div>
      </div>

      <div v-if="analyzeResult.advice" class="analyze-section">
        <h3>💡 AI 建议</h3>
        <div class="analyze-content" style="white-space:pre-wrap">{{ analyzeResult.advice }}</div>
      </div>
    </div>
  </div>
</template>
