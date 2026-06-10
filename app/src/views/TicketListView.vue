<template>
  <div class="page-container">
    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="请输入" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="请选择" clearable>
            <el-option label="待处理" :value="1" />
            <el-option label="处理中" :value="2" />
            <el-option label="审核中" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已结束" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="query.priority" placeholder="请选择" clearable>
            <el-option label="紧急" :value="1" />
            <el-option label="高" :value="2" />
            <el-option label="中" :value="3" />
            <el-option label="低" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="请选择" clearable>
            <el-option label="网络故障" :value="1" />
            <el-option label="设备故障" :value="2" />
            <el-option label="服务异常" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item class="btn-group">
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="$router.push('/tickets/create')">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工单表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
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
            <el-tag :type="priorityType(row.priority)" size="small">{{ row.priorityName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="100" />
        <el-table-column prop="assigneeName" label="处理人" width="100">
          <template #default="{ row }">{{ row.assigneeName || '未指派' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/tickets/${row.id}`)">
              <el-icon :size="18"><View /></el-icon>
            </el-button>
            <el-button link type="primary" :disabled="row.status >= 4" @click="$router.push(`/tickets/${row.id}/edit`)">
              <el-icon :size="18"><EditPen /></el-icon>
            </el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)" :disabled="row.status >= 4">
              <template #reference>
                <el-button link type="danger" :disabled="row.status >= 4">
                  <el-icon :size="18"><Delete /></el-icon>
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页区域 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTickets, deleteTicket } from '@/api/ticket'
import type { TicketVO } from '@/types/api'

const loading = ref(false)
const tableData = ref<TicketVO[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: undefined as number | undefined,
  priority: undefined as number | undefined,
  category: undefined as number | undefined
})

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const res = await getTickets(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.keyword = ''
  query.status = undefined
  query.priority = undefined
  query.category = undefined
  query.page = 1
  loadData()
}

async function handleDelete(id: number) {
  await deleteTicket(id)
  ElMessage.success('删除成功')
  loadData()
}

function priorityType(p: number) {
  return p === 1 ? 'danger' : p === 2 ? 'warning' : p === 3 ? '' : 'info'
}

function statusType(s: number) {
  return s === 1 ? 'warning' : s === 2 ? '' : s === 3 ? 'warning' : s === 4 ? 'success' : 'info'
}
</script>

<style scoped>
.page-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.table-card {
  margin-top: 16px;
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-card :deep(.el-select) {
  width: 140px;
}

.filter-card :deep(.el-select .el-input__inner) {
  color: #333;
}

.btn-group {
  margin-left: auto;
}

.btn-group :deep(.el-button) {
  margin-left: 20px;
}

.btn-group :deep(.el-button:first-child) {
  margin-left: 0;
}

.pagination-wrapper {
  background-color: #f0f2f5;
  padding: 12px 24px;
  border-top: 1px solid #dcdfe6;
  display: flex;
  justify-content: flex-end;
  border-radius: 8px;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.08);
  margin-top: 1px;
}
</style>
