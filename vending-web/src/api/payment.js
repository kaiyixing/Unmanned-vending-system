import request from './request'

export function payOrder(data) {
  return request.post('/payment/pay', data)
}