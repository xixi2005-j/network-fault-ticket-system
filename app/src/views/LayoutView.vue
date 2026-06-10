<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">故障工单系统</div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item v-if="userStore.isAdmin" index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>统计面板</span>
        </el-menu-item>
        <el-menu-item v-if="userStore.isNormalUser" index="/my-tickets">
          <el-icon><FolderOpened /></el-icon>
          <span>我的工单</span>
        </el-menu-item>
        <el-menu-item index="/tickets">
          <el-icon><Tickets /></el-icon>
          <span>工单列表</span>
        </el-menu-item>
        <el-menu-item index="/tickets/create">
          <el-icon><Plus /></el-icon>
          <span>创建工单</span>
        </el-menu-item>
        <el-menu-item v-if="userStore.isAdmin" index="/admin/operations">
          <el-icon><Setting /></el-icon>
          <span>工单操作</span>
        </el-menu-item>
        <el-menu-item v-if="userStore.isOps" index="/ops/tasks">
          <el-icon><List /></el-icon>
          <span>工单任务</span>
        </el-menu-item>
        <el-menu-item v-if="userStore.isAdmin" index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="breadcrumb">{{ currentTitle }}</div>
        <div class="user-info">
          <el-dropdown @command="handleCommand">
            <span class="dropdown-link">
              {{ userStore.user?.realName || userStore.user?.username }}
              <el-tag size="small" :type="userStore.isAdmin ? 'danger' : userStore.isOps ? 'warning' : 'info'" style="margin-left: 8px">
                {{ userStore.roleName }}
              </el-tag>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentTitle = computed(() => {
  const map: Record<string, string> = {
    '/dashboard': '统计面板',
    '/my-tickets': '我的工单',
    '/tickets': '工单列表',
    '/tickets/create': '创建工单',
    '/admin/operations': '工单操作',
    '/ops/tasks': '工单任务',
    '/admin/users': '用户管理'
  }
  // 匹配路由
  for (const [path, title] of Object.entries(map)) {
    if (route.path.startsWith(path)) return title
  }
  if (route.path.match(/^\/tickets\/\d+$/)) return '工单详情'
  if (route.path.match(/^\/tickets\/\d+\/edit$/)) return '编辑工单'
  return '工单系统'
})

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}
.layout > .el-container {
  margin-left: 220px;
}
.aside {
  background: #304156;
  overflow-y: auto;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #3d4f65;
}
.menu {
  border-right: none;
  background: #304156;
}
.menu .el-menu-item {
  color: #bfcbd9;
}
.menu .el-menu-item:hover,
.menu .el-menu-item.is-active {
  background: #263445;
  color: #409eff;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}
.breadcrumb {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}
.dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
  outline: none;
}

.dropdown-link:focus-visible {
  outline: none;
}
.main {
  background: #f0f2f5;
  padding: 20px 20px 0;
  overflow-y: auto;
  height: 0;
}
</style>
