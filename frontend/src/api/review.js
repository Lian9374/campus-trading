import request from './request'

export const reviewApi = {
  create(data) {
    return request.post('/reviews', data)
  },
  getUserReviews(userId, params) {
    return request.get(`/reviews/user/${userId}`, { params })
  }
}
