<template>
  <div class="list-page">
    <!-- 筛选区域 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="搜索标题" clearable @keyup.enter="loadTickets" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable>
            <el-option label="待处理" :value="1" />
            <el-option label="处理中" :value="2" />
            <el-option label="审核中" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已结束" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="query.priority" placeholder="全部" clearable>
            <el-option label="紧急" :value="1" />
            <el-option label="高" :value="2" />
            <el-option label="中" :value="3" />
            <el-option label="低" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="全部" clearable>
            <el-option label="网络故障" :value="1" />
            <el-option label="设备故障" :value="2" />
            <el-option label="服务异常" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTickets">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工单列表 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <el-table :data="tickets" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <router-link :to="`/tickets/${row.id}`" class="ticket-link">{{ row.title }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small">{{ row.priorityName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="100" />
        <el-table-column prop="assigneeName" label="处理人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="router.push(`/tickets/${row.id}`)">查看</el-button>
            <el-button v-if="canEdit(row)" type="warning" link @click="router.push(`/tickets/${row.id}/edit`)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadTickets"
          @current-change="loadTickets"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTickets } from '@/api/ticket'
import { useUserStore } from '@/stores/user'
import type { TicketVO, TicketPageQuery } from '@/types/api'

const router = useRouter()
const userStore = useUserStore()

const tickets = ref<TicketVO[]>([])
const total = ref(0)
const query = reactive<TicketPageQuery>({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: undefined,
  priority: undefined,
  category: undefined
})

onMounted(() => {
  loadTickets()
})

async function loadTickets() {
  const res = await getTickets(query)
  tickets.value = res.data.records
  total.value = res.data.total
}

function resetQuery() {
  query.keyword = ''
  query.status = undefined
  query.priority = undefined
  query.category = undefined
  query.page = 1
  loadTickets()
}

function canEdit(row: TicketVO) {
  return (userStore.user?.id === row.creatorId || userStore.user?.role === 1) && row.status === 1
}

function statusType(s: number) {
  return s === 1 ? 'info' : s === 2 ? 'warning' : s === 3 ? 'warning' : s === 4 ? 'success' : s === 5 ? '' : 'info'
}

function priorityType(p: number) {
  return p === 1 ? 'danger' : p === 2 ? 'warning' : p === 3 ? '' : 'info'
}
</script>

<style scoped>
.list-page {
  height: 100%;
}
.filter-card {
  margin-bottom: 0;
}
.ticket-link {
  color: #409eff;
  text-decoration: none;
}
.ticket-link:hover {
  text-decoration: underline;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
