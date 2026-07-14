import request from './request'

export const reportApi = {
  create(data) {
    return request.post('/reports', data)
  }
}
