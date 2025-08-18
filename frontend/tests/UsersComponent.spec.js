import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock axios used by the component
const axiosMock = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn()
}
vi.mock('../src/axios', () => ({ default: axiosMock }))

// Mock session util used for current user checks
vi.mock('../src/utils/session', () => ({
  getCurrentUsername: () => 'currentUser'
}))

// Minimal router stub to satisfy <router-link/> or navigation if any
const routerStub = { push: vi.fn(), currentRoute: { value: { path: '/' } } }
vi.mock('../src/router', () => ({ default: routerStub }))

describe('UsersComponent.vue', () => {
  beforeEach(() => {
    axiosMock.get.mockReset()
    axiosMock.post.mockReset()
    axiosMock.put.mockReset()
    axiosMock.delete.mockReset()
    // Default GET to a benign empty payload to avoid mounted() failures
    axiosMock.get.mockResolvedValue({ data: { userInfos: [] } })
    localStorage.clear()
  })

  it('fetchData maps API response to internal structure', async () => {
    axiosMock.get.mockResolvedValueOnce({ data: { userInfos: [ { userName: 'alice', active: true } ] } })
    const Comp = (await import('../src/components/UsersComponent.vue')).default
    const wrapper = mount(Comp)

    // allow mounted() -> fetchData() promise to resolve
    await Promise.resolve()
    await Promise.resolve()
    expect(axiosMock.get).toHaveBeenCalledWith('/userService')
    expect(wrapper.vm.userInfos).toEqual([
      { username: 'alice', active: true, password: '', passwordConfirm: '' }
    ])
  })

  it('addUser shows error when passwords do not match', async () => {
    const Comp = (await import('../src/components/UsersComponent.vue')).default
    const wrapper = mount(Comp)
    wrapper.vm.newUser.username = 'bob'
    wrapper.vm.newUserPasword = 'a'
    wrapper.vm.newUserPaswordConfirm = 'b'

    await wrapper.vm.addUser()
    expect(wrapper.vm.errorMessage).toBe('Passwords do not match!')
    expect(axiosMock.post).not.toHaveBeenCalled()
  })

  it('sortBy toggles direction and persists to localStorage', async () => {
    const Comp = (await import('../src/components/UsersComponent.vue')).default
    const wrapper = mount(Comp)
    wrapper.vm.currentSort = 'username'
    wrapper.vm.currentSortDir = 'asc'

    wrapper.vm.sortBy('username')
    expect(wrapper.vm.currentSortDir).toBe('desc')
    expect(localStorage.getItem('UsersColumnsSortDir')).toBe('desc')
  })

  it('getButtonClass returns disabled class for current user', async () => {
    const Comp = (await import('../src/components/UsersComponent.vue')).default
    const wrapper = mount(Comp)
    expect(wrapper.vm.getButtonClass(true)).toBe('actionbutton_disabled')
    expect(wrapper.vm.getButtonClass(false)).toBe('actionbutton')
  })
})
