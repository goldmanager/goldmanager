import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import MetalsComponent from '../src/components/MetalsComponent.vue'

const flushPromises = () => new Promise(resolve => setTimeout(resolve))

const axiosMock = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn()
}))

vi.mock('../src/axios', () => ({ default: axiosMock }))
vi.mock('element-plus/theme-chalk/el-date-picker.css', () => ({}), { virtual: true })
vi.mock('element-plus', () => ({ ElDatePicker: { name: 'el-date-picker', template: '<div />' } }))
vi.mock('element-plus/es/components/date-picker/style/css', () => ({}), { virtual: true })

const mountComponent = async () => {
  const wrapper = mount(MetalsComponent)
  await flushPromises()
  return wrapper
}

describe('MetalsComponent', () => {
  beforeEach(() => {
    localStorage.clear()
    Object.values(axiosMock).forEach(mock => mock.mockReset())
    axiosMock.get.mockResolvedValue({ data: [] })
    axiosMock.post.mockResolvedValue({ data: {} })
    axiosMock.put.mockResolvedValue({})
    axiosMock.delete.mockResolvedValue({})
  })

  it('formats dates with leading zeros', async () => {
    const wrapper = await mountComponent()
    const date = new Date('2024-01-02T03:04:00')
    expect(wrapper.vm.formatDateCustom(date)).toBe('2024-01-02T03:04:00.000')
  })

  it('adds a metal, resets the form, and highlights the new row', async () => {
    const wrapper = await mountComponent()
    const entryDate = '2024-05-10T06:30:00.000'
    const createdMetal = { id: 42, name: 'Testium', price: 123.45 }
    axiosMock.post.mockResolvedValueOnce({ data: createdMetal })

    await wrapper.setData({
      newMaterial: {
        name: createdMetal.name,
        price: createdMetal.price,
        entryDate
      }
    })

    await wrapper.vm.addMaterial()
    await flushPromises()

    expect(axiosMock.post).toHaveBeenCalledWith('/materials', {
      entryDate: new Date(entryDate).toJSON(),
      name: createdMetal.name,
      price: createdMetal.price
    })
    expect(wrapper.vm.metals).toContainEqual(createdMetal)
    expect(wrapper.vm.newMaterial.name).toBe('')
    expect(wrapper.vm.highlightedRow).toBe(createdMetal.id)
  })

  it('filters metals by the current search query', async () => {
    const wrapper = await mountComponent()
    await wrapper.setData({
      metals: [
        { id: 1, name: 'Gold', price: 1 },
        { id: 2, name: 'Silver', price: 1 }
      ],
      searchQuery: 'sil',
      currentSort: 'name',
      currentSortDir: 'asc'
    })

    expect(wrapper.vm.filteredObjects.map(m => m.name)).toEqual(['Silver'])
  })

  it('updates an edited metal and reloads the list', async () => {
    const wrapper = await mountComponent()
    const fetchSpy = vi.spyOn(wrapper.vm, 'fetchMetals').mockResolvedValue()
    const edited = { id: 7, name: 'Aluminum', price: 22 }
    wrapper.vm.editedObject = { ...edited }

    await wrapper.vm.updateObject()
    await flushPromises()

    expect(axiosMock.put).toHaveBeenCalledWith('/materials/7', expect.objectContaining({
      id: 7,
      name: 'Aluminum',
      price: 22
    }))
    expect(fetchSpy).toHaveBeenCalled()
    expect(wrapper.vm.editedObject).toBe(null)
    expect(wrapper.vm.highlightedRow).toBe(7)

    fetchSpy.mockRestore()
  })

  it('deletes a metal after confirmation', async () => {
    const wrapper = await mountComponent()
    const fetchSpy = vi.spyOn(wrapper.vm, 'fetchMetals').mockResolvedValue()
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true)

    await wrapper.vm.deleteObject(11)
    await flushPromises()

    expect(confirmSpy).toHaveBeenCalled()
    expect(axiosMock.delete).toHaveBeenCalledWith('/materials/11')
    expect(fetchSpy).toHaveBeenCalled()
    expect(wrapper.vm.highlightedRow).toBe(11)

    confirmSpy.mockRestore()
    fetchSpy.mockRestore()
  })
})
