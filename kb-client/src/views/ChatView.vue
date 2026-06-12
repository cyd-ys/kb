<template>
  <div class="chat-container">
    <div class="header">
      <h2>智能问答</h2>
      <p>基于企业知识库的智能问答系统</p>
    </div>
    
    <el-card class="chat-card">
      <div class="chat-wrapper">
        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef">
          <div 
            v-for="(message, index) in messages" 
            :key="index" 
            class="message-item"
            :class="{ 'user-message': message.role === 'user', 'bot-message': message.role === 'assistant' }"
          >
            <div class="message-content">
              <div class="message-avatar">
                <el-avatar 
                  v-if="message.role === 'user'" 
                  :size="40" 
                  :src="userAvatar"
                />
                <el-avatar 
                  v-else 
                  :size="40" 
                  :src="botAvatar" 
                  class="bot-avatar"
                />
              </div>
              
              <div class="message-text">
                <div class="message-role">
                  {{ message.role === 'user' ? '你' : 'AI助手' }}
                </div>
                <div class="message-body">
                  <div v-if="message.role === 'assistant' && message.sources" class="sources">
                    <el-tag 
                      v-for="(source, i) in message.sources" 
                      :key="i" 
                      size="small" 
                      type="info"
                      @click="showSourceDetail(source)"
                    >
                      来源 {{ i + 1 }}
                    </el-tag>
                  </div>
                  <div class="text-content">
                    {{ message.content }}
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div v-if="loading" class="loading-indicator">
            <el-icon class="is-loading" :size="20"><Loading /></el-icon>
            <span>AI正在思考中...</span>
          </div>
        </div>
        
        <!-- 输入区域 -->
        <div class="input-area">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题..."
            resize="none"
            @keyup.enter="sendMessage"
          />
          <div class="input-actions">
            <el-button 
              type="primary" 
              :loading="loading" 
              @click="sendMessage"
              :disabled="!inputMessage.trim()"
            >
              发送
            </el-button>
            <el-button @click="clearMessages">
              清空对话
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 来源详情对话框 -->
    <el-dialog v-model="sourceDialogVisible" title="知识来源" width="50%">
      <div v-if="currentSource">
        <h4>{{ currentSource.file }}</h4>
        <p class="source-content">{{ currentSource.text }}</p>
        <p class="source-score">相似度: {{ currentSource.score.toFixed(2) }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import axios from 'axios'

const messages = ref([
  {
    role: 'assistant',
    content: '您好！我是企业知识库AI助手，请问有什么可以帮助您的吗？',
    sources: []
  }
])

const inputMessage = ref('')
const loading = ref(false)
const messageListRef = ref(null)
const sourceDialogVisible = ref(false)
const currentSource = ref(null)

const userAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
const botAvatar = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const userMessage = {
    role: 'user',
    content: inputMessage.value.trim(),
    timestamp: new Date().toISOString()
  }
  
  messages.value.push(userMessage)
  inputMessage.value = ''
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  loading.value = true
  try {
    // 模拟API调用
    // const response = await axios.post('/api/chat', {
    //   question: userMessage.content
    // })
    
    // 模拟响应
    setTimeout(() => {
      const botMessage = {
        role: 'assistant',
        content: `这是关于"${userMessage.content}"的回答。企业知识库中相关信息显示...`,
        sources: [
          {
            file: '企业产品手册.pdf',
            text: '相关文档内容片段1...',
            score: 0.85
          },
          {
            file: '员工培训资料.docx',
            text: '相关文档内容片段2...',
            score: 0.72
          }
        ],
        timestamp: new Date().toISOString()
      }
      
      messages.value.push(botMessage)
      loading.value = false
      scrollToBottom()
    }, 1500)
  } catch (error) {
    ElMessage.error('请求失败，请稍后再试')
    loading.value = false
  }
}

const clearMessages = () => {
  messages.value = [
    {
      role: 'assistant',
      content: '您好！我是企业知识库AI助手，请问有什么可以帮助您的吗？',
      sources: []
    }
  ]
}

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

const showSourceDetail = (source) => {
  currentSource.value = source
  sourceDialogVisible.value = true
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-container {
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

.chat-card {
  height: calc(100vh - 180px);
}

.chat-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  margin-bottom: 20px;
}

.message-item {
  margin-bottom: 20px;
}

.message-content {
  display: flex;
  max-width: 80%;
}

.user-message {
  justify-content: flex-end;
}

.user-message .message-content {
  flex-direction: row-reverse;
}

.bot-message {
  justify-content: flex-start;
}

.message-avatar {
  margin: 0 10px;
}

.bot-avatar {
  background-color: #409EFF;
}

.message-text {
  max-width: calc(100% - 60px);
}

.message-role {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
}

.user-message .message-role {
  text-align: right;
}

.message-body {
  padding: 10px 15px;
  border-radius: 5px;
  position: relative;
}

.user-message .message-body {
  background-color: #409EFF;
  color: white;
  margin-left: 20px;
}

.bot-message .message-body {
  background-color: #f5f7fa;
  color: #333;
  margin-right: 20px;
}

.user-message .message-body::before {
  content: '';
  position: absolute;
  right: -10px;
  top: 10px;
  width: 0;
  height: 0;
  border-top: 10px solid transparent;
  border-bottom: 10px solid transparent;
  border-left: 10px solid #409EFF;
}

.bot-message .message-body::before {
  content: '';
  position: absolute;
  left: -10px;
  top: 10px;
  width: 0;
  height: 0;
  border-top: 10px solid transparent;
  border-bottom: 10px solid transparent;
  border-right: 10px solid #f5f7fa;
}

.sources {
  margin-bottom: 8px;
}

.sources .el-tag {
  margin-right: 5px;
  cursor: pointer;
}

.text-content {
  white-space: pre-wrap;
  word-break: break-word;
}

.input-area {
  padding: 10px;
  border-top: 1px solid #eee;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  padding: 10px;
}

.loading-indicator .el-icon {
  margin-right: 5px;
  animation: rotating 2s linear infinite;
}

.source-content {
  margin: 15px 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 5px;
}

.source-score {
  color: #666;
  font-size: 14px;
  text-align: right;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
