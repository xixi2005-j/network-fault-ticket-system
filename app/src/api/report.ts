import request from '@/utils/request'
import type { Result, CompletionReportVO, CompletionReportDTO } from '@/types/api'

export function submitReport(ticketId: number, data: CompletionReportDTO) {
  return request.post<any, Result<CompletionReportVO>>(`/reports/ticket/${ticketId}`, data)
}

export function getReportByTicketId(ticketId: number) {
  return request.get<any, Result<CompletionReportVO>>(`/reports/ticket/${ticketId}`)
}

export function approveReport(reportId: number) {
  return request.put<any, Result<CompletionReportVO>>(`/reports/${reportId}/approve`)
}

export function rejectReport(reportId: number, rejectReason: string) {
  return request.put<any, Result<CompletionReportVO>>(`/reports/${reportId}/reject`, null, { params: { rejectReason } })
}
