import request from './request'

export function adminProductList(params) {
  return request.get('/admin/product/list', { params })
}

export function adminProductGetById(id) {
  return request.get(`/admin/product/${id}`)
}

export function adminProductSave(data) {
  return request.post('/admin/product', data)
}

export function adminProductUpdate(data) {
  return request.put('/admin/product', data)
}

export function adminProductDelete(id) {
  return request.delete(`/admin/product/${id}`)
}