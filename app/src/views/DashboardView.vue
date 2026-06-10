<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="cards">
      <el-col :span="6">
        <el-card shadow="hover" class="card total">
          <div class="card-inner">
            <div class="label">总工单数</div>
            <div class="value">{{ overview.total }}</div>
          </div>
          <el-icon class="icon"><Tickets /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="card pending">
          <div class="card-inner">
            <div class="label">待处理</div>
            <div class="value">{{ overview.pending }}</div>
          </div>
          <el-icon class="icon"><Clock /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="card processing">
          <div class="card-inner">
            <div class="label">处理中</div>
            <div class="value">{{ overview.processing }}</div>
          </div>
          <el-icon class="icon"><Loading /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="card completed">
          <div class="card-inner">
            <div class="label">已完成</div>
            <div class="value">{{ overview.completed }}</div>
          </div>
          <el-icon class="icon"><CircleCheck /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="20">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>近7天工单趋势</template>
          <div ref="trendChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>按分类统计</template>
          <div ref="categoryChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getTrend, getByCategory } from '@/api/stats'

const overview = reactive({ total: 0, pending: 0, processing: 0, completed: 0 })
const trendChartRef = ref<HTMLElement>()
const categoryChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null

onMounted(async () => {
  await loadOverview()
  await loadTrend()
  await loadCategory()
})

onUnmounted(() => {
  trendChart?.dispose()
  categoryChart?.dispose()
})

async function loadOverview() {
  const res = await getOverview()
  Object.assign(overview, res.data)
}

async function loadTrend() {
  const res = await getTrend(7)
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, bottom: 30, top: 20 },
    xAxis: { type: 'category', data: res.data.map(i => i.date) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      data: res.data.map(i => i.count),
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#409eff' }
    }]
  })
}

async function loadCategory() {
  const res = await getByCategory()
  if (!categoryChartRef.value) return
  categoryChart = echarts.init(categoryChartRef.value)
  categoryChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: res.data,
      label: { show: true, formatter: '{b}: {c}' }
    }]
  })
}
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.cards .card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
}
.card .label {
  font-size: 14px;
  color: #909399;
}
.card .value {
  font-size: 32px;
  font-weight: bold;
  margin-top: 8px;
}
.card .icon {
  font-size: 48px;
  opacity: 0.2;
}
.card.total .value { color: #409eff; }
.card.pending .value { color: #e6a23c; }
.card.processing .value { color: #f56c6c; }
.card.completed .value { color: #67c23a; }
</style>
