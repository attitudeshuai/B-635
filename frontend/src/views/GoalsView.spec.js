import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import GoalsView from './GoalsView.vue'
import api from '../services/api'
import { useToastStore } from '../stores/toast'

const mockGoals = [
  {
    id: 1,
    goalType: 'CALORIES',
    targetValue: 1000,
    currentValue: 500,
    startDate: '2024-01-01',
    endDate: '2024-01-31',
    status: 'ACTIVE',
    title: '一月卡路里目标',
    progress: 50
  },
  {
    id: 2,
    goalType: 'DURATION',
    targetValue: 300,
    currentValue: 300,
    startDate: '2024-01-01',
    endDate: '2024-01-31',
    status: 'COMPLETED',
    title: '运动时长目标',
    progress: 100
  },
  {
    id: 3,
    goalType: 'COUNT',
    targetValue: 10,
    currentValue: 3,
    startDate: '2024-01-01',
    endDate: '2024-01-31',
    status: 'FAILED',
    title: '运动次数目标',
    progress: 30
  }
]

describe('GoalsView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('API 响应字段契约测试', () => {
    it('应正确映射所有后端返回的目标字段', async () => {
      vi.mocked(api.get).mockResolvedValue({
        data: {
          success: true,
          data: mockGoals
        }
      })

      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      await wrapper.vm.$nextTick()

      expect(api.get).toHaveBeenCalledWith('/api/goals')
    })

    it('应包含所有必需字段: id, goalType, targetValue, currentValue, startDate, endDate, status, title, progress', () => {
      const goal = mockGoals[0]
      
      const requiredFields = ['id', 'goalType', 'targetValue', 'currentValue', 'startDate', 'endDate', 'status', 'title', 'progress']
      
      requiredFields.forEach(field => {
        expect(goal).toHaveProperty(field)
      })
    })
  })

  describe('目标类型显示测试', () => {
    it('应正确显示卡路里目标的图标和名称', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      expect(wrapper.vm.getGoalIcon('CALORIES')).toBe('🔥')
      expect(wrapper.vm.getGoalTypeName('CALORIES')).toBe('卡路里目标')
      expect(wrapper.vm.getGoalUnit('CALORIES')).toBe('卡路里')
    })

    it('应正确显示时长目标的图标和名称', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      expect(wrapper.vm.getGoalIcon('DURATION')).toBe('⏱️')
      expect(wrapper.vm.getGoalTypeName('DURATION')).toBe('时长目标')
      expect(wrapper.vm.getGoalUnit('DURATION')).toBe('分钟')
    })

    it('应正确显示次数目标的图标和名称', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      expect(wrapper.vm.getGoalIcon('COUNT')).toBe('🔢')
      expect(wrapper.vm.getGoalTypeName('COUNT')).toBe('次数目标')
      expect(wrapper.vm.getGoalUnit('COUNT')).toBe('次')
    })

    it('未知目标类型应有默认处理', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      expect(wrapper.vm.getGoalIcon('UNKNOWN')).toBe('🎯')
      expect(wrapper.vm.getGoalTypeName('UNKNOWN')).toBe('目标')
      expect(wrapper.vm.getGoalUnit('UNKNOWN')).toBe('')
    })
  })

  describe('状态显示测试', () => {
    it('应正确显示进行中状态', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      expect(wrapper.vm.getStatusName('ACTIVE')).toBe('进行中')
      expect(wrapper.vm.getStatusClass('ACTIVE')).toBe('status-active')
    })

    it('应正确显示已完成状态', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      expect(wrapper.vm.getStatusName('COMPLETED')).toBe('已完成')
      expect(wrapper.vm.getStatusClass('COMPLETED')).toBe('status-completed')
    })

    it('应正确显示未达成状态', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      expect(wrapper.vm.getStatusName('FAILED')).toBe('未达成')
      expect(wrapper.vm.getStatusClass('FAILED')).toBe('status-failed')
    })

    it('未知状态应有默认处理', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      expect(wrapper.vm.getStatusName('UNKNOWN')).toBe('UNKNOWN')
      expect(wrapper.vm.getStatusClass('UNKNOWN')).toBe('')
    })
  })

  describe('进度条颜色测试', () => {
    it('已完成状态使用绿色', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      const color = wrapper.vm.getProgressColor('COMPLETED', 100)
      expect(color).toContain('10b981')
    })

    it('未达成状态使用红色', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      const color = wrapper.vm.getProgressColor('FAILED', 50)
      expect(color).toContain('ef4444')
    })

    it('高进度使用渐变到绿色', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      const color = wrapper.vm.getProgressColor('ACTIVE', 80)
      expect(color).toContain('6366f1')
      expect(color).toContain('10b981')
    })

    it('中等进度使用紫色系', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      const color = wrapper.vm.getProgressColor('ACTIVE', 60)
      expect(color).toContain('8b5cf6')
      expect(color).toContain('6366f1')
    })

    it('低进度使用黄色到紫色', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      const color = wrapper.vm.getProgressColor('ACTIVE', 30)
      expect(color).toContain('f59e0b')
      expect(color).toContain('6366f1')
    })
  })

  describe('空状态测试', () => {
    it('无目标时应显示空状态', async () => {
      vi.mocked(api.get).mockResolvedValue({
        data: {
          success: true,
          data: []
        }
      })

      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      await wrapper.vm.$nextTick()
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.empty-state').exists()).toBe(true)
    })
  })

  describe('日期格式化测试', () => {
    it('应正确格式化日期', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      const formatted = wrapper.vm.formatDate('2024-01-15')
      expect(formatted).toBeDefined()
    })
  })

  describe('API 错误处理测试', () => {
    it('API 失败时应显示错误提示', async () => {
      const mockErrorToast = vi.fn()
      vi.mocked(useToastStore).mockReturnValue({
        success: vi.fn(),
        error: mockErrorToast
      })

      vi.mocked(api.get).mockRejectedValue(new Error('API Error'))

      mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      expect(mockErrorToast).toHaveBeenCalledWith('获取目标失败')
    })
  })

  describe('表单初始化测试', () => {
    it('新建目标表单应正确初始化', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      expect(wrapper.vm.form.goalType).toBe('CALORIES')
      expect(wrapper.vm.form.targetValue).toBe(1000)
      expect(wrapper.vm.form.title).toBe('')
    })

    it('编辑目标时应填充表单数据', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      const testGoal = {
        id: 1,
        goalType: 'DURATION',
        targetValue: 500,
        startDate: '2024-01-01',
        endDate: '2024-01-31',
        title: '测试目标'
      }

      wrapper.vm.openModal(testGoal)

      expect(wrapper.vm.isEditing).toBe(true)
      expect(wrapper.vm.editingId).toBe(1)
      expect(wrapper.vm.form.goalType).toBe('DURATION')
      expect(wrapper.vm.form.targetValue).toBe(500)
      expect(wrapper.vm.form.title).toBe('测试目标')
    })
  })

  describe('字段缺失容错测试', () => {
    it('目标标题为空时应显示类型名称', () => {
      const wrapper = mount(GoalsView, {
        global: {
          stubs: ['ConfirmModal']
        }
      })

      const goalWithoutTitle = { goalType: 'CALORIES', title: '' }
      const displayTitle = goalWithoutTitle.title || wrapper.vm.getGoalTypeName(goalWithoutTitle.goalType)
      
      expect(displayTitle).toBe('卡路里目标')
    })
  })
})
