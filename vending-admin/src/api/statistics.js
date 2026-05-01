import request from './request'

export function statisticsOverview() {
  return request.get('/statistics/overview')
}

export function statisticsByRange(params) {
  return request.get('/statistics/range', { params })
}

export function refundList(params) {
  return request.get('/refund/admin/list', { params })
}

export function refundAudit(data) {
  return request.post('/refund/admin/audit', data)
}