import request from './request'

export const commentApi = {
  create(data) {
    return request.post('/comments', data)
  },
  list(productId, params) {
    return request.get(`/comments/product/${productId}`, { params })
  }
}
