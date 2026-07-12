import request from './request'

export const productApi = {
  list(params) {
    return request.get('/products', { params })
  },
  detail(id) {
    return request.get(`/products/${id}`)
  },
  create(data) {
    return request.post('/products', data)
  },
  update(id, data) {
    return request.put(`/products/${id}`, data)
  },
  updateStatus(id, status) {
    return request.put(`/products/${id}/status`, { status })
  },
  myProducts(params) {
    return request.get('/products/my', { params })
  },
  uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export const categoryApi = {
  list() {
    return request.get('/categories')
  }
}
