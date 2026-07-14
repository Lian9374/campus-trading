import request from './request'

export const userApi = {
  getProfile(userId) {
    return request.get(`/user/profile/${userId}`)
  },
  discoverUsers(keyword, params) {
    return request.get('/user/discover', { params: { keyword, ...params } })
  }
}
