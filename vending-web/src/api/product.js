import request from './request'

export function getProductList(params) {
  return request.get('/product/list', { params })
}

export function getProductById(id) {
  return request.get(`/product/${id}`)
}