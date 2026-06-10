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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTicketDetail } from '@/api/ticket'
import { getComments, addComment } from '@/api/comment'
import type { TicketVO, CommentVO } from '@/types/api'

const route = useRoute()
const loading = ref(false)
const commentLoading = ref(false)
const ticketId = Number(route.params.id)

const ticket = ref<TicketVO>({} as TicketVO)
const comments = ref<CommentVO[]>([])
const newComment = ref('')

onMounted(async () => {
  await loadTicket()
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
