import request from './request'

export function createOrder(data) {
  return request.post('/order/create', data)
}

export function getOrderDetail(orderId) {
  return request.get(`/order/${orderId}`)
}

export function getUserOrders(params) {
  return request.get('/order/list', { params })
}

export function cancelOrder(orderId) {
  return request.put(`/order/${orderId}/cancel`)
}

export function applyRefund(orderId, data) {
  return request.post(`/order/${orderId}/refund`, data)
}