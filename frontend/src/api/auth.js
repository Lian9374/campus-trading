import request from './request'

export const authApi = {
  login(data) {
    return request.post('/auth/login', data)
  },
  register(data) {
    return request.post('/auth/register', data)
  }
}

export const userApi = {
  getMe() {
    return request.get('/user/me')
  },
  updateMe(data) {
    return request.put('/user/me', data)
  },
  searchUsers(keyword, params) {
    return request.get('/user/search', { params: { keyword, ...params } })
  }
}
