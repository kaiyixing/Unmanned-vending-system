import request from './request'

export function adminOrderList(params) {
  return request.get('/admin/order/list', { params })
}

export function adminOrderGetById(id) {
  return request.get(`/admin/order/${id}`)
}