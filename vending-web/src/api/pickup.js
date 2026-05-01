import request from './request'

export function getPickupCode(orderId) {
  return request.get(`/pickup-code/${orderId}`)
}