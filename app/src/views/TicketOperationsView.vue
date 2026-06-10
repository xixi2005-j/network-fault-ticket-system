<template>
  <div class="operations-page">
    <el-row :gutter="20">
      <!-- 待指派工单 -->
      <el-col :span="12">
        <el-card shadow="hover" class="section-card">
          <template #header>
            <div class="section-header">
              <span>待指派工单</span>
              <el-tag type="warning" size="small">{{ pendingTickets.length }}</el-tag>
            </div>
          </template>
          <div v-if="pendingTickets.length === 0" class="empty-tip">暂无待指派工单</div>
          <div v-for="ticket in pendingTickets" :key="ticket.id" class="ticket-item">
            <div class="ticket-info">
              <router-link :to="`/tickets/${ticket.id}`" class="ticket-title">{{ ticket.title }}</router-link>
              <div class="ticket-meta">
                <el-tag :type="priorityType(ticket.priority)" size="small">{{ ticket.priorityName }}</el-tag>
                <span class="meta-text">{{ ticket.creatorName }} · {{ ticket.createTime }}</span>
              </div>
            </div>
            <div class="ticket-actions">
              <el-select v-model="assignMap[ticket.id]" placeholder="选择运维" size="small" style="width: 110px">
                <el-option v-for="u in opsUsers" :key="u.id" :label="u.realName || u.username" :value="u.id" />
              </el-select>
              <el-button type="primary" size="small" @click="handleAssign(ticket.id)" :loading="assigningId === ticket.id">指派</el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 处理中工单 -->
      <el-col :span="12">
        <el-card shadow="hover" class="section-card">
          <template #header>
            <div class="section-header">
              <span>处理中工单</span>
              <el-tag type="" size="small">{{ processingTickets.length }}</el-tag>
            </div>
          </template>
          <div v-if="processingTickets.length === 0" class="empty-tip">暂无处理中工单</div>
          <div v-for="ticket in processingTickets" :key="ticket.id" class="ticket-item">
            <div class="ticket-info">
              <router-link :to="`/tickets/${ticket.id}`" class="ticket-title">{{ ticket.title }}</router-link>
              <div class="ticket-meta">
                <el-tag :type="priorityType(ticket.priority)" size="small">{{ ticket.priorityName }}</el-tag>
                <span class="meta-text">处理人：{{ ticket.assigneeName || '未指派' }}</span>
              </div>
            </div>
            <div class="ticket-actions">
              <el-button type="success" size="small" @click="handleStatus(ticket.id, 3)" :loading="statusId === ticket.id">标记完成</el-button>
              <el-button type="info" size="small" @click="handleStatus(ticket.id, 4)" :loading="statusId === ticket.id">关闭</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 已完成工单 -->
    <el-card shadow="hover" class="section-card" style="margin-top: 20px">
      <template #header>
        <div class="section-header">
          <span>已完成待关闭</span>
          <el-tag type="success" size="small">{{ completedTickets.length }}</el-tag>
        </div>
      </template>
      <div v-if="completedTickets.length === 0" class="empty-tip">暂无已完成待关闭的工单</div>
      <el-table :data="completedTickets" stripe size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/tickets/${row.id}`" class="ticket-title">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="assigneeName" label="处理人" width="100" />
        <el-table-column prop="resolveTime" label="完成时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="handleStatus(row.id, 4)" :loading="statusId === row.id">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTickets, assignTicket, changeTicketStatus } from '@/api/ticket'
import { getUsers } from '@/api/user'
import type { TicketVO, UserVO } from '@/types/api'

const allTickets = ref<TicketVO[]>([])
const opsUsers = ref<UserVO[]>([])
const assignMap = reactive<Record<number, number>>({})
const assigningId = ref<number | null>(null)
const statusId = ref<number | null>(null)

const pendingTickets = computed(() => allTickets.value.filter(t => t.status === 1))
const processingTickets = computed(() => allTickets.value.filter(t => t.status === 2))
const completedTickets = computed(() => allTickets.value.filter(t => t.status === 3))

onMounted(async () => {
  await loadTickets()
  await loadOpsUsers()
})

async function loadTickets() {
  const res = await getTickets({ page: 1, pageSize: 100 })
  allTickets.value = res.data.records
}

async function loadOpsUsers() {
  const res = await getUsers()
  opsUsers.value = res.data.filter(u => u.role === 2)
}

async function handleAssign(ticketId: number) {
  const assigneeId = assignMap[ticketId]
  if (!assigneeId) {
    ElMessage.warning('请先选择运维人员')
    return
  }
  assigningId.value = ticketId
  try {
    await assignTicket(ticketId, assigneeId)
    ElMessage.success('指派成功')
    await loadTickets()
  } finally {
    assigningId.value = null
  }
}

async function handleStatus(ticketId: number, status: number) {
  statusId.value = ticketId
  try {
    await changeTicketStatus(ticketId, status)
    ElMessage.success('状态变更成功')
    await loadTickets()
  } finally {
    statusId.value = null
  }
}

function priorityType(p: number) {
  return p === 1 ? 'danger' : p === 2 ? 'warning' : p === 3 ? '' : 'info'
}
</script>

<style scoped>
.operations-page {
  height: 100%;
}
.section-card {
  height: 100%;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.empty-tip {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}
.ticket-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.ticket-item:last-child {
  border-bottom: none;
}
.ticket-info {
  flex: 1;
  min-width: 0;
}
.ticket-title {
  color: #303133;
  text-decoration: none;
  font-weight: 500;
}
.ticket-title:hover {
  color: #409eff;
}
.ticket-meta {
  margin-top: 6px;
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
  margin-left: 12px;
  flex-shrink: 0;
}
</style>
