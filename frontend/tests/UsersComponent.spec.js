import { mount } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import UsersComponent from '../src/components/UsersComponent.vue'

const flushPromises = () => new Promise(resolve => setTimeout(resolve))

const axiosMock = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn()
}

vi.mock('../src/axios', () => ({ default: axiosMock }))
vi.mock('../src/utils/session', () => ({
  getCurrentUsername: () => 'currentUser'
}))

const routerStub = { push: vi.fn(), currentRoute: { value: { path: '/' } } }
vi.mock('../src/router', () => ({ default: routerStub }))

const mountComponent = async () => {
  const wrapper = mount(UsersComponent)
  await flushPromises()
  return wrapper
}

describe('UsersComponent.vue', () => {
  beforeEach(() => {
    axiosMock.get.mockReset()
    axiosMock.post.mockReset()
    axiosMock.put.mockReset()
    axiosMock.delete.mockReset()
    axiosMock.get.mockResolvedValue({ data: { userInfos: [] } })
    axiosMock.post.mockResolvedValue({ data: {} })
    axiosMock.put.mockResolvedValue({})
    axiosMock.delete.mockResolvedValue({})
    localStorage.clear()
  })

  it('fetchData maps API response to internal structure', async () => {
    axiosMock.get.mockResolvedValueOnce({ data: { userInfos: [{ userName: 'alice', active: true }] } })
    const wrapper = await mountComponent()

    expect(axiosMock.get).toHaveBeenCalledWith('/userService')
    expect(wrapper.vm.userInfos).toEqual([
      { username: 'alice', active: true, password: '', passwordConfirm: '' }
    ])
  })

  it('addUser shows error message when passwords do not match', async () => {
    const wrapper = await mountComponent()
    wrapper.vm.newUser.username = 'bob'
    wrapper.vm.newUserPasword = 'a'
    wrapper.vm.newUserPaswordConfirm = 'b'

    await wrapper.vm.addUser()
    expect(wrapper.vm.errorMessage).toBe('Passwords do not match!')
    expect(axiosMock.post).not.toHaveBeenCalled()
  })

  it('adds a user when passwords match and resets the form', async () => {
    const wrapper = await mountComponent()
    const fetchSpy = vi.spyOn(wrapper.vm, 'fetchData').mockResolvedValue()
    wrapper.vm.newUser.username = 'admin2'
    wrapper.vm.newUserPasword = 'Secret123!'
    wrapper.vm.newUserPaswordConfirm = 'Secret123!'

    await wrapper.vm.addUser()
    await flushPromises()

    expect(axiosMock.post).toHaveBeenCalledWith('/userService', expect.objectContaining({
      username: 'admin2',
      password: 'Secret123!'
    }))
    expect(fetchSpy).toHaveBeenCalled()
    expect(wrapper.vm.newUser.username).toBe('')
    expect(wrapper.vm.highlightedRow).toBe('admin2')

    fetchSpy.mockRestore()
  })

  it('sortBy toggles direction and persists to localStorage', async () => {
    const wrapper = await mountComponent()
    wrapper.vm.currentSort = 'username'
    wrapper.vm.currentSortDir = 'asc'

    wrapper.vm.sortBy('username')
    expect(wrapper.vm.currentSortDir).toBe('desc')
    expect(localStorage.getItem('UsersColumnsSortDir')).toBe('desc')
  })

  it('getButtonClass returns disabled class for current user', async () => {
    const wrapper = await mountComponent()
    expect(wrapper.vm.getButtonClass(true)).toBe('actionbutton_disabled')
    expect(wrapper.vm.getButtonClass(false)).toBe('actionbutton')
  })

  it('filters users when a search query is applied', async () => {
    const wrapper = await mountComponent()
    await wrapper.setData({
      userInfos: [
        { username: 'alice', password: '', passwordConfirm: '', active: true },
        { username: 'bob', password: '', passwordConfirm: '', active: false }
      ],
      searchQuery: 'bo',
      currentSortDir: 'asc'
    })

    expect(wrapper.vm.filteredObjects.map(u => u.username)).toEqual(['bob'])
  })

  it('updates a user password when confirmation matches', async () => {
    const wrapper = await mountComponent()
    const user = { username: 'admin2', password: 'NewPass1!', passwordConfirm: 'NewPass1!', active: true }
    await wrapper.setData({ userInfos: [user] })

    await wrapper.vm.updatePassword(user)
    await flushPromises()

    expect(axiosMock.put).toHaveBeenCalledWith('/userService/updatePassword/admin2', { newPassword: 'NewPass1!' })
    expect(user.password).toBe('')
    expect(user.passwordConfirm).toBe('')
    expect(wrapper.vm.statusMessage).toBe('Password successfully updated!')
    expect(wrapper.vm.highlightedRow).toBe('admin2')
  })

  it('deletes a user after confirmation and refreshes the list', async () => {
    const wrapper = await mountComponent()
    const fetchSpy = vi.spyOn(wrapper.vm, 'fetchData').mockResolvedValue()
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true)

    await wrapper.vm.deleteUser('admin2')
    await flushPromises()

    expect(confirmSpy).toHaveBeenCalled()
    expect(axiosMock.delete).toHaveBeenCalledWith('/userService/deleteuser/admin2')
    expect(fetchSpy).toHaveBeenCalled()
    expect(wrapper.vm.highlightedRow).toBe('admin2')

    confirmSpy.mockRestore()
    fetchSpy.mockRestore()
  })
})
