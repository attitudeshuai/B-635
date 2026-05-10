<template>
  <div class="dashboard">
    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-card-icon" style="background: rgba(99, 102, 241, 0.15);">
          🏃
        </div>
        <div class="stat-card-value">{{ stats.totalExercises || 0 }}</div>
        <div class="stat-card-label">本月运动次数</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-card-icon" style="background: rgba(16, 185, 129, 0.15);">
          ⏱️
        </div>
        <div class="stat-card-value">{{ stats.totalDuration || 0 }}</div>
        <div class="stat-card-label">运动时长(分钟)</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-card-icon" style="background: rgba(245, 158, 11, 0.15);">
          🔥
        </div>
        <div class="stat-card-value">{{ formatNumber(stats.totalCalories || 0) }}</div>
        <div class="stat-card-label">消耗卡路里</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-card-icon" style="background: rgba(139, 92, 246, 0.15);">
          🎯
        </div>
        <div class="stat-card-value">{{ stats.activeGoals || 0 }}</div>
        <div class="stat-card-label">进行中目标</div>
      </div>
    </div>
    
    <div class="dashboard-grid">
      <!-- Chart Section -->
      <div class="card chart-card">
        <div class="card-header">
          <h3>卡路里消耗趋势</h3>
          <div class="chart-period">
            <button 
              v-for="period in ['7', '14', '30']" 
              :key="period"
              :class="['period-btn', { active: selectedPeriod === period }]"
              @click="changePeriod(period)"
            >
              {{ period }}天
            </button>
          </div>
        </div>
        <div class="chart-container">
          <Line v-if="chartData.labels.length" :data="chartData" :options="chartOptions" />
          <div v-else class="empty-state">
            <div class="empty-state-icon">📊</div>
            <p>暂无数据</p>
          </div>
        </div>
      </div>
      
      <!-- Active Goals -->
      <div class="card goals-card">
        <div class="card-header">
          <h3>进行中目标</h3>
          <router-link to="/goals" class="view-all">查看全部</router-link>
        </div>
        <div class="goals-list" v-if="activeGoals.length">
          <div v-for="goal in activeGoals" :key="goal.id" class="goal-item">
            <div class="goal-info">
              <span class="goal-icon">{{ getGoalIcon(goal.goalType) }}</span>
              <div class="goal-details">
                <div class="goal-title">{{ goal.title || getGoalTypeName(goal.goalType) }}</div>
                <div class="goal-progress-text">
                  {{ goal.currentValue }} / {{ goal.targetValue }} {{ getGoalUnit(goal.goalType) }}
                </div>
              </div>
            </div>
            <div class="goal-progress-bar">
              <div class="progress">
                <div class="progress-bar" :style="{ width: goal.progress + '%' }"></div>
              </div>
              <span class="progress-percent">{{ goal.progress }}%</span>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <div class="empty-state-icon">🎯</div>
          <p>暂无进行中目标</p>
          <router-link to="/goals" class="btn btn-primary btn-sm mt-3">创建目标</router-link>
        </div>
      </div>
    </div>
    
    <!-- Recent Exercises -->
    <div class="card recent-exercises">
      <div class="card-header">
        <h3>最近运动</h3>
        <router-link to="/exercises" class="view-all">查看全部</router-link>
      </div>
      <div class="exercises-list" v-if="recentExercises.length">
        <div v-for="exercise in recentExercises" :key="exercise.id" class="exercise-item">
          <div class="exercise-icon" :style="{ background: getTypeColor(exercise.typeColor) }">
            {{ exercise.typeIcon }}
          </div>
          <div class="exercise-info">
            <div class="exercise-name">{{ exercise.typeName }}</div>
            <div class="exercise-date">{{ formatDate(exercise.exerciseDate) }}</div>
          </div>
          <div class="exercise-stats">
            <div class="stat">
              <span class="stat-value">{{ exercise.durationMinutes }}</span>
              <span class="stat-label">分钟</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ exercise.caloriesBurned }}</span>
              <span class="stat-label">卡路里</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-state-icon">🏋️</div>
        <p>暂无运动记录</p>
        <router-link to="/exercises" class="btn btn-primary btn-sm mt-3">添加记录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'
import api from '../services/api'
import { useToastStore } from '../stores/toast'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler)

const toast = useToastStore()

const stats = reactive({
  totalExercises: 0,
  totalDuration: 0,
  totalCalories: 0,
  activeGoals: 0
})

const selectedPeriod = ref('7')
const trendData = ref([])
const activeGoals = ref([])
const recentExercises = ref([])

const chartData = computed(() => ({
  labels: trendData.value.map(d => d.date.slice(5)),
  datasets: [{
    label: '卡路里',
    data: trendData.value.map(d => d.calories),
    borderColor: '#6366f1',
    backgroundColor: 'rgba(99, 102, 241, 0.1)',
    fill: true,
    tension: 0.4,
    pointRadius: 4,
    pointHoverRadius: 6
  }]
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: '#1e293b',
      titleColor: '#f8fafc',
      bodyColor: '#94a3b8',
      borderColor: 'rgba(148, 163, 184, 0.1)',
      borderWidth: 1,
      padding: 12,
      displayColors: false,
      callbacks: {
        label: ctx => `消耗: ${ctx.raw} 卡路里`
      }
    }
  },
  scales: {
    x: {
      grid: { display: false },
      ticks: { color: '#64748b' }
    },
    y: {
      grid: { color: 'rgba(148, 163, 184, 0.1)' },
      ticks: { color: '#64748b' },
      beginAtZero: true
    }
  }
}

async function fetchStats() {
  try {
    const response = await api.get('/api/stats/overview')
    if (response.data.success) {
      Object.assign(stats, response.data.data)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

async function fetchTrend() {
  try {
    const response = await api.get(`/api/stats/trend?days=${selectedPeriod.value}`)
    if (response.data.success) {
      trendData.value = response.data.data
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

async function fetchGoals() {
  try {
    const response = await api.get('/api/goals')
    if (response.data.success) {
      activeGoals.value = response.data.data.filter(g => g.status === 'ACTIVE').slice(0, 3)
    }
  } catch (error) {
    console.error('获取目标失败:', error)
  }
}

async function fetchRecentExercises() {
  try {
    const response = await api.get('/api/exercises?page=0&size=5')
    if (response.data.success) {
      recentExercises.value = response.data.data.content || []
    }
  } catch (error) {
    console.error('获取运动记录失败:', error)
  }
}

function changePeriod(period) {
  selectedPeriod.value = period
  fetchTrend()
}

function formatNumber(num) {
  return num >= 1000 ? (num / 1000).toFixed(1) + 'k' : num
}

function formatDate(date) {
  return new Date(date).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

function getTypeColor(color) {
  return color ? `${color}20` : 'rgba(99, 102, 241, 0.15)'
}

function getGoalIcon(type) {
  const icons = { CALORIES: '🔥', DURATION: '⏱️', COUNT: '🔢' }
  return icons[type] || '🎯'
}

function getGoalTypeName(type) {
  const names = { CALORIES: '卡路里目标', DURATION: '时长目标', COUNT: '次数目标' }
  return names[type] || '目标'
}

function getGoalUnit(type) {
  const units = { CALORIES: '卡', DURATION: '分钟', COUNT: '次' }
  return units[type] || ''
}

onMounted(() => {
  fetchStats()
  fetchTrend()
  fetchGoals()
  fetchRecentExercises()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.5rem;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.card-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
}

.view-all {
  font-size: 0.875rem;
  color: var(--primary);
}

/* Chart */
.chart-card {
  min-height: 350px;
}

.chart-period {
  display: flex;
  gap: 0.5rem;
}

.period-btn {
  padding: 0.375rem 0.75rem;
  background: var(--bg-tertiary);
  border: none;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 0.8125rem;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.period-btn.active,
.period-btn:hover {
  background: var(--primary);
  color: white;
}

.chart-container {
  height: 250px;
}

/* Goals */
.goals-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.goal-item {
  padding: 1rem;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
}

.goal-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.goal-icon {
  font-size: 1.5rem;
}

.goal-title {
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.goal-progress-text {
  font-size: 0.8125rem;
  color: var(--text-muted);
}

.goal-progress-bar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.goal-progress-bar .progress {
  flex: 1;
}

.progress-percent {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--primary-light);
}

/* Recent Exercises */
.exercises-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.exercise-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.exercise-item:hover {
  background: var(--gray-600);
}

.exercise-icon {
  width: 3rem;
  height: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  font-size: 1.5rem;
}

.exercise-info {
  flex: 1;
}

.exercise-name {
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.exercise-date {
  font-size: 0.8125rem;
  color: var(--text-muted);
}

.exercise-stats {
  display: flex;
  gap: 1.5rem;
}

.exercise-stats .stat {
  text-align: center;
}

.exercise-stats .stat-value {
  display: block;
  font-size: 1.125rem;
  font-weight: 600;
}

.exercise-stats .stat-label {
  font-size: 0.75rem;
  color: var(--text-muted);
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
