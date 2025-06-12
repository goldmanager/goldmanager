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
import ItemTypes from '../src/components/ItemTypes.vue'

const mountComponent = async () => {
  vi.spyOn(ItemTypes.methods, 'fetchData').mockResolvedValue()
  const wrapper = mount(ItemTypes)
  await wrapper.vm.$nextTick()
  return wrapper
}

describe('ItemTypes', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('resolves material names', async () => {
    const wrapper = await mountComponent()
    await wrapper.setData({
      metals: [
        { value: 1, text: 'Gold' },
        { value: 2, text: 'Silver' }
      ]
    })
    expect(wrapper.vm.getMaterialName(2)).toBe('Silver')
  })

  it('filters types by search query', async () => {
    const wrapper = await mountComponent()
    await wrapper.setData({
      itemTypes: [
        { id: 1, name: 'Coin', modifier: 1, material: { name: 'Gold' } },
        { id: 2, name: 'Bar', modifier: 1, material: { name: 'Gold' } }
      ],
      currentSort: 'name',
      currentSortDir: 'asc',
      searchQuery: 'coi'
    })
    expect(wrapper.vm.filteredObjects).toHaveLength(1)
    expect(wrapper.vm.filteredObjects[0].name).toBe('Coin')
  })
})
