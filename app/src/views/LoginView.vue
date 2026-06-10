<template>
  <div class="login-container">
    <div class="container">
      <div class="heading">故障工单系统</div>
      <form class="form" @submit.prevent="handleLogin" novalidate>
        <div class="input-wrapper">
          <el-icon class="input-icon"><User /></el-icon>
          <input
            v-model="form.username"
            class="input has-icon"
            :class="{ touched: errors.username }"
            type="text"
            placeholder="用户名"
            @input="clearError('username')"
          />
        </div>
        <span class="validation-msg" :class="{ visible: errors.username }">请输入用户名</span>
        <div class="input-wrapper">
          <el-icon class="input-icon"><Lock /></el-icon>
          <input
            v-model="form.password"
            class="input has-icon"
            :class="{ touched: errors.password }"
            type="password"
            placeholder="密码"
            @input="clearError('password')"
            @keyup.enter="handleLogin"
          />
        </div>
        <span class="validation-msg" :class="{ visible: errors.password }">请输入密码</span>
        <button class="login-button" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      <span class="agreement">还没有账号？<router-link to="/register">立即注册</router-link></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const errors = reactive({
  username: false,
  password: false
})

function clearError(field: 'username' | 'password') {
  errors[field] = false
}

async function handleLogin() {
  // 验证
  errors.username = !form.username.trim()
  errors.password = !form.password.trim()

  if (errors.username || errors.password) return

  loading.value = true
  try {
    const res = await login(form)
    userStore.setToken(res.data.token)
    userStore.setUser(res.data.user)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #e8e8e8;
  margin: 0;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.container {
  transform: scale(1.14);
  width: 420px;
  background: #F8F9FD;
  background: linear-gradient(0deg, rgb(255, 255, 255) 0%, rgb(244, 247, 251) 100%);
  border-radius: 40px;
  padding: 25px 35px;
  border: 5px solid rgb(255, 255, 255);
  box-shadow: rgba(133, 189, 215, 0.8784313725) 0px 30px 30px -20px;
  margin: 20px;
}

.heading {
  text-align: center;
  font-weight: 900;
  font-size: 30px;
  color: rgb(16, 137, 211);
}

.form {
  margin-top: 20px;
}

.form .input {
  width: 100%;
  background: white;
  border: none;
  padding: 15px 20px;
  border-radius: 20px;
  margin-top: 15px;
  box-shadow: #cff0ff 0px 10px 10px -5px;
  border-inline: 2px solid transparent;
  box-sizing: border-box;
}

.form .input::placeholder {
  color: rgb(170, 170, 170);
}

.form .input:focus {
  outline: none;
  border-inline: 2px solid #12B1D1;
}

.form .login-button {
  display: block;
  width: 100%;
  font-weight: bold;
  font-size: 16px;
  background: linear-gradient(45deg, rgb(16, 137, 211) 0%, rgb(18, 177, 209) 100%);
  color: white;
  padding-block: 15px;
  margin: 20px auto;
  border-radius: 20px;
  box-shadow: rgba(133, 189, 215, 0.8784313725) 0px 20px 10px -15px;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}

.form .login-button:hover {
  transform: scale(1.03);
  box-shadow: rgba(133, 189, 215, 0.8784313725) 0px 23px 10px -20px;
}

.form .login-button:active {
  transform: scale(0.95);
  box-shadow: rgba(133, 189, 215, 0.8784313725) 0px 15px 10px -10px;
}

.form .login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.agreement {
  display: block;
  text-align: center;
  margin-top: 15px;
  font-size: 13px;
  color: rgb(170, 170, 170);
}

.agreement a {
  text-decoration: none;
  color: #0099ff;
  font-size: 13px;
}

.form .input.touched {
  border-inline: 2px solid #ff4444;
}

.validation-msg {
  visibility: hidden;
  height: 16px;
  font-size: 11px;
  color: #ff4444;
  margin-top: 5px;
  margin-left: 10px;
}

.validation-msg.visible {
  visibility: visible;
}

.input-wrapper {
  position: relative;
  margin-top: 15px;
}

.input-wrapper .input {
  margin-top: 0;
}

.input-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: rgb(16, 137, 211);
  font-size: 16px;
  z-index: 1;
}

.input.has-icon {
  padding-left: 42px;
}
</style>
