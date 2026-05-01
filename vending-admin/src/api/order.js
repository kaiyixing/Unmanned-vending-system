import request from './request'

export function adminOrderList(params) {
  return request.get('/admin/order/list', { params })
}

export function adminOrderGetById(id) {
  return request.get(`/admin/order/${id}`)
}

export function getRefundList(params) {
  return request.get('/refund/admin/list', { params })
}

export function auditRefund(refundId, approved, remark) {
  return request.post('/refund/admin/audit', { refundId, approved, remark })
}