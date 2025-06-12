import { vi, describe, it, expect, beforeEach } from 'vitest'
vi.mock('../src/axios', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: {} }),
    post: vi.fn().mockResolvedValue({}),
    put: vi.fn().mockResolvedValue({}),
    delete: vi.fn().mockResolvedValue({})
  }
}))
import { mount } from '@vue/test-utils'
import PriceHistoryComponent from '../src/components/PriceHistoryComponent.vue'

// element-plus imports css which Node can't handle during tests
vi.mock('element-plus/theme-chalk/el-date-picker.css', () => ({}), { virtual: true })
vi.mock('element-plus', () => ({ ElDatePicker: { name: 'el-date-picker', template: '<div />' } }))
vi.mock('element-plus/es/components/date-picker/style/css', () => ({}), { virtual: true })
vi.mock('../src/axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

const mountComponent = async () => {
  vi.spyOn(PriceHistoryComponent.methods, 'fetchData').mockResolvedValue()
  vi.spyOn(PriceHistoryComponent.methods, 'fetchPriceHistories').mockResolvedValue()
  const wrapper = mount(PriceHistoryComponent)
  await wrapper.vm.$nextTick()
  return wrapper
}

describe('PriceHistoryComponent', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('computes default date range starting from Jan 1st', async () => {
    const wrapper = await mountComponent()
    const [start, end] = wrapper.vm.getDefaultDateRange()
    expect(start).toMatch(/^\d{4}-01-01T/)
    expect(end).toMatch(/^\d{4}-\d{2}-\d{2}T/)
  })

  it('adds timezone offset to date string', async () => {
    const wrapper = await mountComponent()
    const result = wrapper.vm.addOffsetToDateString('2024-01-01T00:00:00.000')
    expect(result).toMatch(/\d{4}-01-01T00:00:00\.000(?:%2b|-)\d{2}:\d{2}$/)
  })
})
