import { describe, it, expect, vi } from 'vitest'

const router = { currentRoute: { value: { path: '/foo' } }, push: vi.fn() }
const store = { dispatch: vi.fn() }

vi.mock('../src/router', () => ({ default: router }))
vi.mock('../src/store', () => ({ default: store }))

describe('axios interceptor', () => {
  it('redirects to login on 403', async () => {
    const instance = (await import('../src/axios')).default
    const error = { response: { status: 403 } }
    await instance.interceptors.response.handlers[0].rejected(error).catch(() => {})
    expect(store.dispatch).toHaveBeenCalledWith('logout')
    expect(router.push).toHaveBeenCalledWith({ path: '/login' })
  })
})
