<template>
  <div class="tasks-page">
    <el-row :gutter="20">
      <!-- 待处理任务 -->
      <el-col :span="12">
        <el-card shadow="hover" class="section-card">
          <template #header>
            <div class="section-header">
              <span>待处理任务</span>
              <el-tag type="warning" size="small">{{ pendingTasks.length }}</el-tag>
            </div>
          </template>
          <div v-if="pendingTasks.length === 0" class="empty-tip">暂无待处理任务</div>
          <div v-for="ticket in pendingTasks" :key="ticket.id" class="ticket-item">
            <div class="ticket-info">
              <router-link :to="`/tickets/${ticket.id}`" class="ticket-title">{{ ticket.title }}</router-link>
              <div class="ticket-meta">
                <el-tag :type="priorityType(ticket.priority)" size="small">{{ ticket.priorityName }}</el-tag>
                <span class="meta-text">{{ ticket.creatorName }} · {{ ticket.createTime }}</span>
              </div>
            </div>
            <div class="ticket-actions">
              <el-button type="primary" size="small" @click="handleAccept(ticket.id)" :loading="acceptingId === ticket.id">接单</el-button>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 处理中任务 -->
      <el-col :span="12">
        <el-card shadow="hover" class="section-card">
          <template #header>
            <div class="section-header">
              <span>处理中任务</span>
              <el-tag type="" size="small">{{ processingTasks.length }}</el-tag>
            </div>
          </template>
          <div v-if="processingTasks.length === 0" class="empty-tip">暂无处理中任务</div>
          <div v-for="ticket in processingTasks" :key="ticket.id" class="ticket-item">
            <div class="ticket-info">
              <router-link :to="`/tickets/${ticket.id}`" class="ticket-title">{{ ticket.title }}</router-link>
              <div class="ticket-meta">
                <el-tag :type="priorityType(ticket.priority)" size="small">{{ ticket.priorityName }}</el-tag>
                <span class="meta-text">{{ ticket.creatorName }} · {{ ticket.createTime }}</span>
              </div>
            </div>
            <div class="ticket-actions">
              <el-button type="success" size="small" @click="showReportDialog(ticket.id)">提交报告</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 审核中任务 -->
    <el-card shadow="hover" class="section-card" style="margin-top: 20px">
      <template #header>
        <div class="section-header">
          <span>审核中任务</span>
          <el-tag type="warning" size="small">{{ reviewingTasks.length }}</el-tag>
        </div>
      </template>
      <div v-if="reviewingTasks.length === 0" class="empty-tip">暂无审核中任务</div>
      <el-table :data="reviewingTasks" stripe size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/tickets/${row.id}`" class="ticket-title">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="priorityName" label="优先级" width="80" />
        <el-table-column prop="updateTime" label="提交时间" width="170" />
      </el-table>
    </el-card>

    <!-- 已完成任务 -->
    <el-card shadow="hover" class="section-card" style="margin-top: 20px">
      <template #header>
        <div class="section-header">
          <span>已完成任务</span>
          <el-tag type="success" size="small">{{ completedTasks.length }}</el-tag>
        </div>
      </template>
      <div v-if="completedTasks.length === 0" class="empty-tip">暂无已完成任务</div>
      <el-table :data="completedTasks" stripe size="small">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/tickets/${row.id}`" class="ticket-title">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="priorityName" label="优先级" width="80" />
        <el-table-column prop="resolveTime" label="完成时间" width="170" />
      </el-table>
    </el-card>

    <!-- 提交报告对话框 -->
    <el-dialog v-model="reportDialogVisible" title="提交完成报告" width="500px">
      <el-form :model="reportForm" label-width="100px">
        <el-form-item label="完成工作" required>
          <el-input v-model="reportForm.workDone" type="textarea" :rows="4" placeholder="请描述完成的工作内容" />
        </el-form-item>
        <el-form-item label="耗时统计">
          <el-input v-model="reportForm.timeSpent" placeholder="例如：2小时30分钟" />
        </el-form-item>
        <el-form-item label="解决方案">
          <el-input v-model="reportForm.solution" type="textarea" :rows="3" placeholder="请描述解决方案（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReport" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTickets, changeTicketStatus } from '@/api/ticket'
import { submitReport } from '@/api/report'
import type { TicketVO } from '@/types/api'

const allTasks = ref<TicketVO[]>([])
const acceptingId = ref<number | null>(null)

const pendingTasks = computed(() => allTasks.value.filter(t => t.status === 1))
const processingTasks = computed(() => allTasks.value.filter(t => t.status === 2))
const reviewingTasks = computed(() => allTasks.value.filter(t => t.status === 3))
const completedTasks = computed(() => allTasks.value.filter(t => t.status === 4 || t.status === 5))

const reportDialogVisible = ref(false)
const currentTicketId = ref<number | null>(null)
const reportForm = reactive({
  workDone: '',
  timeSpent: '',
  solution: ''
})
const submitting = ref(false)

onMounted(async () => {
  await loadTasks()
})

async function loadTasks() {
  const res = await getTickets({ page: 1, pageSize: 100 })
  allTasks.value = res.data.records
}

async function handleAccept(ticketId: number) {
  acceptingId.value = ticketId
  try {
    await changeTicketStatus(ticketId, 2)
    ElMessage.success('接单成功')
    await loadTasks()
  } finally {
    acceptingId.value = null
  }
}

function showReportDialog(ticketId: number) {
  currentTicketId.value = ticketId
  reportForm.workDone = ''
  reportForm.timeSpent = ''
  reportForm.solution = ''
  reportDialogVisible.value = true
}

async function handleSubmitReport() {
  if (!reportForm.workDone.trim()) {
    ElMessage.warning('请填写完成工作内容')
    return
  }
  if (!currentTicketId.value) return

  submitting.value = true
  try {
    await submitReport(currentTicketId.value, {
      workDone: reportForm.workDone,
      timeSpent: reportForm.timeSpent || undefined,
      solution: reportForm.solution || undefined
    })
    ElMessage.success('报告提交成功')
    reportDialogVisible.value = false
    await loadTasks()
  } finally {
    submitting.value = false
  }
}

function priorityType(p: number) {
  return p === 1 ? 'danger' : p === 2 ? 'warning' : p === 3 ? '' : 'info'
}
</script>

<style scoped>
.tasks-page {
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
