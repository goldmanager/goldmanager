import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'

// Stub NavBar to avoid deep rendering
vi.mock('../src/components/NavBar.vue', () => ({
  default: { name: 'NavBar', template: '<div class="navbar-stub" />' }
}))

describe('App.vue', () => {
  it('renders NavBar and router-view', async () => {
    const App = (await import('../src/App.vue')).default
    const wrapper = mount(App, {
      global: {
        stubs: { 'router-view': { template: '<div class="router-view-stub" />' } }
      }
    })

    expect(wrapper.find('.navbar-stub').exists()).toBe(true)
    expect(wrapper.find('.router-view-stub').exists()).toBe(true)
  })
})

