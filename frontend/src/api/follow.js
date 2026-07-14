import request from './request'

export const followApi = {
  follow(userId) {
    return request.post(`/follow/${userId}`)
  },
  unfollow(userId) {
    return request.delete(`/follow/${userId}`)
  },
  check(userId) {
    return request.get(`/follow/check/${userId}`)
  },
  followers(userId, params) {
    return request.get(`/follow/${userId}/followers`, { params })
  },
  following(userId, params) {
    return request.get(`/follow/${userId}/following`, { params })
  },
  stats(userId) {
    return request.get(`/follow/${userId}/stats`)
  }
}
