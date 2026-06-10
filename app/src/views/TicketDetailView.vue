<template>
  <div v-loading="loading">
    <el-row :gutter="20">
      <!-- 工单信息 -->
      <el-col :span="16">
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
      </el-col>

      <!-- 操作区 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>操作</template>
          <div class="actions">
            <!-- 指派（管理员可见，待处理状态） -->
            <template v-if="userStore.isAdmin && ticket.status === 1">
              <p class="action-label">指派给：</p>
              <el-select v-model="assigneeId" placeholder="选择运维人员" style="width: 100%">
                <el-option v-for="u in opsUsers" :key="u.id" :label="u.realName || u.username" :value="u.id" />
              </el-select>
              <el-button type="primary" style="width: 100%; margin-top: 10px" @click="handleAssign" :loading="actionLoading">指派工单</el-button>
              <el-divider />
            </template>

            <!-- 状态变更 -->
            <template v-if="canChangeStatus">
              <el-button v-if="canStartProcess" type="warning" style="width: 100%" @click="handleStatus(2)" :loading="actionLoading">开始处理</el-button>
              <el-button v-if="canComplete" type="success" style="width: 100%" @click="handleStatus(3)" :loading="actionLoading">标记完成</el-button>
              <el-button v-if="canClose" type="info" style="width: 100%" @click="handleStatus(4)" :loading="actionLoading">关闭工单</el-button>
            </template>

            <el-button style="width: 100%; margin-top: 10px" @click="$router.push(`/tickets/${ticket.id}/edit`)">编辑工单</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTicketDetail, changeTicketStatus, assignTicket } from '@/api/ticket'
import { getComments, addComment } from '@/api/comment'
import { getUsers } from '@/api/user'
import { useUserStore } from '@/stores/user'
import type { TicketVO, CommentVO, UserVO } from '@/types/api'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const actionLoading = ref(false)
const commentLoading = ref(false)
const ticketId = Number(route.params.id)

const ticket = ref<TicketVO>({} as TicketVO)
const comments = ref<CommentVO[]>([])
const opsUsers = ref<UserVO[]>([])
const assigneeId = ref<number>()
const newComment = ref('')

// 状态变更权限
const canChangeStatus = computed(() => {
  const t = ticket.value
  if (t.status === 4) return false
  if (userStore.isAdmin) return true
  if (userStore.isOps && t.assigneeId === userStore.user?.id && t.status === 2) return true
  return false
})

const canStartProcess = computed(() => ticket.value.status === 1 && (userStore.isAdmin || userStore.isOps))
const canComplete = computed(() => ticket.value.status === 2 && (userStore.isAdmin || (userStore.isOps && ticket.value.assigneeId === userStore.user?.id)))
const canClose = computed(() => {
  const t = ticket.value
  if (userStore.isAdmin) return t.status === 1 || t.status === 2 || t.status === 3
  if (t.creatorId === userStore.user?.id) return t.status === 1 || t.status === 3
  return false
})

onMounted(async () => {
  await loadTicket()
  await loadComments()
  if (userStore.isAdmin) {
    await loadOpsUsers()
  }
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

async function loadOpsUsers() {
  const res = await getUsers()
  opsUsers.value = res.data.filter(u => u.role === 2)
}

async function handleStatus(status: number) {
  actionLoading.value = true
  try {
    await changeTicketStatus(ticketId, status)
    ElMessage.success('状态变更成功')
    await loadTicket()
  } finally {
    actionLoading.value = false
  }
}

async function handleAssign() {
  if (!assigneeId.value) {
    ElMessage.warning('请选择处理人')
    return
  }
  actionLoading.value = true
  try {
    await assignTicket(ticketId, assigneeId.value)
    ElMessage.success('指派成功')
    await loadTicket()
  } finally {
    actionLoading.value = false
  }
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

function priorityType(p: number) {
  return p === 1 ? 'danger' : p === 2 ? 'warning' : p === 3 ? '' : 'info'
}
function statusType(s: number) {
  return s === 1 ? 'warning' : s === 2 ? '' : s === 3 ? 'success' : 'info'
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.action-label {
  margin: 0;
  font-size: 14px;
  color: #606266;
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
</style>
