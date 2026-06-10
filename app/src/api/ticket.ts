import request from '@/utils/request'
import type { Result, PageResult, TicketVO, TicketDTO, TicketPageQuery } from '@/types/api'

export function createTicket(data: TicketDTO) {
  return request.post<any, Result<TicketVO>>('/tickets', data)
}

export function getTickets(params: TicketPageQuery) {
  return request.get<any, Result<PageResult<TicketVO>>>('/tickets', { params })
}

export function getTicketDetail(id: number) {
  return request.get<any, Result<TicketVO>>(`/tickets/${id}`)
}

export function updateTicket(id: number, data: TicketDTO) {
  return request.put<any, Result<TicketVO>>(`/tickets/${id}`, data)
}

export function deleteTicket(id: number) {
  return request.delete<any, Result<void>>(`/tickets/${id}`)
}

export function changeTicketStatus(id: number, status: number) {
  return request.put<any, Result<TicketVO>>(`/tickets/${id}/status`, null, { params: { status } })
}

export function assignTicket(id: number, assigneeId: number) {
  return request.put<any, Result<TicketVO>>(`/tickets/${id}/assign`, null, { params: { assigneeId } })
}

export function acceptTicket(id: number) {
  return request.put<any, Result<TicketVO>>(`/tickets/${id}/accept`)
}

export function rateTicket(id: number, satisfaction: number, comment?: string) {
  return request.put<any, Result<TicketVO>>(`/tickets/${id}/rate`, null, { params: { satisfaction, comment } })
}

export function reopenTicket(id: number, reason: string) {
  return request.put<any, Result<TicketVO>>(`/tickets/${id}/reopen`, null, { params: { reason } })
}
