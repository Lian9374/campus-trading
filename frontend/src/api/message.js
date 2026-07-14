import request from './request'

export const messageApi = {
  send(data) {
    return request.post('/messages', data)
  },
  conversations(params) {
    return request.get('/messages/conversations', { params })
  },
  getMessages(conversationId, params) {
    return request.get(`/messages/conversations/${conversationId}`, { params })
  },
  markRead(conversationId) {
    return request.put(`/messages/conversations/${conversationId}/read`)
  },
  unreadCount() {
    return request.get('/messages/unread-count')
  }
}
