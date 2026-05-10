<template>
  <div class="exercises-page">
    <div class="page-header">
      <h2>运动记录</h2>
      <button class="btn btn-primary" @click="openModal()">
        <span>➕</span> 添加记录
      </button>
    </div>
    
    <div class="exercises-list" v-if="exercises.length">
      <div v-for="exercise in exercises" :key="exercise.id" class="exercise-card">
        <div class="exercise-header">
          <div class="exercise-type" :style="{ background: getTypeColor(exercise.typeColor) }">
            <span class="type-icon">{{ exercise.typeIcon }}</span>
            <span class="type-name">{{ exercise.typeName }}</span>
          </div>
          <div class="exercise-date">{{ formatDate(exercise.exerciseDate) }}</div>
        </div>
        
        <div class="exercise-body">
          <div class="exercise-stats">
            <div class="stat-item">
              <span class="stat-icon">⏱️</span>
              <span class="stat-value">{{ exercise.durationMinutes }}</span>
              <span class="stat-label">分钟</span>
            </div>
            <div class="stat-item">
              <span class="stat-icon">🔥</span>
              <span class="stat-value">{{ exercise.caloriesBurned }}</span>
              <span class="stat-label">卡路里</span>
            </div>
            <div v-if="exercise.distanceKm" class="stat-item">
              <span class="stat-icon">📍</span>
              <span class="stat-value">{{ exercise.distanceKm }}</span>
              <span class="stat-label">公里</span>
            </div>
          </div>
          <p v-if="exercise.notes" class="exercise-notes">{{ exercise.notes }}</p>
        </div>
        
        <div class="exercise-actions">
          <button class="btn btn-secondary btn-sm" @click="openModal(exercise)">编辑</button>
          <button class="btn btn-danger btn-sm" @click="showDeleteConfirm(exercise)">删除</button>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-state card">
      <div class="empty-state-icon">🏋️</div>
      <h3 class="empty-state-title">暂无运动记录</h3>
      <p>点击"添加记录"开始记录您的运动</p>
      <button class="btn btn-primary mt-4" @click="openModal()">添加第一条记录</button>
    </div>
    
    <!-- Pagination -->
    <div v-if="totalPages > 1" class="pagination">
      <button class="btn btn-secondary btn-sm" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">上一页</button>
      <span class="page-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
      <button class="btn btn-secondary btn-sm" :disabled="currentPage >= totalPages - 1" @click="changePage(currentPage + 1)">下一页</button>
    </div>
    
    <!-- Add/Edit Modal -->
    <Teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal">
          <div class="modal-header">
            <h3 class="modal-title">{{ isEditing ? '编辑记录' : '添加记录' }}</h3>
            <button class="modal-close" @click="closeModal">×</button>
          </div>
          <form @submit.prevent="handleSubmit" class="modal-body">
            <div class="form-group">
              <label class="form-label">运动类型</label>
              <select v-model="form.typeId" class="form-input" required>
                <option value="">请选择运动类型</option>
                <option v-for="type in exerciseTypes" :key="type.id" :value="type.id">
                  {{ type.icon }} {{ type.name }}
                </option>
              </select>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">运动时长（分钟）</label>
                <input type="number" v-model.number="form.durationMinutes" class="form-input" min="1" required />
              </div>
              <div class="form-group">
                <label class="form-label">距离（公里，可选）</label>
                <input type="number" v-model.number="form.distanceKm" class="form-input" min="0" step="0.1" />
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">运动日期</label>
                <input type="date" v-model="form.exerciseDate" class="form-input" required />
              </div>
              <div class="form-group">
                <label class="form-label">开始时间（可选）</label>
                <input type="time" v-model="form.startTime" class="form-input" />
              </div>
            </div>
            
            <div class="form-group">
              <label class="form-label">备注（可选）</label>
              <textarea v-model="form.notes" class="form-input" rows="2" placeholder="记录运动感受..."></textarea>
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
      title="删除运动记录"
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

const exercises = ref([])
const exerciseTypes = ref([])
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const loading = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)

const confirmModalRef = ref(null)
const deleteMessage = ref('')
const exerciseToDelete = ref(null)

const form = reactive({
  typeId: '',
  durationMinutes: 30,
  distanceKm: null,
  exerciseDate: new Date().toISOString().split('T')[0],
  startTime: '',
  notes: ''
})

async function fetchExercises() {
  try {
    const response = await api.get(`/api/exercises?page=${currentPage.value}&size=10`)
    if (response.data.success) {
      exercises.value = response.data.data.content || []
      totalPages.value = response.data.data.totalPages || 0
    }
  } catch (error) {
    toast.error('获取运动记录失败')
  }
}

async function fetchExerciseTypes() {
  try {
    const response = await api.get('/api/exercise-types')
    if (response.data.success) {
      exerciseTypes.value = response.data.data
    }
  } catch (error) {
    console.error('获取运动类型失败:', error)
  }
}

function openModal(exercise = null) {
  if (exercise) {
    isEditing.value = true
    editingId.value = exercise.id
    form.typeId = exercise.typeId
    form.durationMinutes = exercise.durationMinutes
    form.distanceKm = exercise.distanceKm
    form.exerciseDate = exercise.exerciseDate
    form.startTime = exercise.startTime || ''
    form.notes = exercise.notes || ''
  } else {
    isEditing.value = false
    editingId.value = null
    form.typeId = ''
    form.durationMinutes = 30
    form.distanceKm = null
    form.exerciseDate = new Date().toISOString().split('T')[0]
    form.startTime = ''
    form.notes = ''
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function handleSubmit() {
  loading.value = true
  try {
    const data = {
      typeId: form.typeId,
      durationMinutes: form.durationMinutes,
      distanceKm: form.distanceKm || null,
      exerciseDate: form.exerciseDate,
      startTime: form.startTime || null,
      notes: form.notes || null
    }
    
    if (isEditing.value) {
      await api.put(`/api/exercises/${editingId.value}`, data)
      toast.success('更新成功')
    } else {
      await api.post('/api/exercises', data)
      toast.success('添加成功')
    }
    
    closeModal()
    fetchExercises()
  } catch (error) {
    toast.error(error.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

async function showDeleteConfirm(exercise) {
  exerciseToDelete.value = exercise
  deleteMessage.value = `确定要删除这条「${exercise.typeName}」运动记录吗？此操作无法撤销。`
  
  const confirmed = await confirmModalRef.value.show()
  if (confirmed) {
    await deleteExercise()
  }
}

async function deleteExercise() {
  try {
    await api.delete(`/api/exercises/${exerciseToDelete.value.id}`)
    toast.success('删除成功')
    fetchExercises()
  } catch (error) {
    toast.error('删除失败')
  }
}

function changePage(page) {
  currentPage.value = page
  fetchExercises()
}

function formatDate(date) {
  return new Date(date).toLocaleDateString('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' })
}

function getTypeColor(color) {
  return color ? `${color}40` : 'rgba(99, 102, 241, 0.25)'
}

onMounted(() => {
  fetchExercises()
  fetchExerciseTypes()
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

.exercises-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 1.25rem;
}

.exercise-card {
  padding: 1.5rem;
  background: linear-gradient(145deg, #243049, #1a2438);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 1rem;
  transition: all 0.25s ease;
}

.exercise-card:hover {
  transform: translateY(-3px);
  border-color: rgba(99, 102, 241, 0.4);
  box-shadow: 0 12px 24px -8px rgba(0, 0, 0, 0.4);
}

.exercise-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.exercise-type {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 2rem;
  font-weight: 500;
}

.type-icon {
  font-size: 1.25rem;
}

.type-name {
  font-weight: 600;
  color: #f8fafc;
}

.exercise-date {
  font-size: 0.875rem;
  color: #94a3b8;
  background: rgba(148, 163, 184, 0.1);
  padding: 0.375rem 0.75rem;
  border-radius: 1rem;
}

.exercise-stats {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 1rem;
  padding: 1rem;
  background: rgba(15, 23, 42, 0.5);
  border-radius: 0.75rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.stat-icon {
  font-size: 1rem;
}

.stat-value {
  font-weight: 700;
  font-size: 1.25rem;
  color: #f8fafc;
}

.stat-label {
  font-size: 0.8125rem;
  color: #64748b;
}

.exercise-notes {
  font-size: 0.875rem;
  color: #94a3b8;
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: rgba(148, 163, 184, 0.08);
  border-radius: 0.5rem;
  border-left: 3px solid #6366f1;
}

.exercise-actions {
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

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-top: 2rem;
}

.page-info {
  color: #94a3b8;
}

.modal-footer {
  padding-top: 1rem;
  border-top: none;
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .exercises-list {
    grid-template-columns: 1fr;
  }
}
</style>
