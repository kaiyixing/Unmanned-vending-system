import request from './request'

export function loginApi(data) {
  return request.post('/user/login', data)
}

export function userList() {
  return request.get('/user/list')
}