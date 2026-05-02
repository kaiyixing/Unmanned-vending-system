import request from './request'

export function verifyPickupCode(codeValue) {
  return request.post('/pickup-code/verify', null, {
    params: { codeValue }
  })
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function uploadImageWithPrefix(formData) {
  return request.post('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
