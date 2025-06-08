import { mount } from '@vue/test-utils'
import DataImportComponent from '../src/components/DataImportComponent.vue'
import axios from '../src/axios'
import { describe, it, expect, beforeEach, vi } from 'vitest'

vi.mock('../src/axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}))

const mountComponent = (overrides = {}) =>
  mount(DataImportComponent, {
    data() {
      return { ...overrides }
    },
    methods: { checkImportStatus: vi.fn(), startStatusInterval: vi.fn() }
  })

describe('DataImportComponent', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('disables import when status is running', () => {
    const wrapper = mountComponent({ fileData: 'x', importStatus: 'RUNNING' })
    expect(wrapper.vm.disableImport).toBe(true)
    expect(wrapper.vm.getImportButtonClass()).toBe('actionbutton_disabled')
  })

  it('shows error when passwords do not match', async () => {
    const wrapper = mountComponent({ fileData: 'abc' })
    await wrapper.setData({ importPassword: 'a', importPasswordConfirm: 'b' })
    await wrapper.vm.importData()
    expect(wrapper.vm.errorMessage).toBe('Passwords do not match!')
  })
})
