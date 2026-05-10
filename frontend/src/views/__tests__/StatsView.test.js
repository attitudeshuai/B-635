import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import StatsView from '../StatsView.vue'
import api from '../../services/api'

vi.mock('../../services/api', () => ({
  default: {
    get: vi.fn()
  }
}))

vi.mock('vue-chartjs', () => ({
  Line: {
    name: 'Line',
    template: '<div class="line-chart"></div>',
    props: ['data', 'options']
  },
  Doughnut: {
    name: 'Doughnut',
    template: '<div class="doughnut-chart"></div>',
    props: ['data', 'options']
  }
}))

vi.mock('chart.js', () => ({
  Chart: {
    register: vi.fn()
  },
  CategoryScale: {},
  LinearScale: {},
  PointElement: {},
  LineElement: {},
  ArcElement: {},
  Title: {},
  Tooltip: {},
  Legend: {},
  Filler: {}
}))

function createWrapper() {
  return mount(StatsView, {
    global: {
      stubs: {
        Line: true,
        Doughnut: true
      }
    }
  })
}

describe('StatsView 单元测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('百分比计算函数', () => {
    it('getPercentage - 正确计算百分比', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      const mockDistributionData = [
        { type: '跑步', count: 5 },
        { type: '游泳', count: 3 },
        { type: '骑行', count: 2 }
      ]
      wrapper.vm.distributionData = mockDistributionData

      expect(vm.getPercentage(5)).toBe('50.0')
      expect(vm.getPercentage(3)).toBe('30.0')
      expect(vm.getPercentage(2)).toBe('20.0')
    })

    it('getPercentage - 总数为 0 时返回 0', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      wrapper.vm.distributionData = []

      expect(vm.getPercentage(5)).toBe(0)
    })

    it('getPercentage - 处理小数精度', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      wrapper.vm.distributionData = [
        { type: '跑步', count: 1 },
        { type: '游泳', count: 1 },
        { type: '骑行', count: 1 }
      ]

      expect(vm.getPercentage(1)).toBe('33.3')
    })
  })

  describe('数字格式化函数', () => {
    it('formatNumber - 小于 1000 时直接返回', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.formatNumber(0)).toBe(0)
      expect(vm.formatNumber(500)).toBe(500)
      expect(vm.formatNumber(999)).toBe(999)
    })

    it('formatNumber - 大于等于 1000 时转为 k 单位', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      expect(vm.formatNumber(1000)).toBe('1.0k')
      expect(vm.formatNumber(1500)).toBe('1.5k')
      expect(vm.formatNumber(2500)).toBe('2.5k')
      expect(vm.formatNumber(10000)).toBe('10.0k')
    })
  })

  describe('趋势图表数据转换', () => {
    it('trendChartData - 正确转换趋势数据', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      wrapper.vm.trendData = [
        { date: '2026-05-01', calories: 300 },
        { date: '2026-05-02', calories: 450 },
        { date: '2026-05-03', calories: 0 }
      ]

      const chartData = vm.trendChartData

      expect(chartData.labels).toEqual(['05-01', '05-02', '05-03'])
      expect(chartData.datasets[0].data).toEqual([300, 450, 0])
      expect(chartData.datasets[0].label).toBe('卡路里')
    })

    it('trendChartData - 空数据时返回空数组', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      wrapper.vm.trendData = []

      const chartData = vm.trendChartData

      expect(chartData.labels).toEqual([])
      expect(chartData.datasets[0].data).toEqual([])
    })
  })

  describe('分布图表数据转换', () => {
    it('distributionChartData - 正确转换分布数据', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      wrapper.vm.distributionData = [
        { type: '跑步', count: 5 },
        { type: '游泳', count: 3 },
        { type: '骑行', count: 2 }
      ]

      const chartData = vm.distributionChartData

      expect(chartData.labels).toEqual(['跑步', '游泳', '骑行'])
      expect(chartData.datasets[0].data).toEqual([5, 3, 2])
    })

    it('distributionChartData - 空数据时返回空数组', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      wrapper.vm.distributionData = []

      const chartData = vm.distributionChartData

      expect(chartData.labels).toEqual([])
      expect(chartData.datasets[0].data).toEqual([])
    })

    it('distributionChartData - 正确分配颜色', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      wrapper.vm.distributionData = [
        { type: '跑步', count: 5 },
        { type: '游泳', count: 3 }
      ]

      const chartData = vm.distributionChartData

      expect(chartData.datasets[0].backgroundColor).toHaveLength(2)
      expect(chartData.datasets[0].backgroundColor[0]).toBe('#6366f1')
      expect(chartData.datasets[0].backgroundColor[1]).toBe('#10b981')
    })
  })

  describe('API 数据字段契约验证', () => {
    it('周统计数据必须包含所有必需字段', () => {
      const weeklyStats = {
        count: 5,
        duration: 300,
        calories: 2500,
        startDate: '2026-05-04',
        endDate: '2026-05-10'
      }

      expect(weeklyStats).toHaveProperty('count')
      expect(weeklyStats).toHaveProperty('duration')
      expect(weeklyStats).toHaveProperty('calories')
      expect(weeklyStats).toHaveProperty('startDate')
      expect(weeklyStats).toHaveProperty('endDate')
    })

    it('月统计数据必须包含所有必需字段', () => {
      const monthlyStats = {
        count: 15,
        duration: 900,
        calories: 8000,
        month: 5,
        year: 2026
      }

      expect(monthlyStats).toHaveProperty('count')
      expect(monthlyStats).toHaveProperty('duration')
      expect(monthlyStats).toHaveProperty('calories')
      expect(monthlyStats).toHaveProperty('month')
      expect(monthlyStats).toHaveProperty('year')
    })

    it('趋势数据项必须包含所有必需字段', () => {
      const trendItem = {
        date: '2026-05-10',
        calories: 500
      }

      expect(trendItem).toHaveProperty('date')
      expect(trendItem).toHaveProperty('calories')
    })

    it('分布数据项必须包含所有必需字段', () => {
      const distributionItem = {
        type: '跑步',
        count: 5
      }

      expect(distributionItem).toHaveProperty('type')
      expect(distributionItem).toHaveProperty('count')
    })
  })

  describe('数据获取函数', () => {
    it('fetchWeeklyStats - 正确处理周统计 API 响应', async () => {
      const mockData = {
        count: 5,
        duration: 300,
        calories: 2500,
        startDate: '2026-05-04',
        endDate: '2026-05-10'
      }

      api.get.mockResolvedValue({
        data: { success: true, data: mockData }
      })

      const wrapper = createWrapper()
      await wrapper.vm.fetchWeeklyStats()

      expect(api.get).toHaveBeenCalledWith('/api/stats/weekly')
      expect(wrapper.vm.weeklyStats.count).toBe(5)
      expect(wrapper.vm.weeklyStats.duration).toBe(300)
      expect(wrapper.vm.weeklyStats.calories).toBe(2500)
    })

    it('fetchWeeklyStats - API 失败时不抛出异常', async () => {
      api.get.mockRejectedValue(new Error('Network error'))

      const wrapper = createWrapper()
      const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})

      await expect(wrapper.vm.fetchWeeklyStats()).resolves.not.toThrow()
      consoleError.mockRestore()
    })

    it('fetchMonthlyStats - 正确处理月统计 API 响应', async () => {
      const mockData = {
        count: 15,
        duration: 900,
        calories: 8000,
        month: 5,
        year: 2026
      }

      api.get.mockResolvedValue({
        data: { success: true, data: mockData }
      })

      const wrapper = createWrapper()
      await wrapper.vm.fetchMonthlyStats()

      expect(api.get).toHaveBeenCalledWith('/api/stats/monthly')
      expect(wrapper.vm.monthlyStats.count).toBe(15)
      expect(wrapper.vm.monthlyStats.duration).toBe(900)
      expect(wrapper.vm.monthlyStats.calories).toBe(8000)
    })

    it('fetchTrend - 正确处理趋势 API 响应', async () => {
      const mockData = [
        { date: '2026-05-01', calories: 300 },
        { date: '2026-05-02', calories: 450 }
      ]

      api.get.mockResolvedValue({
        data: { success: true, data: mockData }
      })

      const wrapper = createWrapper()
      await wrapper.vm.fetchTrend()

      expect(api.get).toHaveBeenCalledWith('/api/stats/trend?days=30')
      expect(wrapper.vm.trendData).toHaveLength(2)
      expect(wrapper.vm.trendData[0].date).toBe('2026-05-01')
      expect(wrapper.vm.trendData[0].calories).toBe(300)
    })

    it('fetchDistribution - 正确处理分布 API 响应', async () => {
      const mockData = [
        { type: '跑步', count: 5 },
        { type: '游泳', count: 3 }
      ]

      api.get.mockResolvedValue({
        data: { success: true, data: mockData }
      })

      const wrapper = createWrapper()
      await wrapper.vm.fetchDistribution()

      expect(api.get).toHaveBeenCalledWith('/api/stats/distribution')
      expect(wrapper.vm.distributionData).toHaveLength(2)
      expect(wrapper.vm.distributionData[0].type).toBe('跑步')
      expect(wrapper.vm.distributionData[0].count).toBe(5)
    })
  })

  describe('响应式数据初始化', () => {
    it('weeklyStats 初始值正确', () => {
      const wrapper = createWrapper()

      expect(wrapper.vm.weeklyStats.count).toBe(0)
      expect(wrapper.vm.weeklyStats.duration).toBe(0)
      expect(wrapper.vm.weeklyStats.calories).toBe(0)
    })

    it('monthlyStats 初始值正确', () => {
      const wrapper = createWrapper()

      expect(wrapper.vm.monthlyStats.count).toBe(0)
      expect(wrapper.vm.monthlyStats.duration).toBe(0)
      expect(wrapper.vm.monthlyStats.calories).toBe(0)
    })

    it('trendData 和 distributionData 初始为空数组', () => {
      const wrapper = createWrapper()

      expect(wrapper.vm.trendData).toEqual([])
      expect(wrapper.vm.distributionData).toEqual([])
    })
  })

  describe('totalCount 计算属性', () => {
    it('totalCount - 正确计算总次数', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      wrapper.vm.distributionData = [
        { type: '跑步', count: 5 },
        { type: '游泳', count: 3 },
        { type: '骑行', count: 2 }
      ]

      expect(vm.totalCount).toBe(10)
    })

    it('totalCount - 空数据时返回 0', () => {
      const wrapper = createWrapper()
      const vm = wrapper.vm

      wrapper.vm.distributionData = []

      expect(vm.totalCount).toBe(0)
    })
  })
})
