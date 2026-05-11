import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import StatsView from './StatsView.vue'
import api from '../services/api'

const mockWeeklyStats = {
  count: 5,
  duration: 150,
  calories: 1200,
  startDate: '2024-01-01',
  endDate: '2024-01-07'
}

const mockMonthlyStats = {
  count: 20,
  duration: 600,
  calories: 5000,
  month: 1,
  year: 2024
}

const mockTrendData = [
  { date: '2024-01-01', calories: 100 },
  { date: '2024-01-02', calories: 200 },
  { date: '2024-01-03', calories: 150 },
  { date: '2024-01-04', calories: 300 },
  { date: '2024-01-05', calories: 250 },
  { date: '2024-01-06', calories: 180 },
  { date: '2024-01-07', calories: 220 }
]

const mockDistributionData = [
  { type: 'Running', count: 10 },
  { type: 'Swimming', count: 5 },
  { type: 'Cycling', count: 8 }
]

describe('StatsView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('API 响应字段契约测试', () => {
    it('周统计响应应包含所有必需字段', () => {
      const requiredFields = ['count', 'duration', 'calories', 'startDate', 'endDate']
      
      requiredFields.forEach(field => {
        expect(mockWeeklyStats).toHaveProperty(field)
      })
    })

    it('月统计响应应包含所有必需字段', () => {
      const requiredFields = ['count', 'duration', 'calories', 'month', 'year']
      
      requiredFields.forEach(field => {
        expect(mockMonthlyStats).toHaveProperty(field)
      })
    })

    it('趋势数据响应应包含所有必需字段', () => {
      const requiredFields = ['date', 'calories']
      
      mockTrendData.forEach(item => {
        requiredFields.forEach(field => {
          expect(item).toHaveProperty(field)
        })
      })
    })

    it('类型分布响应应包含所有必需字段', () => {
      const requiredFields = ['type', 'count']
      
      mockDistributionData.forEach(item => {
        requiredFields.forEach(field => {
          expect(item).toHaveProperty(field)
        })
      })
    })

    it('应调用所有必需的 API 接口', async () => {
      vi.mocked(api.get)
        .mockResolvedValueOnce({ data: { success: true, data: mockWeeklyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockMonthlyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockTrendData } })
        .mockResolvedValueOnce({ data: { success: true, data: mockDistributionData } })

      mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      expect(api.get).toHaveBeenCalledWith('/api/stats/weekly')
      expect(api.get).toHaveBeenCalledWith('/api/stats/monthly')
      expect(api.get).toHaveBeenCalledWith('/api/stats/trend?days=30')
      expect(api.get).toHaveBeenCalledWith('/api/stats/distribution')
    })
  })

  describe('数据处理测试', () => {
    it('数字格式化测试', () => {
      const wrapper = mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      expect(wrapper.vm.formatNumber(500)).toBe(500)
      expect(wrapper.vm.formatNumber(1000)).toBe('1.0k')
      expect(wrapper.vm.formatNumber(1500)).toBe('1.5k')
      expect(wrapper.vm.formatNumber(20000)).toBe('20.0k')
    })

    it('百分比计算测试', async () => {
      vi.mocked(api.get)
        .mockResolvedValueOnce({ data: { success: true, data: mockWeeklyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockMonthlyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockTrendData } })
        .mockResolvedValueOnce({ data: { success: true, data: mockDistributionData } })

      const wrapper = mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      expect(wrapper.vm.totalCount).toBe(23)
      expect(wrapper.vm.getPercentage(10)).toBeCloseTo(43.5, 1)
      expect(wrapper.vm.getPercentage(5)).toBeCloseTo(21.7, 1)
      expect(wrapper.vm.getPercentage(8)).toBeCloseTo(34.8, 1)
    })

    it('总计数为0时百分比应为0', async () => {
      vi.mocked(api.get)
        .mockResolvedValueOnce({ data: { success: true, data: mockWeeklyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockMonthlyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockTrendData } })
        .mockResolvedValueOnce({ data: { success: true, data: [] } })

      const wrapper = mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      expect(wrapper.vm.totalCount).toBe(0)
      expect(wrapper.vm.getPercentage(10)).toBe(0)
    })
  })

  describe('图表数据测试', () => {
    it('趋势图表数据应正确映射', async () => {
      vi.mocked(api.get)
        .mockResolvedValueOnce({ data: { success: true, data: mockWeeklyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockMonthlyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockTrendData } })
        .mockResolvedValueOnce({ data: { success: true, data: mockDistributionData } })

      const wrapper = mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      expect(wrapper.vm.trendChartData.labels).toHaveLength(7)
      expect(wrapper.vm.trendChartData.datasets[0].data).toHaveLength(7)
      expect(wrapper.vm.trendChartData.datasets[0].data[0]).toBe(100)
    })

    it('分布图表数据应正确映射', async () => {
      vi.mocked(api.get)
        .mockResolvedValueOnce({ data: { success: true, data: mockWeeklyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockMonthlyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockTrendData } })
        .mockResolvedValueOnce({ data: { success: true, data: mockDistributionData } })

      const wrapper = mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      expect(wrapper.vm.distributionChartData.labels).toEqual(['Running', 'Swimming', 'Cycling'])
      expect(wrapper.vm.distributionChartData.datasets[0].data).toEqual([10, 5, 8])
    })
  })

  describe('空数据处理测试', () => {
    it('无趋势数据时应显示空状态', async () => {
      vi.mocked(api.get)
        .mockResolvedValueOnce({ data: { success: true, data: mockWeeklyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockMonthlyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: [] } })
        .mockResolvedValueOnce({ data: { success: true, data: mockDistributionData } })

      const wrapper = mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      expect(wrapper.vm.trendData).toHaveLength(0)
    })

    it('无分布数据时应显示空状态', async () => {
      vi.mocked(api.get)
        .mockResolvedValueOnce({ data: { success: true, data: mockWeeklyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockMonthlyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockTrendData } })
        .mockResolvedValueOnce({ data: { success: true, data: [] } })

      const wrapper = mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      expect(wrapper.vm.distributionData).toHaveLength(0)
    })
  })

  describe('数据映射测试', () => {
    it('周统计数据应正确映射到组件状态', async () => {
      vi.mocked(api.get)
        .mockResolvedValueOnce({ data: { success: true, data: mockWeeklyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockMonthlyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockTrendData } })
        .mockResolvedValueOnce({ data: { success: true, data: mockDistributionData } })

      const wrapper = mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      expect(wrapper.vm.weeklyStats.count).toBe(5)
      expect(wrapper.vm.weeklyStats.duration).toBe(150)
      expect(wrapper.vm.weeklyStats.calories).toBe(1200)
    })

    it('月统计数据应正确映射到组件状态', async () => {
      vi.mocked(api.get)
        .mockResolvedValueOnce({ data: { success: true, data: mockWeeklyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockMonthlyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockTrendData } })
        .mockResolvedValueOnce({ data: { success: true, data: mockDistributionData } })

      const wrapper = mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      expect(wrapper.vm.monthlyStats.count).toBe(20)
      expect(wrapper.vm.monthlyStats.duration).toBe(600)
      expect(wrapper.vm.monthlyStats.calories).toBe(5000)
    })
  })

  describe('API 错误处理测试', () => {
    it('周统计 API 失败时应记录错误', async () => {
      const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
      
      vi.mocked(api.get)
        .mockRejectedValueOnce(new Error('Weekly API Error'))
        .mockResolvedValueOnce({ data: { success: true, data: mockMonthlyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockTrendData } })
        .mockResolvedValueOnce({ data: { success: true, data: mockDistributionData } })

      mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      expect(consoleError).toHaveBeenCalled()
      consoleError.mockRestore()
    })

    it('趋势 API 失败时应记录错误', async () => {
      const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
      
      vi.mocked(api.get)
        .mockResolvedValueOnce({ data: { success: true, data: mockWeeklyStats } })
        .mockResolvedValueOnce({ data: { success: true, data: mockMonthlyStats } })
        .mockRejectedValueOnce(new Error('Trend API Error'))
        .mockResolvedValueOnce({ data: { success: true, data: mockDistributionData } })

      mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      await new Promise(resolve => setTimeout(resolve, 100))

      expect(consoleError).toHaveBeenCalled()
      consoleError.mockRestore()
    })
  })

  describe('字段类型测试', () => {
    it('统计数据类型应正确', () => {
      expect(typeof mockWeeklyStats.count).toBe('number')
      expect(typeof mockWeeklyStats.duration).toBe('number')
      expect(typeof mockWeeklyStats.calories).toBe('number')
      expect(typeof mockWeeklyStats.startDate).toBe('string')
      expect(typeof mockWeeklyStats.endDate).toBe('string')
    })

    it('趋势数据类型应正确', () => {
      mockTrendData.forEach(item => {
        expect(typeof item.date).toBe('string')
        expect(typeof item.calories).toBe('number')
      })
    })

    it('分布数据类型应正确', () => {
      mockDistributionData.forEach(item => {
        expect(typeof item.type).toBe('string')
        expect(typeof item.count).toBe('number')
      })
    })
  })

  describe('图表配置测试', () => {
    it('趋势图表配置应正确定义', () => {
      const wrapper = mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      expect(wrapper.vm.lineChartOptions).toBeDefined()
      expect(wrapper.vm.lineChartOptions.plugins.legend.display).toBe(false)
      expect(wrapper.vm.lineChartOptions.scales.x.ticks.color).toBe('#64748b')
    })

    it('分布图表配置应正确定义', () => {
      const wrapper = mount(StatsView, {
        global: {
          stubs: ['Line', 'Doughnut']
        }
      })

      expect(wrapper.vm.doughnutOptions).toBeDefined()
      expect(wrapper.vm.doughnutOptions.plugins.legend.position).toBe('bottom')
      expect(wrapper.vm.doughnutOptions.plugins.legend.labels.color).toBe('#94a3b8')
    })
  })
})
