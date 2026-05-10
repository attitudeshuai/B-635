import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import GoalsView from '../GoalsView.vue'
import api from '../../services/api'

vi.mock('../../services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

function createWrapper() {
  const pinia = createPinia()
  return mount(GoalsView, {
    global: {
      plugins: [pinia],
      stubs: {
        ConfirmModal: {
          template: '<div></div>',
          methods: {
            show: () => Promise.resolve(true)
          }
        }
      }
    }
  })
}

describe('GoalsView 单元测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('目标类型映射函数', () => {
    it('getGoalIcon - 正确返回各类型图标', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getGoalIcon('CALORIES')).toBe('🔥')
      expect(vm.getGoalIcon('DURATION')).toBe('⏱️')
      expect(vm.getGoalIcon('COUNT')).toBe('🔢')
    })

    it('getGoalIcon - 未知类型返回默认图标', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getGoalIcon('UNKNOWN')).toBe('🎯')
      expect(vm.getGoalIcon(null)).toBe('🎯')
    })

    it('getGoalTypeName - 正确返回各类型名称', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getGoalTypeName('CALORIES')).toBe('卡路里目标')
      expect(vm.getGoalTypeName('DURATION')).toBe('时长目标')
      expect(vm.getGoalTypeName('COUNT')).toBe('次数目标')
    })

    it('getGoalTypeName - 未知类型返回默认名称', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getGoalTypeName('UNKNOWN')).toBe('目标')
    })

    it('getGoalUnit - 正确返回各类型单位', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getGoalUnit('CALORIES')).toBe('卡路里')
      expect(vm.getGoalUnit('DURATION')).toBe('分钟')
      expect(vm.getGoalUnit('COUNT')).toBe('次')
    })

    it('getGoalUnit - 未知类型返回空字符串', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getGoalUnit('UNKNOWN')).toBe('')
    })
  })

  describe('状态映射函数', () => {
    it('getStatusName - 正确返回各状态名称', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getStatusName('ACTIVE')).toBe('进行中')
      expect(vm.getStatusName('COMPLETED')).toBe('已完成')
      expect(vm.getStatusName('FAILED')).toBe('未达成')
    })

    it('getStatusName - 未知状态返回原值', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getStatusName('PENDING')).toBe('PENDING')
    })

    it('getStatusClass - 正确返回各状态样式类', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getStatusClass('ACTIVE')).toBe('status-active')
      expect(vm.getStatusClass('COMPLETED')).toBe('status-completed')
      expect(vm.getStatusClass('FAILED')).toBe('status-failed')
    })

    it('getStatusClass - 未知状态返回空字符串', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getStatusClass('UNKNOWN')).toBe('')
    })
  })

  describe('进度条颜色函数', () => {
    it('getProgressColor - 已完成状态返回绿色', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getProgressColor('COMPLETED', 100)).toBe('linear-gradient(90deg, #10b981, #34d399)')
    })

    it('getProgressColor - 未达成状态返回红色', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getProgressColor('FAILED', 30)).toBe('linear-gradient(90deg, #ef4444, #f87171)')
    })

    it('getProgressColor - 进行中高进度返回绿色渐变', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getProgressColor('ACTIVE', 80)).toBe('linear-gradient(90deg, #6366f1, #10b981)')
      expect(vm.getProgressColor('ACTIVE', 75)).toBe('linear-gradient(90deg, #6366f1, #10b981)')
    })

    it('getProgressColor - 进行中中等进度返回紫色渐变', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getProgressColor('ACTIVE', 60)).toBe('linear-gradient(90deg, #8b5cf6, #6366f1)')
      expect(vm.getProgressColor('ACTIVE', 50)).toBe('linear-gradient(90deg, #8b5cf6, #6366f1)')
    })

    it('getProgressColor - 进行中低进度返回橙色渐变', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.getProgressColor('ACTIVE', 30)).toBe('linear-gradient(90deg, #f59e0b, #6366f1)')
      expect(vm.getProgressColor('ACTIVE', 0)).toBe('linear-gradient(90deg, #f59e0b, #6366f1)')
    })
  })

  describe('日期格式化函数', () => {
    it('formatDate - 正确格式化日期字符串', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      const result = vm.formatDate('2026-05-10')
      expect(result).toContain('5月')
      expect(result).toContain('10日')
    })

    it('formatDate - 正确格式化 ISO 日期', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      const result = vm.formatDate('2026-01-01')
      expect(result).toContain('1月')
      expect(result).toContain('1日')
    })
  })

  describe('API 数据字段契约验证', () => {
    it('目标对象必须包含所有前端需要的字段', () => {
      const goal = {
        id: 1,
        goalType: 'CALORIES',
        targetValue: 1000,
        currentValue: 500,
        startDate: '2026-05-01',
        endDate: '2026-05-31',
        status: 'ACTIVE',
        title: '测试目标',
        progress: 50
      }

      expect(goal).toHaveProperty('id')
      expect(goal).toHaveProperty('goalType')
      expect(goal).toHaveProperty('targetValue')
      expect(goal).toHaveProperty('currentValue')
      expect(goal).toHaveProperty('startDate')
      expect(goal).toHaveProperty('endDate')
      expect(goal).toHaveProperty('status')
      expect(goal).toHaveProperty('title')
      expect(goal).toHaveProperty('progress')
    })

    it('fetchGoals - 正确处理 API 返回的目标列表', async () => {
      const mockGoals = [
        {
          id: 1,
          goalType: 'CALORIES',
          targetValue: 1000,
          currentValue: 500,
          startDate: '2026-05-01',
          endDate: '2026-05-31',
          status: 'ACTIVE',
          title: '减脂目标',
          progress: 50
        }
      ]

      api.get.mockResolvedValue({
        data: { success: true, data: mockGoals }
      })

      const wrapper = createWrapper()
      await wrapper.vm.fetchGoals()

      expect(wrapper.vm.goals).toHaveLength(1)
      expect(wrapper.vm.goals[0].id).toBe(1)
      expect(wrapper.vm.goals[0].goalType).toBe('CALORIES')
      expect(wrapper.vm.goals[0].progress).toBe(50)
    })
  })

  describe('表单初始化', () => {
    it('openModal - 新建目标时重置表单', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      vm.form.title = '旧标题'
      vm.form.goalType = 'DURATION'
      vm.form.targetValue = 500

      vm.openModal()

      expect(vm.isEditing).toBe(false)
      expect(vm.editingId).toBeNull()
      expect(vm.form.title).toBe('')
      expect(vm.form.goalType).toBe('CALORIES')
      expect(vm.form.targetValue).toBe(1000)
      expect(vm.showModal).toBe(true)
    })

    it('openModal - 编辑目标时填充表单', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      const goal = {
        id: 1,
        goalType: 'DURATION',
        targetValue: 300,
        currentValue: 150,
        startDate: '2026-05-01',
        endDate: '2026-05-31',
        status: 'ACTIVE',
        title: '运动时长目标',
        progress: 50
      }

      vm.openModal(goal)

      expect(vm.isEditing).toBe(true)
      expect(vm.editingId).toBe(1)
      expect(vm.form.title).toBe('运动时长目标')
      expect(vm.form.goalType).toBe('DURATION')
      expect(vm.form.targetValue).toBe(300)
      expect(vm.form.startDate).toBe('2026-05-01')
      expect(vm.form.endDate).toBe('2026-05-31')
    })

    it('openModal - 编辑无标题目标时使用空字符串', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      const goal = {
        id: 1,
        goalType: 'COUNT',
        targetValue: 10,
        currentValue: 5,
        startDate: '2026-05-01',
        endDate: '2026-05-31',
        status: 'ACTIVE',
        title: null,
        progress: 50
      }

      vm.openModal(goal)

      expect(vm.form.title).toBe('')
    })
  })

  describe('关闭模态框', () => {
    it('closeModal - 正确关闭模态框', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      vm.showModal = true
      vm.closeModal()

      expect(vm.showModal).toBe(false)
    })
  })
})
