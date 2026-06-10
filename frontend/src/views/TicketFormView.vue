<template>
  <el-card shadow="never">
    <template #header>{{ isEdit ? '编辑工单' : '创建工单' }}</template>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" style="max-width: 600px">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入工单标题" />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="form.category" placeholder="请选择分类">
          <el-option label="网络故障" :value="1" />
          <el-option label="设备故障" :value="2" />
          <el-option label="服务异常" :value="3" />
          <el-option label="其他" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="form.priority" placeholder="请选择优先级">
          <el-option label="紧急" :value="1" />
          <el-option label="高" :value="2" />
          <el-option label="中" :value="3" />
          <el-option label="低" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="问题描述">
        <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请详细描述问题..." />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleSubmit">{{ isEdit ? '保存修改' : '提交工单' }}</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { createTicket, getTicketDetail, updateTicket } from '@/api/ticket'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  category: undefined as number | undefined,
  priority: 3,
  description: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

onMounted(async () => {
  if (isEdit.value) {
    const res = await getTicketDetail(Number(route.params.id))
    form.title = res.data.title
    form.category = res.data.category
    form.priority = res.data.priority
    form.description = res.data.description || ''
  }
})

async function handleSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    if (isEdit.value) {
      await updateTicket(Number(route.params.id), form)
      ElMessage.success('修改成功')
    } else {
      await createTicket(form)
      ElMessage.success('创建成功')
    }
    router.push('/tickets')
  } finally {
    loading.value = false
  }
}
</script>
