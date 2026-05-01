<script setup>
import { ref, computed } from 'vue'
import { getAllJobs, searchJobs, addJob } from '../api'

const jobs = ref(null)
const keyword = ref('')
const loading = ref(false)
const showModal = ref(false)
const detailJob = ref(null)

// Add job form
const form = ref({
  title: '', company: '', location: '', salaryRange: '',
  education: '', experience: '', techStack: '', description: ''
})

async function loadAll() {
  loading.value = true
  try {
    const res = await getAllJobs()
    if (res.data.code === 200) jobs.value = res.data.data
    else window.__toast?.(res.data.message, 'error')
  } catch (e) {
    window.__toast?.(e.message, 'error')
  } finally { loading.value = false }
}

async function doSearch() {
  if (!keyword.value.trim()) { loadAll(); return }
  loading.value = true
  try {
    const res = await searchJobs(keyword.value)
    if (res.data.code === 200) jobs.value = res.data.data
    else window.__toast?.(res.data.message, 'error')
  } catch (e) {
    window.__toast?.(e.message, 'error')
  } finally { loading.value = false }
}

function onSearchKey(e) { if (e.key === 'Enter') doSearch() }

function showDetail(job) {
  detailJob.value = job
  showModal.value = true
}

function getTechs(s) {
  return s ? s.split(',').map(x => x.trim()).filter(Boolean) : []
}

const jobCount = computed(() => jobs.value?.length ?? null)

async function doAdd() {
  if (!form.value.title.trim()) { window.__toast?.('请填写岗位名称', 'error'); return }
  try {
    const res = await addJob(form.value)
    if (res.data.code === 200) {
      window.__toast?.('职位添加成功！', 'success')
      Object.keys(form.value).forEach(k => form.value[k] = '')
    } else {
      window.__toast?.(res.data.message || '添加失败', 'error')
    }
  } catch (e) { window.__toast?.(e.message, 'error') }
}
</script>

<template>
  <div>
    <h1>💼 职位库</h1>
    <p class="subtitle" style="color:var(--muted);margin-bottom:28px;font-size:.9rem">
      浏览、搜索和管理职位信息
    </p>

    <!-- Search -->
    <div class="card">
      <h2><span class="dot"></span>搜索职位</h2>
      <div style="display:flex;gap:12px">
        <input v-model="keyword" type="text" placeholder="输入关键词搜索..." style="flex:1" @keydown="onSearchKey">
        <button class="btn btn-primary" @click="doSearch">🔍 搜索</button>
        <button class="btn btn-secondary" @click="loadAll">📋 全部</button>
      </div>
    </div>

    <!-- Table -->
    <div class="card">
      <h2>
        <span class="dot"></span>职位列表
        <span v-if="jobCount !== null" style="font-size:.8rem;color:var(--muted);font-weight:400">({{ jobCount }} 条)</span>
      </h2>

      <div v-if="loading" style="text-align:center;padding:40px"><span class="spinner"></span></div>

      <div v-else-if="!jobs" class="empty">
        <div class="icon">💼</div>
        <p>点击"全部"加载职位列表</p>
      </div>

      <div v-else-if="!jobs.length" class="empty">
        <div class="icon">📭</div>
        <p>暂无职位数据</p>
      </div>

      <table v-else class="job-table">
        <thead>
          <tr><th>ID</th><th>岗位</th><th>公司</th><th>地点</th><th>薪资</th><th>技术栈</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="j in jobs" :key="j.id">
            <td>{{ j.id || '-' }}</td>
            <td class="title-cell">{{ j.title || '-' }}</td>
            <td>{{ j.company || '-' }}</td>
            <td>{{ j.location || '-' }}</td>
            <td>{{ j.salaryRange || '-' }}</td>
            <td class="tech-cell">
              <span v-for="t in getTechs(j.techStack).slice(0, 4)" :key="t" class="tech-badge">{{ t }}</span>
              <span v-if="getTechs(j.techStack).length > 4" class="tech-badge">+{{ getTechs(j.techStack).length - 4 }}</span>
            </td>
            <td><button class="btn btn-sm btn-secondary" @click="showDetail(j)">详情</button></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Add Job -->
    <div class="card">
      <h2><span class="dot"></span>添加职位</h2>
      <div class="form-row">
        <div class="form-group">
          <label>岗位名称 *</label>
          <input v-model="form.title" type="text" placeholder="如：Java 后端开发">
        </div>
        <div class="form-group">
          <label>公司名称</label>
          <input v-model="form.company" type="text" placeholder="如：智迈科技">
        </div>
      </div>
      <div class="form-row three">
        <div class="form-group">
          <label>工作地点</label>
          <input v-model="form.location" type="text" placeholder="如：北京">
        </div>
        <div class="form-group">
          <label>薪资范围</label>
          <input v-model="form.salaryRange" type="text" placeholder="如：15-25K">
        </div>
        <div class="form-group">
          <label>学历要求</label>
          <input v-model="form.education" type="text" placeholder="如：本科">
        </div>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>经验要求</label>
          <input v-model="form.experience" type="text" placeholder="如：3-5年">
        </div>
        <div class="form-group">
          <label>技术栈（逗号分隔）</label>
          <input v-model="form.techStack" type="text" placeholder="如：Java,Spring,MySQL,Redis">
        </div>
      </div>
      <div class="form-group">
        <label>岗位描述</label>
        <textarea v-model="form.description" placeholder="粘贴完整的岗位描述..."></textarea>
      </div>
      <button class="btn btn-primary" @click="doAdd">➕ 添加职位</button>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <button class="modal-close" @click="showModal = false">✕</button>
        <template v-if="detailJob">
          <h2 style="margin-bottom:4px">{{ detailJob.title }}</h2>
          <div style="color:var(--muted);margin-bottom:20px">{{ detailJob.company }} · {{ detailJob.location }}</div>
          <div class="form-row" style="margin-bottom:16px">
            <div><label>薪资</label><div style="padding:6px 0">{{ detailJob.salaryRange || '-' }}</div></div>
            <div><label>学历</label><div style="padding:6px 0">{{ detailJob.education || '-' }}</div></div>
          </div>
          <div class="form-row" style="margin-bottom:16px">
            <div><label>经验</label><div style="padding:6px 0">{{ detailJob.experience || '-' }}</div></div>
            <div><label>来源</label><div style="padding:6px 0">{{ detailJob.source || '-' }}</div></div>
          </div>
          <div style="margin-bottom:16px">
            <label>技术栈</label>
            <div class="skill-tags">
              <span v-for="t in getTechs(detailJob.techStack)" :key="t" class="skill-tag matched">{{ t }}</span>
            </div>
          </div>
          <div>
            <label>岗位描述</label>
            <div style="padding:12px 16px;background:var(--surface);border-radius:8px;font-size:.88rem;line-height:1.8;max-height:300px;overflow-y:auto;white-space:pre-wrap">
              {{ detailJob.description || '暂无描述' }}
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
