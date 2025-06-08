import { mount } from '@vue/test-utils'
import DataExportComponent from '../src/components/DataExportComponent.vue'
import axios from '../src/axios'
import { describe, it, expect, vi } from 'vitest'

vi.mock('../src/axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

const defaultData = {
  exportPassword: '',
  exportPasswordConfirm: '',
  errorMessage: '',
  statusMessage: '',
  exportStatusMessage: '',
  showExportStatus: false,
  statusIntervalId: null,
  exportStatus: '',
  exportStarted: false
}

const mountComponent = (overrides = {}) =>
  mount(DataExportComponent, {
    data() {
      return { ...defaultData, ...overrides }
    },
    global: { stubs: ['el-date-picker'] }
  })

describe('DataExportComponent', () => {
  it('disables export while running', async () => {
    axios.get.mockResolvedValue({ data: { status: 'RUNNING' } })
    const wrapper = mountComponent()
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.disableExport).toBe(true)
    expect(wrapper.vm.getExportButtonClass()).toBe('actionbutton_disabled')
  })

  it('enables export when idle', async () => {
    axios.get.mockResolvedValue({ data: { status: '' } })
    const wrapper = mountComponent()
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.disableExport).toBe(false)
    expect(wrapper.vm.getExportButtonClass()).toBe('actionbutton')
  })
})
