import request from './request'

export function getCabinetList(city) {
  const params = {}
  if (city) {
    params.city = city
  }
  return request.get('/cabinet/list', { params }).then(res => res.data)
}

export function getCabinetById(id) {
  return request.get(`/cabinet/${id}`)
}

export function getCabinetProducts(id) {
  return request.get(`/cabinet/${id}/products`)
}