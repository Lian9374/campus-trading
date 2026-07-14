import request from './request'

export const notificationApi = {
  list(params) {
    return request.get('/notifications', { params })
  },
  unreadCount() {
    return request.get('/notifications/unread-count')
  },
  markRead(id) {
    return request.put(`/notifications/${id}/read`)
  },
  markAllRead() {
    return request.put('/notifications/read-all')
  }
}
