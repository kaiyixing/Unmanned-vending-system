import request from './request'

export function loginApi(data) {
  return request.post('/user/login', data)
}

export function refreshTokenApi(data) {
  return request.post('/user/refresh', data)
}

export function logoutApi(data) {
  return request.post('/user/logout', data)
}

export function userList() {
  return request.get('/user/list')
}