import request from './request'

export const orderApi = {
  create(data) {
    return request.post('/orders', data)
  },
  myOrders(params) {
    return request.get('/orders', { params })
  },
  confirm(id) {
    return request.put(`/orders/${id}/confirm`)
  },
  complete(id) {
    return request.put(`/orders/${id}/complete`)
  },
  cancel(id) {
    return request.put(`/orders/${id}/cancel`)
  }
}
