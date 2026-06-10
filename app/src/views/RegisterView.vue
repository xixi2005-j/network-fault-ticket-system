<template>
  <div class="login-container">
    <div class="container">
      <div class="heading">注册账号</div>
      <form class="form" @submit.prevent="handleRegister" novalidate>
        <div class="input-wrapper">
          <el-icon class="input-icon"><User /></el-icon>
          <input
            v-model="form.username"
            class="input has-icon"
            :class="{ touched: errors.username }"
            type="text"
            placeholder="用户名（3-50个字符）"
            @input="clearError('username')"
          />
        </div>
        <span class="validation-msg" :class="{ visible: errors.username }">{{ errorMsgs.username }}</span>
        <div class="input-wrapper">
          <el-icon class="input-icon"><Lock /></el-icon>
          <input
            v-model="form.password"
            class="input has-icon"
            :class="{ touched: errors.password }"
            type="password"
            placeholder="密码（至少6位）"
            @input="clearError('password')"
          />
        </div>
        <span class="validation-msg" :class="{ visible: errors.password }">{{ errorMsgs.password }}</span>
        <div class="input-wrapper">
          <el-icon class="input-icon"><Lock /></el-icon>
          <input
            v-model="form.confirmPassword"
            class="input has-icon"
            :class="{ touched: errors.confirmPassword }"
            type="password"
            placeholder="确认密码"
            @input="clearError('confirmPassword')"
          />
        </div>
        <span class="validation-msg" :class="{ visible: errors.confirmPassword }">{{ errorMsgs.confirmPassword }}</span>
        <div class="input-wrapper">
          <el-icon class="input-icon"><UserFilled /></el-icon>
          <input
            v-model="form.realName"
            class="input has-icon"
            type="text"
            placeholder="真实姓名（选填）"
          />
        </div>
        <button class="login-button" type="submit" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      <span class="agreement">已有账号？<router-link to="/login">立即登录</router-link></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: ''
})

const errors = reactive({
  username: false,
  password: false,
  confirmPassword: false
})

const errorMsgs = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

function clearError(field: 'username' | 'password' | 'confirmPassword') {
  errors[field] = false
  errorMsgs[field] = ''
}

async function handleRegister() {
  let hasError = false

  if (!form.username.trim() || form.username.length < 3) {
    errors.username = true
    errorMsgs.username = '用户名至少3个字符'
    hasError = true
  }

  if (!form.password || form.password.length < 6) {
    errors.password = true
    errorMsgs.password = '密码至少6位'
    hasError = true
  }

  if (form.password !== form.confirmPassword) {
    errors.confirmPassword = true
    errorMsgs.confirmPassword = '两次密码不一致'
    hasError = true
  }

  if (hasError) return

  loading.value = true
  try {
    await register({
      username: form.username,
      password: form.password,
      realName: form.realName || undefined
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
