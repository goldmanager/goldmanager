import { mount } from '@vue/test-utils'
import ItemsComponent from '../src/components/ItemsComponent.vue'
import { describe, it, expect, beforeEach, vi } from 'vitest'

const mountComponent = async () => {
  const wrapper = mount(ItemsComponent, {
    methods: { fetchData: vi.fn() }
  })
  await wrapper.vm.$nextTick()
  return wrapper
}

describe('ItemsComponent', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('filters items by search query', async () => {
    const wrapper = await mountComponent()
    await wrapper.setData({
      items: [
        { id: 1, name: 'Gold', itemType: { name: 't' }, unit: { name: 'u' }, itemStorage: { name: '' }, amount: 1, itemCount: 1 },
        { id: 2, name: 'Silver', itemType: { name: 't' }, unit: { name: 'u' }, itemStorage: { name: '' }, amount: 2, itemCount: 1 }
      ],
      currentSort: 'name',
      currentSortDir: 'asc',
      searchQuery: 'gold'
    })
    expect(wrapper.vm.filteredObjects).toHaveLength(1)
    expect(wrapper.vm.filteredObjects[0].name).toBe('Gold')
  })

  it('paginates items', async () => {
    const wrapper = await mountComponent()
    await wrapper.setData({
      items: [
        { id: 1, name: 'A', itemType: { name: 't' }, unit: { name: 'u' }, itemStorage: { name: '' }, amount: 1, itemCount: 1 },
        { id: 2, name: 'B', itemType: { name: 't' }, unit: { name: 'u' }, itemStorage: { name: '' }, amount: 2, itemCount: 1 },
        { id: 3, name: 'C', itemType: { name: 't' }, unit: { name: 'u' }, itemStorage: { name: '' }, amount: 3, itemCount: 1 }
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
