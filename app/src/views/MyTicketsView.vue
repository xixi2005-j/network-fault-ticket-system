<template>
  <div class="my-tickets-page">
    <!-- 统计卡片区域 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="4" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card total">
          <div class="stat-icon">
            <el-icon :size="28"><Tickets /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ stats.total }}</div>
            <div class="stat-label">全部工单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card pending">
          <div class="stat-icon">
            <el-icon :size="28"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ stats.pending }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card processing">
          <div class="stat-icon">
            <el-icon :size="28"><Loading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ stats.processing }}</div>
            <div class="stat-label">处理中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card reviewing">
          <div class="stat-icon">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ stats.reviewing }}</div>
            <div class="stat-label">审核中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card completed">
          <div class="stat-icon">
            <el-icon :size="28"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ stats.completed }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card closed">
          <div class="stat-icon">
            <el-icon :size="28"><SuccessFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-num">{{ stats.closed }}</div>
            <div class="stat-label">已结束</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待处理操作区域 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <!-- 待验收工单 -->
      <el-col :span="12">
        <el-card shadow="hover" class="action-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon :size="20" color="#E6A23C"><Warning /></el-icon>
                <span>待验收工单</span>
              </div>
              <el-badge :value="pendingAcceptTickets.length" :max="99" type="warning" />
            </div>
          </template>
          <div v-if="pendingAcceptTickets.length === 0" class="empty-state">
            <el-icon :size="48" color="#C0C4CC"><CircleCheck /></el-icon>
            <p>暂无待验收工单</p>
          </div>
          <div v-else class="ticket-list">
            <div v-for="ticket in pendingAcceptTickets" :key="ticket.id" class="ticket-item">
              <div class="ticket-main">
                <router-link :to="`/tickets/${ticket.id}`" class="ticket-title">{{ ticket.title }}</router-link>
                <div class="ticket-meta">
                  <el-tag :type="priorityType(ticket.priority)" size="small" effect="plain">{{ ticket.priorityName }}</el-tag>
                  <span class="meta-text">{{ ticket.assigneeName || '未指派' }} · {{ formatTime(ticket.resolveTime) }}</span>
                </div>
              </div>
              <div class="ticket-actions">
                <el-button type="success" size="small" @click="confirmAccept(ticket.id)" :loading="acceptingId === ticket.id">
                  <el-icon><Check /></el-icon>验收
                </el-button>
                <el-button type="warning" size="small" plain @click="showReopenDialog(ticket.id)">
                  <el-icon><RefreshLeft /></el-icon>返工
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 待评价工单 -->
      <el-col :span="12">
        <el-card shadow="hover" class="action-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon :size="20" color="#409EFF"><Star /></el-icon>
                <span>待评价工单</span>
              </div>
              <el-badge :value="pendingRateTickets.length" :max="99" type="primary" />
            </div>
          </template>
          <div v-if="pendingRateTickets.length === 0" class="empty-state">
            <el-icon :size="48" color="#C0C4CC"><Star /></el-icon>
            <p>暂无待评价工单</p>
          </div>
          <div v-else class="ticket-list">
            <div v-for="ticket in pendingRateTickets" :key="ticket.id" class="ticket-item">
              <div class="ticket-main">
                <router-link :to="`/tickets/${ticket.id}`" class="ticket-title">{{ ticket.title }}</router-link>
                <div class="ticket-meta">
                  <el-tag :type="priorityType(ticket.priority)" size="small" effect="plain">{{ ticket.priorityName }}</el-tag>
                  <span class="meta-text">{{ ticket.assigneeName || '未指派' }} · {{ formatTime(ticket.closeTime) }}</span>
                </div>
              </div>
              <div class="ticket-actions">
                <el-button type="primary" size="small" @click="showRatingDialog(ticket.id)">
                  <el-icon><Star /></el-icon>评价
                </el-button>
                <el-button type="warning" size="small" plain @click="showReopenDialog(ticket.id)">
                  <el-icon><RefreshLeft /></el-icon>返工
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 全部工单列表 -->
    <el-card shadow="hover" class="table-card" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>全部工单</span>
          <el-radio-group v-model="statusFilter" size="small" @change="loadTickets">
            <el-radio-button :value="0">全部</el-radio-button>
            <el-radio-button :value="1">待处理</el-radio-button>
            <el-radio-button :value="2">处理中</el-radio-button>
            <el-radio-button :value="3">审核中</el-radio-button>
            <el-radio-button :value="4">已完成</el-radio-button>
            <el-radio-button :value="5">已结束</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-table :data="filteredTickets" stripe v-loading="loading" style="width: 100%">
        <el-table-column label="ID" width="70">
          <template #default="{ row }">{{ String(row.id).padStart(3, '0') }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/tickets/${row.id}`" class="link">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small" effect="plain">{{ row.priorityName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small" effect="dark">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="处理人" width="100">
          <template #default="{ row }">{{ row.assigneeName || '未指派' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/tickets/${row.id}`)">
              <el-icon><View /></el-icon>查看
            </el-button>
            <el-button v-if="row.status === 4" link type="success" @click="confirmAccept(row.id)" :loading="acceptingId === row.id">
              <el-icon><Check /></el-icon>验收
            </el-button>
            <el-button v-if="row.status === 4 || row.status === 5" link type="warning" @click="showReopenDialog(row.id)">
              <el-icon><RefreshLeft /></el-icon>返工
            </el-button>
            <el-button v-if="row.status === 5 && !row.satisfaction" link type="primary" @click="showRatingDialog(row.id)">
              <el-icon><Star /></el-icon>评价
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 返工对话框 -->
    <el-dialog v-model="reopenDialogVisible" title="申请返工" width="450px" :close-on-click-modal="false">
      <el-form :model="reopenForm" label-width="80px">
        <el-form-item label="工单" required>
          <span>{{ currentTicketTitle }}</span>
        </el-form-item>
        <el-form-item label="原因" required>
          <el-input v-model="reopenForm.reason" type="textarea" :rows="4" placeholder="请详细说明返工的原因，以便运维人员更好地处理问题" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reopenDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="handleReopen" :loading="reopening">确认返工</el-button>
      </template>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog v-model="ratingDialogVisible" title="评价工单" width="450px" :close-on-click-modal="false">
      <el-form :model="ratingForm" label-width="80px">
        <el-form-item label="工单" required>
          <span>{{ currentTicketTitle }}</span>
        </el-form-item>
        <el-form-item label="评分" required>
          <el-rate v-model="ratingForm.score" show-score allow-half :colors="['#F56C6C', '#E6A23C', '#67C23A']" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="ratingForm.comment" type="textarea" :rows="4" placeholder="请输入您的评价，帮助我们改进服务（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ratingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRate" :loading="ratinging">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTickets, acceptTicket, rateTicket, reopenTicket } from '@/api/ticket'
import { useUserStore } from '@/stores/user'
import type { TicketVO } from '@/types/api'

const userStore = useUserStore()
const loading = ref(false)
const tickets = ref<TicketVO[]>([])
const statusFilter = ref(0)

// 统计数据
const stats = reactive({
  total: 0,
  pending: 0,
  processing: 0,
  reviewing: 0,
  completed: 0,
  closed: 0
})

// 操作相关
const acceptingId = ref<number | null>(null)
const reopenDialogVisible = ref(false)
const ratingDialogVisible = ref(false)
const currentTicketId = ref<number | null>(null)
const currentTicketTitle = ref('')
const reopening = ref(false)
const ratinging = ref(false)

const reopenForm = reactive({
  reason: ''
})

const ratingForm = reactive({
  score: 5,
  comment: ''
})

// 待验收的工单
const pendingAcceptTickets = computed(() => {
  return tickets.value.filter(t => t.status === 4)
})

// 待评价的工单（已结束但未评价）
const pendingRateTickets = computed(() => {
  return tickets.value.filter(t => t.status === 5 && !t.satisfaction)
})

// 筛选后的工单
const filteredTickets = computed(() => {
  if (statusFilter.value === 0) return tickets.value
  return tickets.value.filter(t => t.status === statusFilter.value)
})

onMounted(() => {
  loadTickets()
})

async function loadTickets() {
  loading.value = true
  try {
    const res = await getTickets({ page: 1, pageSize: 100 })
    tickets.value = res.data.records
    updateStats()
  } finally {
    loading.value = false
  }
}

function updateStats() {
  stats.total = tickets.value.length
  stats.pending = tickets.value.filter(t => t.status === 1).length
  stats.processing = tickets.value.filter(t => t.status === 2).length
  stats.reviewing = tickets.value.filter(t => t.status === 3).length
  stats.completed = tickets.value.filter(t => t.status === 4).length
  stats.closed = tickets.value.filter(t => t.status === 5).length
}

// 确认验收
function confirmAccept(ticketId: number) {
  const ticket = tickets.value.find(t => t.id === ticketId)
  ElMessageBox.confirm(
    `确认验收工单「${ticket?.title || ''}」？验收后工单将变为已结束状态。`,
    '确认验收',
    {
      confirmButtonText: '确认验收',
      cancelButtonText: '取消',
      type: 'success'
    }
  ).then(() => {
    handleAccept(ticketId)
  }).catch(() => {})
}

// 验收工单
async function handleAccept(ticketId: number) {
  acceptingId.value = ticketId
  try {
    await acceptTicket(ticketId)
    ElMessage.success('验收成功')
    await loadTickets()
  } finally {
    acceptingId.value = null
  }
}

// 显示返工对话框
function showReopenDialog(ticketId: number) {
  const ticket = tickets.value.find(t => t.id === ticketId)
  currentTicketId.value = ticketId
  currentTicketTitle.value = ticket?.title || ''
  reopenForm.reason = ''
  reopenDialogVisible.value = true
}

// 返工工单
async function handleReopen() {
  if (!reopenForm.reason.trim()) {
    ElMessage.warning('请填写返工的原因')
    return
  }
  if (!currentTicketId.value) return

  reopening.value = true
  try {
    await reopenTicket(currentTicketId.value, reopenForm.reason)
    ElMessage.success('已申请返工')
    reopenDialogVisible.value = false
    await loadTickets()
  } finally {
    reopening.value = false
  }
}

// 显示评价对话框
function showRatingDialog(ticketId: number) {
  const ticket = tickets.value.find(t => t.id === ticketId)
  currentTicketId.value = ticketId
  currentTicketTitle.value = ticket?.title || ''
  ratingForm.score = 5
  ratingForm.comment = ''
  ratingDialogVisible.value = true
}

// 提交评价
async function handleRate() {
  if (!currentTicketId.value) return

  ratinging.value = true
  try {
    await rateTicket(currentTicketId.value, ratingForm.score, ratingForm.comment || undefined)
    ElMessage.success('评价成功，感谢您的反馈')
    ratingDialogVisible.value = false
    await loadTickets()
  } finally {
    ratinging.value = false
  }
}

// 优先级样式
function priorityType(p: number) {
  return p === 1 ? 'danger' : p === 2 ? 'warning' : p === 3 ? 'primary' : 'info'
}

// 状态样式
function statusType(s: number) {
  return s === 1 ? 'info' : s === 2 ? 'warning' : s === 3 ? 'primary' : s === 4 ? 'success' : ''
}

// 格式化时间
function formatTime(time: string | null | undefined) {
  if (!time) return ''
  return time.substring(0, 16)
}
</script>

<style scoped>
.my-tickets-page {
  padding: 0;
}

/* 统计卡片 */
.stat-cards {
  margin-bottom: 0;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  cursor: default;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  font-family: 'Lexend', sans-serif;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 不同状态卡片颜色 */
.stat-card.total .stat-icon {
  background: linear-gradient(135deg, #E8F0FE, #D4E4FC);
  color: #2563EB;
}
.stat-card.total .stat-num {
  color: #2563EB;
}

.stat-card.pending .stat-icon {
  background: linear-gradient(135deg, #FEF3E2, #FDE6C4);
  color: #E6A23C;
}
.stat-card.pending .stat-num {
  color: #E6A23C;
}

.stat-card.processing .stat-icon {
  background: linear-gradient(135deg, #FEF0E6, #FDE0CC);
  color: #F56C6C;
}
.stat-card.processing .stat-num {
  color: #F56C6C;
}

.stat-card.reviewing .stat-icon {
  background: linear-gradient(135deg, #E8F4FD, #D0EAFA);
  color: #409EFF;
}
.stat-card.reviewing .stat-num {
  color: #409EFF;
}

.stat-card.completed .stat-icon {
  background: linear-gradient(135deg, #E8F8E8, #D4F0D4);
  color: #67C23A;
}
.stat-card.completed .stat-num {
  color: #67C23A;
}

.stat-card.closed .stat-icon {
  background: linear-gradient(135deg, #F0F0F0, #E4E4E4);
  color: #909399;
}
.stat-card.closed .stat-num {
  color: #909399;
}

/* 操作卡片 */
.action-card {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #C0C4CC;
}

.empty-state p {
  margin-top: 12px;
  font-size: 14px;
}

/* 工单列表 */
.ticket-list {
  max-height: 400px;
  overflow-y: auto;
}

.ticket-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid #F2F6FC;
  transition: background-color 0.2s;
}

.ticket-item:last-child {
  border-bottom: none;
}

.ticket-item:hover {
  background-color: #F5F7FA;
  margin: 0 -20px;
  padding: 14px 20px;
  border-radius: 8px;
}

.ticket-main {
  flex: 1;
  min-width: 0;
}

.ticket-title {
  color: #303133;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ticket-title:hover {
  color: #2563EB;
}

.ticket-meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-text {
  font-size: 12px;
  color: #909399;
}

.ticket-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
  flex-shrink: 0;
}

/* 表格链接 */
.link {
  color: #2563EB;
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}

/* 响应式 */
@media (max-width: 768px) {
  .stat-card {
    padding: 16px;
    margin-bottom: 8px;
  }

  .stat-icon {
    width: 44px;
    height: 44px;
    margin-right: 12px;
  }

  .stat-num {
    font-size: 22px;
  }

  .ticket-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .ticket-actions {
    margin-left: 0;
    margin-top: 12px;
    width: 100%;
  }

  .ticket-actions .el-button {
    flex: 1;
  }
}
</style>
