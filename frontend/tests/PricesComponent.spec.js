import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'

// Axios mock
const axiosMock = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn()
}
vi.mock('../src/axios', () => ({ default: axiosMock }))

// Simple helper to settle pending promises
const flush = async () => { await Promise.resolve(); await Promise.resolve() }

const priceItem = (id, name, amount = 1, unitName = 'g', itemCount = 1, materialName = 'Gold') => ({
  item: {
    id,
    name,
    amount,
    unit: { name: unitName },
    itemCount,
    itemType: { material: { name: materialName } },
    itemStorage: { name: 'Vault' }
  },
  price: id * 10,
  priceTotal: id * 100
})

describe('PricesComponent.vue', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    axiosMock.get.mockReset()
    localStorage.clear()
  })

  it('renders header and loads default price list', async () => {
    // Default mock sequence: /prices, /materials, /itemStorages
    axiosMock.get
      .mockImplementationOnce(async (url) => ({ data: { totalPrice: 0, prices: [priceItem(1, 'Alpha')] } }))
      .mockImplementationOnce(async (url) => ({ data: [ { id: 1, name: 'Gold' } ] }))
      .mockImplementationOnce(async (url) => ({ data: [ { id: 10, name: 'Home' } ] }))

    const Comp = (await import('../src/components/PricesComponent.vue')).default
    const wrapper = mount(Comp)

    await flush()

    expect(wrapper.find('h1').text()).toBe('Prices')
    // verify requests happened for default view
    expect(axiosMock.get).toHaveBeenCalledWith('/prices')
    expect(axiosMock.get).toHaveBeenCalledWith('/materials')
    expect(axiosMock.get).toHaveBeenCalledWith('/itemStorages')
    expect(wrapper.vm.priceList.prices.length).toBe(1)
  })

  it('toggles view type and fetches groups, persists selection', async () => {
    // Initial price list calls
    axiosMock.get
      .mockResolvedValueOnce({ data: { totalPrice: 0, prices: [] } })
      .mockResolvedValueOnce({ data: [] }) // materials
      .mockResolvedValueOnce({ data: [] }) // itemStorages

    const Comp = (await import('../src/components/PricesComponent.vue')).default
    const wrapper = mount(Comp)
    await flush()

    // Switch to GroupByMetal
    axiosMock.get
      .mockResolvedValueOnce({ data: { priceGroups: [ { groupName: 'Gold', prices: [priceItem(2, 'Bravo')] } ] } })
      .mockResolvedValueOnce({ data: [] }) // materials
      .mockResolvedValueOnce({ data: [] }) // itemStorages

    wrapper.vm.setCurrentViewType({ target: { value: 'GroupByMetal' } })
    await flush()

    expect(localStorage.getItem('PriceViewType')).toBe('GroupByMetal')
    expect(wrapper.vm.currentViewType).toBe('GroupByMetal')
    expect(axiosMock.get).toHaveBeenCalledWith('/prices/groupBy/material')
    expect(wrapper.vm.priceGroups.length).toBe(1)
  })

  it('sortPriceListBy toggles direction and affects sorted order', async () => {
    axiosMock.get
      .mockResolvedValueOnce({ data: { totalPrice: 0, prices: [] } })
      .mockResolvedValueOnce({ data: [] }) // materials
      .mockResolvedValueOnce({ data: [] }) // itemStorages

    const Comp = (await import('../src/components/PricesComponent.vue')).default
    const wrapper = mount(Comp)
    await flush()

    // Seed prices
    wrapper.vm.priceList = { totalPrice: 0, prices: [ priceItem(2, 'Bravo'), priceItem(1, 'Alpha') ] }
    wrapper.vm.currentPriceListSort = 'name'
    wrapper.vm.currentPriceListSortDir = 'asc'

    // Ascending by default
    let names = wrapper.vm.sortedPriceList.map(p => p.item.name)
    expect(names).toEqual(['Alpha', 'Bravo'])

    // Toggle to desc by clicking same column
    wrapper.vm.sortPriceListBy('name')
    names = wrapper.vm.sortedPriceList.map(p => p.item.name)
    expect(wrapper.vm.currentPriceListSort).toBe('name')
    expect(wrapper.vm.currentPriceListSortDir).toBe('desc')
    expect(names).toEqual(['Bravo', 'Alpha'])

    // Column is persisted
    expect(localStorage.getItem('PriceListColumnsSort')).toBe('name')
  })

  it('paginates price list', async () => {
    axiosMock.get
      .mockResolvedValueOnce({ data: { totalPrice: 0, prices: [] } })
      .mockResolvedValueOnce({ data: [] }) // materials
      .mockResolvedValueOnce({ data: [] }) // itemStorages

    const Comp = (await import('../src/components/PricesComponent.vue')).default
    const wrapper = mount(Comp)
    await flush()

    wrapper.vm.priceListPageSize = 1
    wrapper.vm.priceList = { totalPrice: 0, prices: [ priceItem(1, 'Alpha'), priceItem(2, 'Bravo') ] }
    wrapper.vm.currentPriceListSort = 'name'
    wrapper.vm.currentPriceListSortDir = 'asc'

    // Page 1
    let page = wrapper.vm.paginatedPriceList
    expect(page.prices).toHaveLength(1)
    expect(page.prices[0].item.name).toBe('Alpha')

    // Next page
    wrapper.vm.nextPriceListPage()
    page = wrapper.vm.paginatedPriceList
    expect(wrapper.vm.currentPriceListPage).toBe(2)
    expect(page.prices[0].item.name).toBe('Bravo')

    // Prev page
    wrapper.vm.prevPriceListPage()
    page = wrapper.vm.paginatedPriceList
    expect(wrapper.vm.currentPriceListPage).toBe(1)
    expect(page.prices[0].item.name).toBe('Alpha')
  })
})

