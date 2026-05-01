import request from './request'

export function adminCabinetList(params) {
  return request.get('/admin/cabinet/list', { params })
}

export function adminCabinetGetById(id) {
  return request.get(`/admin/cabinet/${id}`)
}

export function adminCabinetSave(data) {
  return request.post('/admin/cabinet', data)
}

export function adminCabinetUpdate(data) {
  return request.put('/admin/cabinet', data)
}

export function adminCabinetDelete(id) {
  return request.delete(`/admin/cabinet/${id}`)
}