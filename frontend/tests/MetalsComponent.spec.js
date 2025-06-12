import { vi, describe, it, expect, beforeEach } from 'vitest'
vi.mock('../src/axios', () => ({
  default: {
    get: vi.fn().mockResolvedValue({ data: [] }),
    post: vi.fn().mockResolvedValue({ data: {} }),
    put: vi.fn().mockResolvedValue({}),
    delete: vi.fn().mockResolvedValue({})
  }
}))
import { mount } from '@vue/test-utils'
import MetalsComponent from '../src/components/MetalsComponent.vue'

// element-plus css cannot be processed in tests
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
  vi.spyOn(MetalsComponent.methods, 'fetchMetals').mockResolvedValue()
  const wrapper = mount(MetalsComponent)
  await wrapper.vm.$nextTick()
  return wrapper
}

describe('MetalsComponent', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('formats dates with leading zeros', async () => {
    const wrapper = await mountComponent()
    const date = new Date('2024-01-02T03:04:00')
    expect(wrapper.vm.formatDateCustom(date)).toBe('2024-01-02T03:04:00.000')
  })
})
