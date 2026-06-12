<template>
  <div class="home-container">
    <el-container>
      <!-- 侧边栏导航 -->
      <el-aside width="200px">
        <div class="logo">
          <h2>企业知识库</h2>
        </div>
        
        <el-menu
          active-text-color="#409EFF"
          background-color="#304156"
          text-color="#bfcbd9"
          router
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          
          <el-menu-item index="/knowledge">
            <el-icon><Folder /></el-icon>
            <span>知识库管理</span>
          </el-menu-item>
          
          <el-menu-item index="/chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>智能问答</span>
          </el-menu-item>
          
          <el-menu-item index="/stats" v-if="isAdmin">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航 -->
        <el-header>
          <div class="header-right">
            <el-dropdown>
              <span class="el-dropdown-link">
                <el-avatar :size="30" :src="avatar" />
                {{ username }}
                <el-icon class="el-icon--right">
                  <arrow-down />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 内容区 -->
        <el-main>
          <div class="welcome-card">
            <h2>欢迎使用企业知识库系统</h2>
            <p>基于RAG技术的企业内部知识问答平台</p>
            
            <div class="quick-actions">
              <el-card 
                shadow="hover" 
                class="action-card" 
                @click="router.push('/knowledge')"
              >
                <el-icon :size="40"><Folder /></el-icon>
                <h3>知识库管理</h3>
                <p>上传和管理企业知识文档</p>
              </el-card>
              
              <el-card 
                shadow="hover" 
                class="action-card" 
                @click="router.push('/chat')"
              >
                <el-icon :size="40"><ChatDotRound /></el-icon>
                <h3>智能问答</h3>
                <p>基于知识库的智能问答</p>
              </el-card>
              
              <el-card 
                shadow="hover" 
                class="action-card" 
                @click="router.push('/stats')"
                v-if="isAdmin"
              >
                <el-icon :size="40"><DataAnalysis /></el-icon>
                <h3>数据统计</h3>
                <p>查看系统使用情况</p>
              </el-card>
            </div>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { 
  HomeFilled, 
  Folder, 
  ChatDotRound, 
  DataAnalysis, 
  ArrowDown 
} from '@element-plus/icons-vue'

const router = useRouter()

const username = ref(localStorage.getItem('username') || '管理员')
const avatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
const isAdmin = computed(() => localStorage.getItem('isAdmin') === 'true')

const handleLogout = () => {
  ElMessageBox.confirm('确认退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('isAdmin')
    router.push('/login')
  })
}
</script>

<style scoped>
.home-container {
  height: 100vh;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  background-color: #2b2f3a;
}

.el-aside {
  background-color: #304156;
  height: 100vh;
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: white;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}

.welcome-card {
  background-color: white;
  border-radius: 5px;
  padding: 30px;
  text-align: center;
}

.welcome-card h2 {
  margin-bottom: 10px;
  color: #333;
}

.welcome-card p {
  color: #666;
  margin-bottom: 30px;
}

.quick-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.action-card {
  width: 200px;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.action-card:hover {
  transform: translateY(-5px);
}

.action-card h3 {
  margin: 15px 0 10px;
}

.action-card p {
  color: #666;
  font-size: 14px;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.el-dropdown-link .el-avatar {
  margin-right: 10px;
}
</style>
