import request from './request'

export function getCabinetList(params) {
  return request.get('/cabinet/list', { params }).then(res => res.data.records || [])
}

export function getCabinetById(id) {
  return request.get(`/cabinet/${id}`)
}

export function getCabinetProducts(id) {
  return request.get(`/cabinet/${id}/products`)
}