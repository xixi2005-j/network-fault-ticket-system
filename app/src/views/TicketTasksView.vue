<template>
  <div class="tasks-page">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card pending">
          <div class="stat-num">{{ pendingCount }}</div>
          <div class="stat-label">待处理</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card processing">
          <div class="stat-num">{{ processingCount }}</div>
          <div class="stat-label">处理中</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card completed">
          <div class="stat-num">{{ completedCount }}</div>
          <div class="stat-label">已完成</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 我的任务列表 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <div class="section-header">
          <span>我的任务</span>
          <el-radio-group v-model="statusFilter" size="small">
            <el-radio-button :value="0">全部</el-radio-button>
            <el-radio-button :value="2">处理中</el-radio-button>
            <el-radio-button :value="3">已完成</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-table :data="filteredTasks" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/tickets/${row.id}`" class="link">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small">{{ row.priorityName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="提交人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 2" type="success" size="small" @click="handleComplete(row.id)" :loading="completingId === row.id">完成</el-button>
            <span v-else-if="row.status === 3" class="done-text">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTickets, changeTicketStatus } from '@/api/ticket'
import { useUserStore } from '@/stores/user'
import type { TicketVO } from '@/types/api'

const userStore = useUserStore()
const loading = ref(false)
const tasks = ref<TicketVO[]>([])
const statusFilter = ref(0)
const completingId = ref<number | null>(null)

const pendingCount = computed(() => tasks.value.filter(t => t.status === 1).length)
const processingCount = computed(() => tasks.value.filter(t => t.status === 2).length)
const completedCount = computed(() => tasks.value.filter(t => t.status === 3).length)

const filteredTasks = computed(() => {
  if (statusFilter.value === 0) return tasks.value
  return tasks.value.filter(t => t.status === statusFilter.value)
})

onMounted(() => loadTasks())

async function loadTasks() {
  loading.value = true
  try {
    const res = await getTickets({ page: 1, pageSize: 100 })
    tasks.value = res.data.records.filter(t => t.assigneeId === userStore.user?.id)
  } finally {
    loading.value = false
  }
}

async function handleComplete(id: number) {
  completingId.value = id
  try {
    await changeTicketStatus(id, 3)
    ElMessage.success('已标记完成')
    await loadTasks()
  } finally {
    completingId.value = null
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
.tasks-page {
  height: 100%;
}
.stat-cards {
  margin-bottom: 0;
}
.stat-card {
  text-align: center;
}
.stat-num {
  font-size: 36px;
  font-weight: bold;
}
.stat-card.pending .stat-num {
  color: #e6a23c;
}
.stat-card.processing .stat-num {
  color: #409eff;
}
.stat-card.completed .stat-num {
  color: #67c23a;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.link {
  color: #409eff;
  text-decoration: none;
}
.link:hover {
  text-decoration: underline;
}
.done-text {
  font-size: 12px;
  color: #67c23a;
}
</style>
