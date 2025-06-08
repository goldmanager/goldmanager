import store from '../src/store'

describe('Vuex store authentication', () => {
  beforeEach(() => {
    store.replaceState({ isAuthenticated: false })
  })

  it('login action sets authentication state', async () => {
    await store.dispatch('login')
    expect(store.getters.isAuthenticated).toBe(true)
  })

  it('logout action clears authentication state', async () => {
    store.replaceState({ isAuthenticated: true })
    await store.dispatch('logout')
    expect(store.getters.isAuthenticated).toBe(false)
  })
})
