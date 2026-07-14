import request from './request'

export const userApi = {
  getProfile(userId) {
    return request.get(`/user/profile/${userId}`)
  }
}
