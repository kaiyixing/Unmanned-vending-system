import request from './request'

export function adminInventoryList(params) {
  return request.get('/inventory/list', { params })
}

export function adminInventoryAlerts() {
  return request.get('/inventory/alerts')
}

export function adminInventoryUpdate(data) {
  return request.put('/inventory', data)
}

export function adminInventoryAdd(data) {
  return request.post('/inventory', data)
}