import request from './request'

export const favoriteApi = {
  add(productId) {
    return request.post('/favorites', { productId })
  },
  remove(productId) {
    return request.delete(`/favorites/${productId}`)
  },
  list(params) {
    return request.get('/favorites', { params })
  },
  check(productId) {
    return request.get(`/favorites/check/${productId}`)
  }
}
