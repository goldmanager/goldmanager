import { mount } from '@vue/test-utils'
import PriceChart from '../src/components/PriceChart.vue'

describe('PriceChart', () => {
  it('creates chart data from modelValue', () => {
    const prices = [
      { date: '2024-01-01', totalPrice: 10, metalPrice: 5 },
      { date: '2024-01-02', totalPrice: 15, metalPrice: 8 }
    ]
    const wrapper = mount(PriceChart, {
      props: { modelValue: prices },
      global: {
        stubs: {
          'line-chart': true
        }
      }
    })

    const chartData = wrapper.vm.chartData
    expect(chartData.labels).toEqual(['2024-01-01', '2024-01-02'])
    expect(chartData.datasets[0].data).toEqual([10, 15])
    expect(chartData.datasets[1].data).toEqual([5, 8])
  })
})
