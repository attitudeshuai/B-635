<template>
  <div class="goals-page">
    <div class="page-header">
      <h2>运动目标</h2>
      <button class="btn btn-primary" @click="openModal()">
        <span>🎯</span> 新建目标
      </button>
    </div>
    
    <div class="goals-grid" v-if="goals.length">
      <div v-for="goal in goals" :key="goal.id" class="goal-card">
        <div class="goal-header">
          <div class="goal-type">
            <span class="goal-icon">{{ getGoalIcon(goal.goalType) }}</span>
            <span class="goal-type-name">{{ getGoalTypeName(goal.goalType) }}</span>
          </div>
          <span :class="['status-badge', getStatusClass(goal.status)]">
            {{ getStatusName(goal.status) }}
          </span>
        </div>
        
        <h3 class="goal-title">{{ goal.title || getGoalTypeName(goal.goalType) }}</h3>
        
        <div class="goal-progress-section">
          <div class="progress-header">
            <span class="progress-text">
              {{ goal.currentValue }} / {{ goal.targetValue }} {{ getGoalUnit(goal.goalType) }}
            </span>
            <span class="progress-percent">{{ goal.progress }}%</span>
          </div>
          <div class="progress-track">
            <div 
              class="progress-fill" 
              :style="{ width: goal.progress + '%', background: getProgressColor(goal.status, goal.progress) }"
            ></div>
          </div>
        </div>
        
        <div class="goal-dates">
          <span class="date-icon">📅</span>
          <span>{{ formatDate(goal.startDate) }} - {{ formatDate(goal.endDate) }}</span>
        </div>
        
        <div class="goal-actions">
          <button class="btn btn-secondary btn-sm" @click="openModal(goal)">编辑</button>
          <button class="btn btn-danger btn-sm" @click="showDeleteConfirm(goal)">删除</button>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-state card">
      <div class="empty-state-icon">🎯</div>
      <h3 class="empty-state-title">暂无运动目标</h3>
      <p>设定目标让运动更有动力</p>
      <button class="btn btn-primary mt-4" @click="openModal()">创建第一个目标</button>
    </div>
    
    <!-- Add/Edit Modal -->
    <Teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">{{ isEditing ? '编辑目标' : '新建目标' }}</h3>
            <button class="modal-close" @click="closeModal">×</button>
          </div>
          <form @submit.prevent="handleSubmit" class="modal-body">
            <div class="form-group">
              <label class="form-label">目标标题</label>
              <input type="text" v-model="form.title" class="form-input" placeholder="如：本周运动5次" />
            </div>
            
            <div class="form-group">
              <label class="form-label">目标类型</label>
              <select v-model="form.goalType" class="form-input" required>
                <option value="CALORIES">🔥 卡路里消耗</option>
                <option value="DURATION">⏱️ 运动时长</option>
                <option value="COUNT">🔢 运动次数</option>
              </select>
            </div>
            
            <div class="form-group">
              <label class="form-label">目标值 ({{ getGoalUnit(form.goalType) }})</label>
              <input type="number" v-model.number="form.targetValue" class="form-input" min="1" required />
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">开始日期</label>
                <input type="date" v-model="form.startDate" class="form-input" required />
              </div>
              <div class="form-group">
                <label class="form-label">结束日期</label>
                <input type="date" v-model="form.endDate" class="form-input" required />
              </div>
            </div>
            
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="loading">
                {{ loading ? '保存中...' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
    
    <!-- Delete Confirm Modal -->
    <ConfirmModal 
      ref="confirmModalRef"
      type="danger"
      title="删除运动目标"
      :message="deleteMessage"
      confirmText="确认删除"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../services/api'
import { useToastStore } from '../stores/toast'
import ConfirmModal from '../components/ConfirmModal.vue'

const toast = useToastStore()

const goals = ref([])
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const loading = ref(false)

const confirmModalRef = ref(null)
const deleteMessage = ref('')
const goalToDelete = ref(null)

const form = reactive({
  title: '',
  goalType: 'CALORIES',
  targetValue: 1000,
  startDate: new Date().toISOString().split('T')[0],
  endDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
})

async function fetchGoals() {
  try {
    const response = await api.get('/api/goals')
    if (response.data.success) {
      goals.value = response.data.data
    }
  } catch (error) {
    toast.error('获取目标失败')
  }
}

function openModal(goal = null) {
  if (goal) {
    isEditing.value = true
    editingId.value = goal.id
    form.title = goal.title || ''
    form.goalType = goal.goalType
    form.targetValue = goal.targetValue
    form.startDate = goal.startDate
    form.endDate = goal.endDate
  } else {
    isEditing.value = false
    editingId.value = null
    form.title = ''
    form.goalType = 'CALORIES'
    form.targetValue = 1000
    form.startDate = new Date().toISOString().split('T')[0]
    form.endDate = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function handleSubmit() {
  loading.value = true
  try {
    const data = { ...form }
    
    if (isEditing.value) {
      await api.put(`/api/goals/${editingId.value}`, data)
      toast.success('更新成功')
    } else {
      await api.post('/api/goals', data)
      toast.success('创建成功')
    }
    
    closeModal()
    fetchGoals()
  } catch (error) {
    toast.error(error.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

async function showDeleteConfirm(goal) {
  goalToDelete.value = goal
  deleteMessage.value = `确定要删除目标「${goal.title || getGoalTypeName(goal.goalType)}」吗？此操作无法撤销。`
  
  const confirmed = await confirmModalRef.value.show()
  if (confirmed) {
    await deleteGoal()
  }
}

async function deleteGoal() {
  try {
    await api.delete(`/api/goals/${goalToDelete.value.id}`)
    toast.success('删除成功')
    fetchGoals()
  } catch (error) {
    toast.error('删除失败')
  }
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
  const units = { CALORIES: '卡路里', DURATION: '分钟', COUNT: '次' }
  return units[type] || ''
}

function getStatusName(status) {
  const names = { ACTIVE: '进行中', COMPLETED: '已完成', FAILED: '未达成' }
  return names[status] || status
}

function getStatusClass(status) {
  const classes = { ACTIVE: 'status-active', COMPLETED: 'status-completed', FAILED: 'status-failed' }
  return classes[status] || ''
}

function getProgressColor(status, progress) {
  if (status === 'COMPLETED') return 'linear-gradient(90deg, #10b981, #34d399)'
  if (status === 'FAILED') return 'linear-gradient(90deg, #ef4444, #f87171)'
  if (progress >= 75) return 'linear-gradient(90deg, #6366f1, #10b981)'
  if (progress >= 50) return 'linear-gradient(90deg, #8b5cf6, #6366f1)'
  return 'linear-gradient(90deg, #f59e0b, #6366f1)'
}

function formatDate(date) {
  return new Date(date).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

onMounted(() => {
  fetchGoals()
})
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.page-header h2 {
  font-size: 1.5rem;
}

.goals-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 1.25rem;
}

.goal-card {
  padding: 1.5rem;
  background: linear-gradient(145deg, #243049, #1a2438);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 1rem;
  transition: all 0.25s ease;
}

.goal-card:hover {
  transform: translateY(-3px);
  border-color: rgba(139, 92, 246, 0.4);
  box-shadow: 0 12px 24px -8px rgba(0, 0, 0, 0.4);
}

.goal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.goal-type {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.goal-icon {
  font-size: 1.75rem;
}

.goal-type-name {
  color: #94a3b8;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-badge {
  padding: 0.375rem 0.875rem;
  border-radius: 2rem;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.status-active {
  background: rgba(99, 102, 241, 0.2);
  color: #818cf8;
  border: 1px solid rgba(99, 102, 241, 0.3);
}

.status-completed {
  background: rgba(16, 185, 129, 0.2);
  color: #34d399;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.status-failed {
  background: rgba(239, 68, 68, 0.2);
  color: #f87171;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.goal-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1.25rem;
  color: #f8fafc;
}

.goal-progress-section {
  margin-bottom: 1.25rem;
  padding: 1rem;
  background: rgba(15, 23, 42, 0.5);
  border-radius: 0.75rem;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.progress-text {
  font-size: 0.9375rem;
  color: #cbd5e1;
  font-weight: 500;
}

.progress-percent {
  font-weight: 700;
  font-size: 1.125rem;
  color: #a5b4fc;
}

.progress-track {
  height: 0.625rem;
  background: rgba(71, 85, 105, 0.4);
  border-radius: 1rem;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 1rem;
  transition: width 0.5s ease;
}

.goal-dates {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #94a3b8;
  margin-bottom: 1rem;
  padding: 0.5rem 0.75rem;
  background: rgba(148, 163, 184, 0.08);
  border-radius: 0.5rem;
  width: fit-content;
}

.date-icon {
  font-size: 1rem;
}

.goal-actions {
  display: flex;
  gap: 0.75rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.modal-footer {
  padding-top: 1rem;
}

@media (max-width: 640px) {
  .goals-grid {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
