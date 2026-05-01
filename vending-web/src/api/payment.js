import request from './request'

export function payOrder(orderId, payChannel = 'mock') {
  return request.post('/payment/pay', null, { params: { orderId, payChannel } })
}