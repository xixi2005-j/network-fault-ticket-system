import request from '@/utils/request'
import type { Result, LoginDTO, RegisterDTO, UserVO } from '@/types/api'

export function login(data: LoginDTO) {
  return request.post<any, Result<{ token: string; user: UserVO }>>('/auth/login', data)
}

export function register(data: RegisterDTO) {
  return request.post<any, Result<UserVO>>('/auth/register', data)
}

export function getUserInfo() {
  return request.get<any, Result<UserVO>>('/auth/info')
}
