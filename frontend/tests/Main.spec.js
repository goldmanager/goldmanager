import { describe, it, expect, vi, beforeEach } from 'vitest'

// Create a root element for mounting
beforeEach(() => {
  document.body.innerHTML = '<div id="app"></div>'
})

// Mock router and store with install hooks to observe usage
const used = { router: 0, store: 0 }
const mockRouter = {
  install(app) {
    used.router++
    // provide minimal router-link/view so App and NavBar render
    app.component('router-view', { name: 'RouterView', template: '<div />' })
    app.component('router-link', { name: 'RouterLink', props: ['to'], template: '<a><slot /></a>' })
    app.config.globalProperties.$router = this
  },
  currentRoute: { value: { path: '/' } }
}
const mockStore = {
  install(app) {
    used.store++
    app.config.globalProperties.$store = {
      getters: { isAuthenticated: false },
      dispatch: () => {}
    }
  }
}

vi.mock('../src/router', () => ({ default: mockRouter }))
vi.mock('../src/store', () => ({ default: mockStore }))

describe('main.js bootstrap', () => {
  it('mounts the app and uses router + store', async () => {
    await import('../src/main.js')

    // After importing main.js, the app should be mounted
    const root = document.getElementById('app')
    expect(root).not.toBeNull()
    // Vue replaces the mount point contents
    expect(root.innerHTML).not.toBe('')

    expect(used.router).toBe(1)
    expect(used.store).toBe(1)
  })
})
