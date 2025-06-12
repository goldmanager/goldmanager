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
import ItemStorages from '../src/components/ItemStorages.vue'

const mountComponent = async () => {
  vi.spyOn(ItemStorages.methods, 'fetchData').mockResolvedValue()
  const wrapper = mount(ItemStorages)
  await wrapper.vm.$nextTick()
  return wrapper
}

describe('ItemStorages', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('filters storages by search query', async () => {
    const wrapper = await mountComponent()
    await wrapper.setData({
      itemStorages: [
        { id: 1, name: 'Store A', description: 'x' },
        { id: 2, name: 'Store B', description: 'y' }
      ],
      currentSort: 'name',
      currentSortDir: 'asc',
      searchQuery: 'store a'
    })
    expect(wrapper.vm.filteredObjects).toHaveLength(1)
    expect(wrapper.vm.filteredObjects[0].name).toBe('Store A')
  })

  it('paginates storages', async () => {
    const wrapper = await mountComponent()
    await wrapper.setData({
      itemStorages: [
        { id: 1, name: 'A', description: '' },
        { id: 2, name: 'B', description: '' },
        { id: 3, name: 'C', description: '' }
      ],
      currentSort: 'name',
      currentSortDir: 'asc',
      pageSize: 2
    })
    expect(wrapper.vm.paginatedObjects.map(o => o.name)).toEqual(['A', 'B'])
    wrapper.vm.nextPage()
    expect(wrapper.vm.paginatedObjects.map(o => o.name)).toEqual(['C'])
    wrapper.vm.prevPage()
    expect(wrapper.vm.paginatedObjects.map(o => o.name)).toEqual(['A', 'B'])
  })
})
