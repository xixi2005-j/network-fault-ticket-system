import request from '@/utils/request'
import type { Result, UserVO } from '@/types/api'

export function getUsers() {
  return request.get<any, Result<UserVO[]>>('/users')
}

export function updateUserRole(id: number, role: number) {
  return request.put<any, Result<void>>(`/users/${id}/role`, null, { params: { role } })
}

export function updateUserStatus(id: number, status: number) {
  return request.put<any, Result<void>>(`/users/${id}/status`, null, { params: { status } })
}
