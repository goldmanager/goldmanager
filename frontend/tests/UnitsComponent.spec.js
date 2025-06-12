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
import UnitsComponent from '../src/components/UnitsComponent.vue'

vi.mock('../src/axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}))

const mountComponent = async () => {
  vi.spyOn(UnitsComponent.methods, 'fetchUnits').mockResolvedValue()
  const wrapper = mount(UnitsComponent)
  await wrapper.vm.$nextTick()
  return wrapper
}

describe('UnitsComponent', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('sorts units by name', async () => {
    const wrapper = await mountComponent()
    await wrapper.setData({
      units: [
        { name: 'b', factor: 1 },
        { name: 'a', factor: 1 }
      ],
      currentSort: 'name',
      currentSortDir: 'asc'
    })
    expect(wrapper.vm.sortedObjects.map(u => u.name)).toEqual(['a', 'b'])
  })
})
