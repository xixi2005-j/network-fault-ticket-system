<template>
  <div v-loading="loading">
    <!-- 工单信息 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ ticket.title }}</span>
          <el-button type="primary" link @click="$router.push('/tickets')">← 返回列表</el-button>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单编号">
          <span class="ticket-no">NO.{{ String(ticket.id).padStart(3, '0') }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(ticket.status)" effect="dark">{{ ticket.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="分类">
          <el-tag effect="plain">{{ ticket.categoryName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="priorityType(ticket.priority)" effect="plain">{{ ticket.priorityName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ ticket.creatorName }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ ticket.assigneeName || '未指派' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ ticket.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ ticket.updateTime }}</el-descriptions-item>
        <el-descriptions-item v-if="ticket.resolveTime" label="解决时间">{{ ticket.resolveTime }}</el-descriptions-item>
        <el-descriptions-item v-if="ticket.closeTime" label="关闭时间">{{ ticket.closeTime }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">
          <div style="white-space: pre-wrap; min-height: 60px">{{ ticket.description || '无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="ticket.satisfaction" label="满意度评价" :span="2">
          <div class="satisfaction-display">
            <el-rate :model-value="ticket.satisfaction" disabled show-score />
            <span v-if="ticket.satisfactionComment" class="satisfaction-comment">{{ ticket.satisfactionComment }}</span>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 完成报告卡片（如果有） -->
    <el-card v-if="report" shadow="never" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>完成报告</span>
          <el-tag :type="report.status === 1 ? 'warning' : report.status === 2 ? 'success' : 'danger'" size="small" effect="dark">
            {{ report.statusText }}
          </el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="报告人">{{ report.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ report.createTime }}</el-descriptions-item>
        <el-descriptions-item label="完成工作" :span="2">{{ report.workDone }}</el-descriptions-item>
        <el-descriptions-item label="耗时统计" :span="2">{{ report.timeSpent || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="解决方案" :span="2">{{ report.solution || '未填写' }}</el-descriptions-item>
        <el-descriptions-item v-if="report.rejectReason" label="驳回原因" :span="2">
          <span style="color: #f56c6c">{{ report.rejectReason }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 评论区 -->
    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>评论记录</span>
          <el-tag type="info" size="small">{{ comments.length }} 条</el-tag>
        </div>
      </template>
      <div v-if="comments.length === 0" class="empty-comment">
        <el-icon :size="48" color="#C0C4CC"><ChatLineSquare /></el-icon>
        <p>暂无评论</p>
      </div>
      <div v-else class="comment-list">
        <div v-for="item in comments" :key="item.id" class="comment-item">
          <div class="comment-header">
            <div class="comment-user-info">
              <el-avatar :size="32" class="comment-avatar">{{ (item.realName || item.username || '').charAt(0) }}</el-avatar>
              <span class="comment-user">{{ item.realName || item.username }}</span>
            </div>
            <span class="comment-time">{{ item.createTime }}</span>
          </div>
          <div class="comment-content">{{ item.content }}</div>
        </div>
      </div>

      <el-divider />
      <div class="comment-input">
        <el-input v-model="newComment" type="textarea" :rows="3" placeholder="输入评论内容..." maxlength="500" show-word-limit />
        <el-button type="primary" style="margin-top: 10px" @click="handleAddComment" :loading="commentLoading">
          <el-icon><Promotion /></el-icon>发送评论
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTicketDetail } from '@/api/ticket'
import { getReportByTicketId } from '@/api/report'
import { getComments, addComment } from '@/api/comment'
import type { TicketVO, CommentVO, CompletionReportVO } from '@/types/api'

const route = useRoute()
const loading = ref(false)
const commentLoading = ref(false)
const ticketId = Number(route.params.id)

const ticket = ref<TicketVO>({} as TicketVO)
const report = ref<CompletionReportVO | null>(null)
const comments = ref<CommentVO[]>([])
const newComment = ref('')

onMounted(async () => {
  await loadTicket()
  await loadReport()
  await loadComments()
})

async function loadTicket() {
  loading.value = true
  try {
    const res = await getTicketDetail(ticketId)
    ticket.value = res.data
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  const res = await getComments(ticketId)
  comments.value = res.data
}

async function handleAddComment() {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  commentLoading.value = true
  try {
    await addComment(ticketId, newComment.value)
    newComment.value = ''
    await loadComments()
    ElMessage.success('评论成功')
  } finally {
    commentLoading.value = false
  }
}

async function loadReport() {
  try {
    const res = await getReportByTicketId(ticketId)
    report.value = res.data
  } catch (e) {
    // 报告可能不存在
  }
}

function priorityType(p: number) {
  return p === 1 ? 'danger' : p === 2 ? 'warning' : p === 3 ? 'primary' : 'info'
}

function statusType(s: number) {
  return s === 1 ? 'info' : s === 2 ? 'warning' : s === 3 ? 'primary' : s === 4 ? 'success' : ''
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ticket-no {
  font-family: 'Lexend', monospace;
  font-weight: 600;
  color: #2563EB;
}

.satisfaction-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.satisfaction-comment {
  color: #606266;
  font-style: italic;
}

/* 空评论 */
.empty-comment {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #C0C4CC;
}

.empty-comment p {
  margin-top: 12px;
  font-size: 14px;
}

/* 评论列表 */
.comment-list {
  max-height: 500px;
  overflow-y: auto;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #F2F6FC;
}

.comment-item:last-of-type {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.comment-user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-avatar {
  background: linear-gradient(135deg, #2563EB, #3B82F6);
  color: #fff;
  font-size: 14px;
}

.comment-user {
  font-weight: 600;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  color: #606266;
  line-height: 1.8;
  padding-left: 42px;
}

/* 评论输入 */
.comment-input {
  display: flex;
  flex-direction: column;
}

.comment-input .el-button {
  align-self: flex-end;
}
</style>
