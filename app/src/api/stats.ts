import request from '@/utils/request'
import type { Result, StatsOverview } from '@/types/api'

export function getOverview() {
  return request.get<any, Result<StatsOverview>>('/stats/overview')
}

export function getByCategory() {
  return request.get<any, Result<{ name: string; value: number }[]>>('/stats/by-category')
}

export function getByPriority() {
  return request.get<any, Result<{ name: string; value: number }[]>>('/stats/by-priority')
}

export function getTrend(days: number = 7) {
  return request.get<any, Result<{ date: string; count: number }[]>>('/stats/trend', { params: { days } })
}
