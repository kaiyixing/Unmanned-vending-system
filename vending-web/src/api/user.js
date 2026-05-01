import request from './request'

export function loginApi(data) {
  return request.post('/user/login', data)
}

export function registerApi(data) {
  return request.post('/user/register', data)
}

export function getUserInfo() {
  return request.get('/user/info')
}

export function updateUserInfo(data) {
  return request.put('/user/info', data)
}