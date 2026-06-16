<template>
  <el-card shadow="never" class="table-card">
    <template #header>用户管理</template>
    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : row.role === 2 ? 'warning' : 'info'" size="small">
            {{ row.role === 1 ? '管理员' : row.role === 2 ? '运维人员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-select v-model="row.role" size="small" style="width: 100px; margin-right: 8px" @change="handleRoleChange(row)">
            <el-option label="管理员" :value="1" />
            <el-option label="运维人员" :value="2" />
            <el-option label="普通用户" :value="3" />
          </el-select>
          <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUsers, updateUserRole, updateUserStatus } from '@/api/user'
import { notifyRefresh, onRefresh } from '@/utils/sync'
import type { UserVO } from '@/types/api'

const loading = ref(false)
const users = ref<UserVO[]>([])

let cleanup: (() => void) | undefined

onMounted(() => {
  loadData()
  cleanup = onRefresh(() => loadData())
})

onUnmounted(() => { cleanup?.() })

async function loadData() {
  loading.value = true
  try {
    const res = await getUsers()
    users.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleRoleChange(row: UserVO) {
  await updateUserRole(row.id, row.role)
  ElMessage.success('角色修改成功')
  notifyRefresh()
}

async function handleStatusChange(row: UserVO) {
  await updateUserStatus(row.id, row.status)
  ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
  notifyRefresh()
}
</script>
