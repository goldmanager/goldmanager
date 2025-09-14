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

  it('loads default visibility when none saved', async () => {
    const wrapper = await mountComponent()
    const vis = wrapper.vm.loadVisibility('metal-1')
    expect(vis).toEqual({ totalPrice: true, metalPrice: true })
  })

  it('persists and loads per-metal visibility in localStorage', async () => {
    const wrapper = await mountComponent()
    const metalId = '123'
    // Simulate selecting a metal
    wrapper.vm.currentMetal = metalId
    // Save a visibility preference
    const saved = { totalPrice: false, metalPrice: true }
    wrapper.vm.saveVisibility(metalId, saved)
    // Load it back
    const loaded = wrapper.vm.loadVisibility(metalId)
    expect(loaded).toEqual(saved)
  })

  it('handles legend visibility change and saves preference', async () => {
    const wrapper = await mountComponent()
    const metalId = 'm-42'
    wrapper.vm.currentMetal = metalId
    const newVis = { totalPrice: true, metalPrice: false }
    wrapper.vm.onLegendVisibilityChange(newVis)
    // Visible state updated
    expect(wrapper.vm.visibleDatasets).toEqual(newVis)
    // Persisted in localStorage
    const raw = localStorage.getItem(`PriceHistoryVisibility:${metalId}`)
    expect(JSON.parse(raw)).toEqual(newVis)
  })
})
