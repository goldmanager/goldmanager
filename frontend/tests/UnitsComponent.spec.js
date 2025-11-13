import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import UnitsComponent from '../src/components/UnitsComponent.vue'

const flushPromises = () => new Promise(resolve => setTimeout(resolve))

const axiosMock = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn()
}

vi.mock('../src/axios', () => ({ default: axiosMock }))

const mountComponent = async () => {
  const wrapper = mount(UnitsComponent)
  await flushPromises()
  return wrapper
}

describe('UnitsComponent', () => {
  beforeEach(() => {
    localStorage.clear()
    Object.values(axiosMock).forEach(mock => mock.mockReset())
    axiosMock.get.mockResolvedValue({ data: [] })
    axiosMock.post.mockResolvedValue({ data: {} })
    axiosMock.put.mockResolvedValue({})
    axiosMock.delete.mockResolvedValue({})
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

  it('adds a unit and resets the form', async () => {
    const wrapper = await mountComponent()
    const created = { name: 'Gram', factor: 0.035 }
    axiosMock.post.mockResolvedValueOnce({ data: created })

    await wrapper.setData({
      newUnit: { ...created }
    })

    await wrapper.vm.addUnit()
    await flushPromises()

    expect(axiosMock.post).toHaveBeenCalledWith('/units', created)
    expect(wrapper.vm.units).toContainEqual(created)
    expect(wrapper.vm.newUnit).toEqual({ name: '', factor: 1 })
    expect(wrapper.vm.highlightedRow).toBe(created.name)
  })

  it('updates the currently edited unit', async () => {
    const wrapper = await mountComponent()
    const fetchSpy = vi.spyOn(wrapper.vm, 'fetchUnits').mockResolvedValue()
    const edited = { name: 'Pound', factor: 2 }
    wrapper.vm.editedObject = { ...edited }

    await wrapper.vm.updateUnit()
    await flushPromises()

    expect(axiosMock.put).toHaveBeenCalledWith('/units/Pound', edited)
    expect(fetchSpy).toHaveBeenCalled()
    expect(wrapper.vm.editedObject).toBe(null)
    expect(wrapper.vm.highlightedRow).toBe('Pound')

    fetchSpy.mockRestore()
  })

  it('deletes a unit after confirmation', async () => {
    const wrapper = await mountComponent()
    const fetchSpy = vi.spyOn(wrapper.vm, 'fetchUnits').mockResolvedValue()
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true)

    await wrapper.vm.deleteUnit('Ounce')
    await flushPromises()

    expect(confirmSpy).toHaveBeenCalled()
    expect(axiosMock.delete).toHaveBeenCalledWith('/units/Ounce')
    expect(fetchSpy).toHaveBeenCalled()
    expect(wrapper.vm.highlightedRow).toBe('Ounce')

    confirmSpy.mockRestore()
    fetchSpy.mockRestore()
  })

  it('filters units using the search query', async () => {
    const wrapper = await mountComponent()
    await wrapper.setData({
      units: [
        { name: 'Gram', factor: 1 },
        { name: 'Ounce', factor: 1 }
      ],
      searchQuery: 'oun',
      currentSort: 'name',
      currentSortDir: 'asc'
    })

    expect(wrapper.vm.filteredObjects.map(u => u.name)).toEqual(['Ounce'])
  })
})
