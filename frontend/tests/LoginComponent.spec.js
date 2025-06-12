import { mount } from '@vue/test-utils'
import { createStore } from 'vuex'
import LoginComponent from '../src/components/LoginComponent.vue'
import axios from '../src/axios'
import { describe, it, expect, beforeEach, vi } from 'vitest'

vi.mock('../src/axios', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn().mockResolvedValue({ data: { status: '' } })
  }
}))

const mountComponent = () => {
  const loginAction = vi.fn()
  const store = createStore({
    actions: { login: loginAction }
  })
  const router = { push: vi.fn() }
  const wrapper = mount(LoginComponent, {
    global: {
      plugins: [store],
      mocks: { $router: router }
    }
  })
  return { wrapper, loginAction, router }
}

describe('LoginComponent', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('displays error message on failed login', async () => {
    axios.post.mockRejectedValue(new Error('fail'))
    const { wrapper } = mountComponent()
    await wrapper.setData({ username: 'u', password: 'p' })
    await wrapper.find('form').trigger('submit.prevent')
    expect(wrapper.vm.errorMessage).toBe('Login failed. Please check your credentials.')
  })

  it('dispatches login action on success', async () => {
    axios.post.mockResolvedValue({ data: { refreshAfter: 'r' } })
    const { wrapper, loginAction, router } = mountComponent()
    await wrapper.setData({ username: 'u', password: 'p' })
    await wrapper.find('form').trigger('submit.prevent')
    expect(loginAction).toHaveBeenCalled()
    expect(router.push).toHaveBeenCalledWith('/')
  })
})
