import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserVO } from '@/types/api'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<UserVO | null>(
    JSON.parse(localStorage.getItem('user') || 'null')
  )

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 1)
  const isOps = computed(() => user.value?.role === 2)
  const isNormalUser = computed(() => user.value?.role === 3)
  const roleName = computed(() => {
    switch (user.value?.role) {
      case 1: return '管理员'
      case 2: return '运维人员'
      case 3: return '普通用户'
      default: return ''
    }
  })

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('token', t)
  }

  function setUser(u: UserVO) {
    user.value = u
    localStorage.setItem('user', JSON.stringify(u))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, user, isLoggedIn, isAdmin, isOps, isNormalUser, roleName, setToken, setUser, logout }
})
