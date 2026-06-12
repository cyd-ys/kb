<template>
  <div class="knowledge-container">
    <div class="header">
      <h2>知识库管理</h2>
      <p>上传和管理企业知识文档</p>
    </div>
    
    <el-card class="upload-card">
      <el-upload
        class="upload-demo"
        drag
        multiple
        :action="uploadUrl"
        :headers="headers"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        :file-list="fileList"
      >
        <el-icon :size="60"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持上传 PDF、Word、Excel、TXT、Markdown 文件，单个文件不超过100MB
          </div>
        </template>
      </el-upload>
    </el-card>
    
    <el-card class="document-list">
      <template #header>
        <div class="card-header">
          <span>已上传文档</span>
          <el-button type="primary" size="small" @click="refreshList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <el-table :data="documents" style="width: 100%">
        <el-table-column prop="name" label="文件名" width="200" />
        <el-table-column prop="size" label="大小" width="120" />
        <el-table-column prop="uploadTime" label="上传时间" width="180" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="previewDocument(row)">查看</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteDocument(row)"
              :loading="row.deleting"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const uploadUrl = ref('http://localhost:8080/api/documents/upload')
const headers = ref({
  Authorization: `Bearer ${localStorage.getItem('token')}`
})
const fileList = ref([])
const documents = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 模拟获取文档列表
const fetchDocuments = async () => {
  try {
    // 实际项目中调用API
    // const response = await axios.get('/api/documents', {
    //   params: { page: currentPage.value, size: pageSize.value }
    // })
    
    // 模拟数据
    documents.value = [
      {
        id: '1',
        name: '企业产品手册.pdf',
        size: '2.5MB',
        uploadTime: '2023-05-10 14:30',
        status: '已索引'
      },
      {
        id: '2',
        name: '员工培训资料.docx',
        size: '1.2MB',
        uploadTime: '2023-05-09 10:15',
        status: '已索引'
      },
      {
        id: '3',
        name: '财务制度.xlsx',
        size: '800KB',
        uploadTime: '2023-05-08 16:45',
        status: '处理中'
      }
    ]
    total.value = documents.value.length
  } catch (error) {
    ElMessage.error('获取文档列表失败')
  }
}

const beforeUpload = (file) => {
  const allowedTypes = [
    'application/pdf',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'text/plain',
    'text/markdown'
  ]
  
  const isAllowed = allowedTypes.includes(file.type)
  const isLt100M = file.size / 1024 / 1024 < 100
  
  if (!isAllowed) {
    ElMessage.error('不支持的文件类型!')
  }
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过100MB!')
  }
  
  return isAllowed && isLt100M
}

const handleUploadSuccess = (response, file) => {
  ElMessage.success(`${file.name} 上传成功`)
  fetchDocuments()
}

const handleUploadError = (error, file) => {
  ElMessage.error(`${file.name} 上传失败: ${error.message}`)
}

const deleteDocument = async (row) => {
  try {
    row.deleting = true
    // 实际项目中调用API
    // await axios.delete(`/api/documents/${row.id}`)
    ElMessage.success('删除成功')
    fetchDocuments()
  } catch (error) {
    ElMessage.error('删除失败')
  } finally {
    row.deleting = false
  }
}

const previewDocument = (row) => {
  ElMessage.info(`预览文档: ${row.name}`)
  // 实际项目中实现文档预览逻辑
}

const refreshList = () => {
  fetchDocuments()
}

const getStatusType = (status) => {
  const statusMap = {
    '已索引': 'success',
    '处理中': 'warning',
    '失败': 'danger'
  }
  return statusMap[status] || ''
}

onMounted(() => {
  fetchDocuments()
})
</script>

<style scoped>
.knowledge-container {
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

.upload-card {
  margin-bottom: 20px;
}

.document-list {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.upload-demo {
  text-align: center;
}

.el-upload__tip {
  margin-top: 10px;
  color: #666;
  font-size: 14px;
}
</style>
