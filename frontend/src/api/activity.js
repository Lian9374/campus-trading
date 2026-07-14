import request from './request'

export const activityApi = {
  feed(params) {
    return request.get('/activities/feed', { params })
  },
  userActivities(userId, params) {
    return request.get(`/activities/user/${userId}`, { params })
  }
}
