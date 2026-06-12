<template>
  <div class="stats-container">
    <div class="header">
      <h2>数据统计</h2>
      <p>系统使用情况数据分析</p>
    </div>
    
    <div class="stats-grid">
      <!-- 统计卡片 -->
      <el-card class="stat-card">
        <div class="stat-item">
          <div class="stat-icon" style="background-color: #409EFF;">
            <el-icon :size="30"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">156</div>
            <div class="stat-label">文档总数</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-item">
          <div class="stat-icon" style="background-color: #67C23A;">
            <el-icon :size="30"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">328</div>
            <div class="stat-label">问答总数</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-item">
          <div class="stat-icon" style="background-color: #E6A23C;">
            <el-icon :size="30"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">42</div>
            <div class="stat-label">活跃用户</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-item">
          <div class="stat-icon" style="background-color: #F56C6C;">
            <el-icon :size="30"><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">89%</div>
            <div class="stat-label">回答准确率</div>
          </div>
        </div>
      </el-card>
    </div>
    
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>文档上传趋势</span>
          <el-select v-model="timeRange" style="width: 120px;">
            <el-option label="最近7天" value="7days" />
            <el-option label="最近30天" value="30days" />
            <el-option label="最近90天" value="90days" />
          </el-select>
        </div>
      </template>
      <div class="chart-container" ref="docTrendChart"></div>
    </el-card>
    
    <div class="chart-row">
      <el-card class="chart-card">
        <template #header>
          <span>问答数量分布</span>
        </template>
        <div class="chart-container" ref="qaDistChart"></div>
      </el-card>
      
      <el-card class="chart-card">
        <template #header>
          <span>热门文档</span>
        </template>
        <div class="chart-container" ref="hotDocChart"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import { 
  Document, 
  ChatDotRound, 
  User, 
  Timer 
} from '@element-plus/icons-vue'

const timeRange = ref('30days')
const docTrendChart = ref(null)
const qaDistChart = ref(null)
const hotDocChart = ref(null)

// 初始化图表
const initCharts = () => {
  // 文档上传趋势图
  const trendChart = echarts.init(docTrendChart.value)
  trendChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: [12, 18, 15, 22, 30, 24, 35],
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        },
        lineStyle: {
          width: 3,
          color: '#409EFF'
        },
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  })

  // 问答数量分布图
  const qaChart = echarts.init(qaDistChart.value)
  qaChart.setOption({
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        name: '问答分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 1048, name: '产品相关' },
          { value: 735, name: '技术相关' },
          { value: 580, name: '财务相关' },
          { value: 484, name: '人力资源' },
          { value: 300, name: '其他' }
        ]
      }
    ]
  })

  // 热门文档图
  const hotDocChart = echarts.init(hotDocChart.value)
  hotDocChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: ['产品手册.pdf', '技术规范.docx', '财务制度.xlsx', '员工手册.pdf', '培训资料.pptx']
    },
    series: [
      {
        name: '引用次数',
        type: 'bar',
        data: [120, 85, 62, 45, 32],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#007dee' }
          ])
        }
      }
    ]
  })

  // 窗口大小变化时重新调整图表大小
  window.addEventListener('resize', function() {
    trendChart.resize()
    qaChart.resize()
    hotDocChart.resize()
  })
}

watch(timeRange, () => {
  // 实际项目中根据时间范围重新获取数据
  initCharts()
})

onMounted(() => {
  initCharts()
})
</script>

<style scoped>
.stats-container {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  margin-bottom: 5px;
  color: #333;
}

.header p {
  color: #666;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-item {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 15px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
</style>
