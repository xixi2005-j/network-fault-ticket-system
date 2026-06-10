/** 通用响应结构 */
export interface Result<T> {
  code: number
  message: string
  data: T
}

/** 分页响应 */
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

/** 用户 */
export interface UserVO {
  id: number
  username: string
  realName: string | null
  phone: string | null
  email: string | null
  role: number
  status: number
  createTime: string
}

/** 登录请求 */
export interface LoginDTO {
  username: string
  password: string
}

/** 注册请求 */
export interface RegisterDTO {
  username: string
  password: string
  realName?: string
  phone?: string
  email?: string
}

/** 工单 */
export interface TicketVO {
  id: number
  title: string
  description: string | null
  category: number
  categoryName: string
  priority: number
  priorityName: string
  status: number
  statusName: string
  creatorId: number
  creatorName: string | null
  assigneeId: number | null
  assigneeName: string | null
  createTime: string
  updateTime: string
  resolveTime: string | null
}

/** 创建/编辑工单 */
export interface TicketDTO {
  title: string
  description?: string
  category: number
  priority?: number
}

/** 工单分页查询 */
export interface TicketPageQuery {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
  priority?: number
  category?: number
}

/** 评论 */
export interface CommentVO {
  id: number
  ticketId: number
  userId: number
  username: string | null
  realName: string | null
  content: string
  createTime: string
}

/** 统计概览 */
export interface StatsOverview {
  total: number
  pending: number
  processing: number
  completed: number
}
