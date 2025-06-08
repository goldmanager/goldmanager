import { mount } from '@vue/test-utils'
import { createStore } from 'vuex'
import NavBar from '../src/components/NavBar.vue'

const mountWithAuth = (isAuth) => {
  const store = createStore({
    getters: {
      isAuthenticated: () => isAuth
    },
    actions: {
      logout: () => {}
    }
  })

  return mount(NavBar, {
    global: {
      plugins: [store],
      stubs: ['router-link']
    }
  })
}

describe('NavBar', () => {
  it('shows login link when not authenticated', () => {
    const wrapper = mountWithAuth(false)
    const loginLink = wrapper.find('router-link-stub[to="/login"]')
    expect(loginLink.exists()).toBe(true)
    expect(wrapper.find('button').exists()).toBe(false)
  })

  it('shows logout button when authenticated', () => {
    const wrapper = mountWithAuth(true)
    expect(wrapper.find('button').text()).toBe('Logout')
    const linkTexts = wrapper.findAll('router-link-stub').map(l => l.text())
    expect(linkTexts).not.toContain('Login')
  })
})
