<template>
  <el-header class="navbar">
    <div class="navbar-left">
      <router-link to="/" class="logo">
        <div class="logo-icon">
          <el-icon :size="20"><ShoppingBag /></el-icon>
        </div>
        <span class="logo-text">校园二手</span>
      </router-link>
      <nav class="nav-links">
        <router-link to="/" class="nav-link" exact-active-class="active">首页</router-link>
        <router-link to="/" class="nav-link" active-class="active">全部商品</router-link>
        <router-link to="/feed" class="nav-link" active-class="active">动态</router-link>
      </nav>
    </div>

    <div class="navbar-right">
      <template v-if="userStore.isLoggedIn">
        <router-link to="/publish" class="nav-btn">
          <el-button type="primary" round>发布商品</el-button>
        </router-link>

        <!-- 私信图标 -->
        <router-link to="/messages" class="notif-bell">
          <el-badge :value="unreadMsgCount" :max="99" :hidden="unreadMsgCount === 0">
            <el-icon :size="22"><ChatDotRound /></el-icon>
          </el-badge>
        </router-link>

        <!-- 通知铃铛 -->
        <el-dropdown trigger="click" @command="handleNotifCommand" @visible-change="onNotifOpen">
          <span class="notif-bell">
            <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
              <el-icon :size="22"><Bell /></el-icon>
            </el-badge>
          </span>
          <template #dropdown>
            <div class="notif-panel">
              <div class="notif-header">
                <span>消息通知</span>
                <el-button text size="small" @click="markAllRead">全部已读</el-button>
              </div>
              <div class="notif-list" v-if="notifications.length > 0">
                <div
                  v-for="n in notifications"
                  :key="n.id"
                  class="notif-item"
                  :class="{ unread: !n.isRead }"
                  @click="handleNotifClick(n)"
                >
                  <div class="notif-dot" v-if="!n.isRead"></div>
                  <div class="notif-body">
                    <p class="notif-title">{{ n.title }}</p>
                    <p class="notif-content">{{ n.content }}</p>
                    <span class="notif-time">{{ formatTime(n.createdAt) }}</span>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无通知" :image-size="48" />
            </div>
          </template>
        </el-dropdown>

        <el-dropdown trigger="click">
          <span class="user-dropdown">
            <el-avatar :size="34" :src="userStore.userInfo?.avatar" class="user-avatar" />
            <span class="username">{{ userStore.userInfo?.nickname }}</span>
            <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/my/orders')">
                <el-icon><Document /></el-icon> 我的订单
              </el-dropdown-item>
              <el-dropdown-item @click="$router.push('/my/products')">
                <el-icon><Goods /></el-icon> 我的发布
              </el-dropdown-item>
              <el-dropdown-item @click="$router.push('/my/favorites')">
                <el-icon><Star /></el-icon> 我的收藏
              </el-dropdown-item>
              <el-dropdown-item divided @click="$router.push(`/user/${userStore.userInfo?.id}`)">
                <el-icon><User /></el-icon> 我的主页
              </el-dropdown-item>
              <el-dropdown-item @click="$router.push('/my/following')">
                <el-icon><Connection /></el-icon> 我的关注
              </el-dropdown-item>
              <el-dropdown-item @click="handleLogout">
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template v-else>
        <router-link to="/login" class="login-link">登录</router-link>
        <router-link to="/register">
          <el-button type="primary" round>注册</el-button>
        </router-link>
      </template>
    </div>
  </el-header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { notificationApi } from '../api/notification'
import { messageApi } from '../api/message'

const router = useRouter()
const userStore = useUserStore()

// 通知
const unreadCount = ref(0)
const notifications = ref([])
let pollTimer = null

// 私信
const unreadMsgCount = ref(0)
let msgPollTimer = null

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchUnreadCount()
    fetchMsgUnreadCount()
    pollTimer = setInterval(fetchUnreadCount, 30000) // 30s 轮询通知
    msgPollTimer = setInterval(fetchMsgUnreadCount, 15000) // 15s 轮询私信
  }
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
  if (msgPollTimer) clearInterval(msgPollTimer)
})

async function fetchUnreadCount() {
  try {
    const data = await notificationApi.unreadCount()
    unreadCount.value = data.count
  } catch (e) { /* ignore */ }
}

async function fetchMsgUnreadCount() {
  try {
    const data = await messageApi.unreadCount()
    unreadMsgCount.value = data.count
  } catch (e) { /* ignore */ }
}

async function onNotifOpen(visible) {
  if (visible) {
    try {
      const data = await notificationApi.list({ page: 0, size: 10 })
      notifications.value = data.content
    } catch (e) { /* ignore */ }
  }
}

async function handleNotifClick(notif) {
  if (!notif.isRead) {
    try {
      await notificationApi.markRead(notif.id)
      notif.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) { /* ignore */ }
  }
  // 跳转到关联页面
  if (notif.relatedId) {
    if (notif.type === 'ORDER' || notif.type === 'SYSTEM') {
      router.push('/my/orders')
    }
  }
}

async function markAllRead() {
  try {
    await notificationApi.markAllRead()
    notifications.value.forEach(n => n.isRead = true)
    unreadCount.value = 0
    ElMessage.success('已全部标为已读')
  } catch (e) { /* ignore */ }
}

function handleNotifCommand(cmd) {
  // dropdown command handler placeholder
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return d.toLocaleDateString()
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 32px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 36px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  border-radius: var(--radius-sm);
  color: #fff;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-links {
  display: flex;
  gap: 4px;
}

.nav-links .nav-link {
  padding: 6px 16px;
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  font-weight: 500;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.nav-links .nav-link:hover,
.nav-links .nav-link.active {
  color: var(--color-primary);
  background: rgba(16, 185, 129, 0.08);
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-full);
  transition: background var(--transition-fast);
}

.user-dropdown:hover {
  background: var(--color-bg);
}

.user-avatar {
  box-shadow: 0 0 0 3px var(--color-primary-lighter);
}

.username {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text);
}

.dropdown-arrow {
  color: var(--color-text-muted);
  font-size: 12px;
}

.login-link {
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  font-weight: 500;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.login-link:hover {
  color: var(--color-primary);
  background: rgba(16, 185, 129, 0.06);
}

/* === Notification === */
.notif-bell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}

.notif-bell:hover {
  background: var(--color-bg);
  color: var(--color-primary);
}

.notif-panel {
  width: 360px;
  max-height: 420px;
  display: flex;
  flex-direction: column;
}

.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--color-border-light);
  font-size: var(--text-sm);
  font-weight: 600;
}

.notif-list {
  overflow-y: auto;
  flex: 1;
}

.notif-item {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background var(--transition-fast);
  border-bottom: 1px solid var(--color-border-light);
}

.notif-item:hover {
  background: var(--color-bg);
}

.notif-item.unread {
  background: rgba(16, 185, 129, 0.03);
}

.notif-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
  margin-top: 6px;
  flex-shrink: 0;
}

.notif-body {
  flex: 1;
  min-width: 0;
}

.notif-title {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: 2px;
}

.notif-content {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notif-time {
  font-size: 11px;
  color: var(--color-text-muted);
}
</style>
