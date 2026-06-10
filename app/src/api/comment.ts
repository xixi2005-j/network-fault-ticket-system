import request from '@/utils/request'
import type { Result, CommentVO } from '@/types/api'

export function addComment(ticketId: number, content: string) {
  return request.post<any, Result<CommentVO>>(`/tickets/${ticketId}/comments`, { content })
}

export function getComments(ticketId: number) {
  return request.get<any, Result<CommentVO[]>>(`/tickets/${ticketId}/comments`)
}
