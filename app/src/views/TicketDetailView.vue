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
        <el-descriptions-item label="分类">
          <el-tag>{{ ticket.categoryName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="priorityType(ticket.priority)">{{ ticket.priorityName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(ticket.status)">{{ ticket.statusName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ ticket.creatorName }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ ticket.assigneeName || '未指派' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ ticket.createTime }}</el-descriptions-item>
        <el-descriptions-item label="解决时间" :span="2">{{ ticket.resolveTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">
          <div style="white-space: pre-wrap; min-height: 60px">{{ ticket.description || '无' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 评论区 -->
    <el-card shadow="never" style="margin-top: 16px">
      <template #header>评论记录</template>
      <div v-if="comments.length === 0" style="color: #909399; text-align: center; padding: 20px">暂无评论</div>
      <div v-for="item in comments" :key="item.id" class="comment-item">
        <div class="comment-header">
          <span class="comment-user">{{ item.realName || item.username }}</span>
          <span class="comment-time">{{ item.createTime }}</span>
        </div>
        <div class="comment-content">{{ item.content }}</div>
      </div>

      <el-divider />
      <el-input v-model="newComment" type="textarea" :rows="3" placeholder="输入评论内容..." />
      <el-button type="primary" style="margin-top: 10px" @click="handleAddComment" :loading="commentLoading">发送评论</el-button>
    </el-card>

    <!-- 完成报告卡片（如果有） -->
    <el-card v-if="report" shadow="never" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>完成报告</span>
          <el-tag :type="report.status === 1 ? 'warning' : report.status === 2 ? 'success' : 'danger'" size="small">
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

    <!-- 用户操作卡片 -->
    <el-card v-if="showUserActions" shadow="never" style="margin-top: 16px">
      <template #header>操作</template>
      <div class="user-actions">
        <!-- 已完成状态：用户可以验收或重新打开 -->
        <template v-if="ticket.status === 4 && isCreator">
          <el-button type="success" @click="handleAccept" :loading="acting">确认验收</el-button>
          <el-button type="warning" @click="showReopenDialog = true">重新打开</el-button>
        </template>
        <!-- 已结束状态：用户可以评价或重新打开 -->
        <template v-if="ticket.status === 5 && isCreator">
          <el-button v-if="!ticket.satisfaction" type="primary" @click="showRatingDialog = true">评价工单</el-button>
          <el-button type="warning" @click="showReopenDialog = true">重新打开</el-button>
        </template>
      </div>
    </el-card>

    <!-- 重新打开对话框 -->
    <el-dialog v-model="showReopenDialog" title="重新打开工单" width="400px">
      <el-form label-width="80px">
        <el-form-item label="原因" required>
          <el-input v-model="reopenReason" type="textarea" :rows="3" placeholder="请说明重新打开的原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReopenDialog = false">取消</el-button>
        <el-button type="warning" @click="handleReopen" :loading="acting">确认</el-button>
      </template>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog v-model="showRatingDialog" title="评价工单" width="400px">
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="ratingScore" show-score allow-half />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="ratingComment" type="textarea" :rows="3" placeholder="请输入评语（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRatingDialog = false">取消</el-button>
        <el-button type="primary" @click="handleRate" :loading="acting">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTicketDetail, acceptTicket, rateTicket, reopenTicket } from '@/api/ticket'
import { getReportByTicketId } from '@/api/report'
import { getComments, addComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import type { TicketVO, CommentVO, CompletionReportVO } from '@/types/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const commentLoading = ref(false)
const ticketId = Number(route.params.id)

const ticket = ref<TicketVO>({} as TicketVO)
const report = ref<CompletionReportVO | null>(null)
const comments = ref<CommentVO[]>([])
const newComment = ref('')

const acting = ref(false)
const showReopenDialog = ref(false)
const reopenReason = ref('')
const showRatingDialog = ref(false)
const ratingScore = ref(5)
const ratingComment = ref('')

const isCreator = computed(() => ticket.value && userStore.user?.id === ticket.value.creatorId)
const showUserActions = computed(() => {
  if (!ticket.value || !isCreator.value) return false
  return ticket.value.status === 4 || ticket.value.status === 5
})

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

async function handleAccept() {
  acting.value = true
  try {
    await acceptTicket(ticketId)
    ElMessage.success('验收成功')
    await loadTicket()
  } finally {
    acting.value = false
  }
}

async function handleReopen() {
  if (!reopenReason.value.trim()) {
    ElMessage.warning('请填写重新打开的原因')
    return
  }
  acting.value = true
  try {
    await reopenTicket(ticketId, reopenReason.value)
    ElMessage.success('工单已重新打开')
    showReopenDialog.value = false
    reopenReason.value = ''
    await loadTicket()
    await loadReport()
  } finally {
    acting.value = false
  }
}

async function handleRate() {
  acting.value = true
  try {
    await rateTicket(ticketId, ratingScore.value, ratingComment.value || undefined)
    ElMessage.success('评价成功')
    showRatingDialog.value = false
    await loadTicket()
  } finally {
    acting.value = false
  }
}

function priorityType(p: number) {
  return p === 1 ? 'danger' : p === 2 ? 'warning' : p === 3 ? '' : 'info'
}
function statusType(s: number) {
  return s === 1 ? 'warning' : s === 2 ? '' : s === 3 ? 'warning' : s === 4 ? 'success' : 'info'
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.comment-item:last-of-type {
  border-bottom: none;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.comment-user {
  font-weight: 500;
  color: #303133;
}
.comment-time {
  font-size: 12px;
  color: #909399;
}
.comment-content {
  color: #606266;
  line-height: 1.6;
}
.user-actions {
  display: flex;
  gap: 12px;
}
</style>
