import axios from 'axios'
import { ref } from 'vue'

// Reactive API base URL
export const apiBase = ref('http://localhost:8080')
export const username = ref('zhimai')
export const apiConnected = ref(false)

const http = axios.create({ timeout: 30000 })

// Update base URL dynamically
http.interceptors.request.use(config => {
  config.baseURL = apiBase.value
  return config
})

// Health check
export async function checkHealth() {
  try {
    const res = await http.get('/api/jobs')
    apiConnected.value = res.data?.code === 200
  } catch {
    apiConnected.value = false
  }
}

// ─── Resume ───
export function uploadResume(file) {
  const fd = new FormData()
  fd.append('file', file)
  return http.post(`/api/resume/upload?username=${username.value}`, fd)
}

// ─── Match ───
export function matchJobs(topK = 5) {
  return http.get('/api/match', { params: { username: username.value, topK } })
}

export function analyzeMatch(jobId) {
  return http.get('/api/analyze', { params: { username: username.value, jobId } })
}

// ─── Chat ───
export function sendChat(question) {
  return http.post('/api/chat', null, { params: { username: username.value, question } })
}

// ─── Jobs ───
export function getAllJobs() {
  return http.get('/api/jobs')
}

export function searchJobs(keyword) {
  return http.get('/api/jobs/search', { params: { keyword } })
}

export function addJob(job) {
  return http.post('/api/jobs', job)
}

export function batchImportJobs(jobs) {
  return http.post('/api/jobs/batch', jobs)
}
