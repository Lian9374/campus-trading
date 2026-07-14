<template>
  <Layout>
    <div class="messages-page">
      <!-- 左栏：会话列表 -->
      <div class="conversation-panel" :class="{ hidden: activeConversationId && isMobile }">
        <div class="panel-header">
          <h3>私信</h3>
          <el-button size="small" type="primary" plain @click="showNewMsgDialog = true">
            <el-icon><Plus /></el-icon> 新私信
          </el-button>
        </div>
        <div class="conversation-list" v-if="conversations.length > 0">
          <div
            v-for="c in conversations"
            :key="c.id"
            class="conversation-item"
            :class="{ active: activeConversationId === c.id }"
            @click="openConversation(c)"
          >
            <div class="conv-avatar">
              <el-avatar :size="42" :src="c.otherUserAvatar">
                {{ (c.otherUserName || '?')[0] }}
              </el-avatar>
              <span class="unread-badge" v-if="c.unreadCount > 0">{{ c.unreadCount > 99 ? '99+' : c.unreadCount }}</span>
            </div>
            <div class="conv-body">
              <div class="conv-top">
                <span class="conv-name">{{ c.otherUserName }}</span>
                <span class="conv-time">{{ formatTime(c.lastMessageAt) }}</span>
              </div>
              <p class="conv-preview">{{ c.lastMessage || '暂无消息' }}</p>
              <p class="conv-product">{{ c.productTitle }}</p>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无会话" :image-size="60" />
      </div>

      <!-- 右栏：消息详情 -->
      <div class="message-panel" :class="{ hidden: !activeConversationId && isMobile }">
        <template v-if="activeConversation">
          <!-- 头部 -->
          <div class="msg-header">
            <el-button v-if="isMobile" text @click="activeConversationId = null" class="back-btn">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <div class="msg-header-info" @click="$router.push(`/product/${activeConversation.productId}`)">
              <img :src="activeConversation.productCover || 'https://placehold.co/40x40/f5f7fa/999?text=N'" class="header-product-img" />
              <div>
                <strong>{{ activeConversation.otherUserName }}</strong>
                <span class="header-product-title">{{ activeConversation.productTitle }}</span>
              </div>
            </div>
            <el-button size="small" @click="$router.push(`/product/${activeConversation.productId}`)">
              查看商品
            </el-button>
          </div>

          <!-- 消息列表 -->
          <div class="msg-list" ref="msgListRef">
            <div
              v-for="m in messages"
              :key="m.id"
              class="msg-item"
              :class="{ 'msg-mine': m.senderId === userStore.userInfo?.id }"
            >
              <div class="msg-bubble">
                <p class="msg-text">{{ m.content }}</p>
                <span class="msg-time">{{ formatTime(m.createdAt) }}</span>
              </div>
            </div>
            <div v-if="loadingMore" class="loading-more">加载中...</div>
            <div v-if="!hasMore && messages.length > 10" class="no-more">— 没有更多消息了 —</div>
          </div>

          <!-- 输入框 -->
          <div class="msg-input-area">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="2"
              placeholder="输入消息，Enter 发送，Shift+Enter 换行"
              @keydown.enter.exact.prevent="sendMessage"
              resize="none"
            />
            <el-button type="primary" :disabled="!inputText.trim()" @click="sendMessage" class="send-btn">
              发送
            </el-button>
          </div>
        </template>
        <div v-else class="empty-chat">
          <el-empty description="选择一个会话开始聊天" :image-size="80" />
        </div>
      </div>
    </div>

    <!-- 新私信弹窗 -->
    <el-dialog v-model="showNewMsgDialog" title="发起新私信" width="460px" :before-close="() => showNewMsgDialog = false">
      <el-input
        v-model="newMsgKeyword"
        placeholder="搜索用户昵称..."
        size="large"
        clearable
        @input="searchUsersForMsg"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <div style="margin-top: 14px; max-height: 320px; overflow-y: auto">
        <div v-for="u in newMsgUsers" :key="u.id" class="new-msg-user" @click="startChat(u)">
          <el-avatar :size="40" :src="u.avatar">{{ (u.nickname || '?')[0] }}</el-avatar>
          <div class="new-msg-body">
            <span class="new-msg-name">{{ u.nickname }}</span>
            <span class="new-msg-campus" v-if="u.campus">{{ u.campus }}</span>
          </div>
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <el-empty v-if="newMsgKeyword && newMsgKeyword.length >= 2 && newMsgUsers.length === 0" description="未找到用户" :image-size="48" />
        <div v-if="newMsgKeyword.length < 2" style="text-align: center; color: var(--color-text-muted); padding: 20px; font-size: var(--text-sm)">
          输入至少2个字符搜索用户
        </div>
      </div>
      <template #footer>
        <el-button @click="showNewMsgDialog = false">取消</el-button>
      </template>
    </el-dialog>
  </Layout>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useUserStore } from '../stores/user'
import { messageApi } from '../api/message'
import { userApi } from '../api/user'
import Layout from '../components/Layout.vue'

const userStore = useUserStore()
const conversations = ref([])
const activeConversationId = ref(null)
const activeConversation = ref(null)
const messages = ref([])
const inputText = ref('')
const msgListRef = ref(null)
const hasMore = ref(true)
const currentPage = ref(0)
const loadingMore = ref(false)
const isMobile = ref(window.innerWidth < 768)

let pollTimer = null

onMounted(async () => {
  window.addEventListener('resize', onResize)
  await fetchConversations()

  // 检查 URL query 参数
  const query = new URLSearchParams(window.location.search)
  const productId = query.get('productId')
  const receiverId = query.get('receiverId')

  if (receiverId) {
    // 从用户主页或商品页跳转过来，查找或创建会话
    const existing = conversations.value.find(
      c => c.otherUserId == receiverId && (productId ? c.productId == productId : c.productId === 0)
    )
    if (existing) {
      openConversation(existing)
    } else {
      // 自动发起新私信
      try {
        const payload = { receiverId: Number(receiverId), content: '你好！' }
        if (productId) payload.productId = Number(productId)
        await messageApi.send(payload)
        await fetchConversations()
        const conv = conversations.value.find(
          c => c.otherUserId == receiverId && (productId ? c.productId == productId : c.productId === 0)
        )
        if (conv) openConversation(conv)
      } catch (e) { /* ignore */ }
    }
  }

  if (userStore.isLoggedIn) {
    pollTimer = setInterval(pollMessages, 10000)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  if (pollTimer) clearInterval(pollTimer)
})

function onResize() {
  isMobile.value = window.innerWidth < 768
}

async function fetchConversations() {
  try {
    const data = await messageApi.conversations({ page: 0, size: 50 })
    conversations.value = data.content
  } catch (e) { /* ignore */ }
}

async function openConversation(c) {
  activeConversationId.value = c.id
  activeConversation.value = c
  currentPage.value = 0
  hasMore.value = true
  messages.value = []

  try {
    const data = await messageApi.getMessages(c.id, { page: 0, size: 30 })
    messages.value = data.content.reverse() // 最旧的在前面
    hasMore.value = !data.last
    currentPage.value = 0

    // 清除未读
    if (c.unreadCount > 0) {
      c.unreadCount = 0
      await messageApi.markRead(c.id)
    }

    await nextTick()
    scrollToBottom()
  } catch (e) { /* ignore */ }
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || !activeConversation.value) return
  inputText.value = ''

  try {
    const data = await messageApi.send({
      productId: activeConversation.value.productId,
      receiverId: activeConversation.value.otherUserId,
      content: text
    })
    // 如果是新会话，更新会话列表
    if (data.id !== activeConversationId.value) {
      activeConversationId.value = data.id
    }
    // 刷新会话列表获取最新 lastMessage
    await fetchConversations()
    // 局部添加消息
    messages.value.push({
      id: Date.now(),
      conversationId: activeConversationId.value,
      senderId: userStore.userInfo.id,
      senderName: userStore.userInfo.nickname,
      content: text,
      isRead: false,
      createdAt: new Date().toISOString()
    })
    await nextTick()
    scrollToBottom()
  } catch (e) { /* ignore */ }
}

async function pollMessages() {
  try {
    await fetchConversations()
    // 如果在聊天中，刷新当前会话
    if (activeConversationId.value) {
      const data = await messageApi.getMessages(activeConversationId.value, { page: 0, size: 30 })
      const oldLen = messages.value.length
      messages.value = data.content.reverse()
      if (messages.value.length > oldLen) {
        await nextTick()
        scrollToBottom()
      }
    }
  } catch (e) { /* ignore */ }
}

// 新私信
const showNewMsgDialog = ref(false)
const newMsgKeyword = ref('')
const newMsgUsers = ref([])
let newMsgTimer = null

function searchUsersForMsg() {
  if (newMsgTimer) clearTimeout(newMsgTimer)
  const kw = newMsgKeyword.value.trim()
  if (kw.length < 2) { newMsgUsers.value = []; return }
  newMsgTimer = setTimeout(async () => {
    try {
      const data = await userApi.discoverUsers(kw, { page: 0, size: 10 })
      newMsgUsers.value = data.content
    } catch (e) { newMsgUsers.value = [] }
  }, 300)
}

async function startChat(u) {
  showNewMsgDialog.value = false
  newMsgKeyword.value = ''
  newMsgUsers.value = []

  // 检查是否已有与该用户的会话
  const existing = conversations.value.find(
    c => c.otherUserId === u.id && c.productId === 0
  )
  if (existing) {
    openConversation(existing)
    return
  }

  // 发第一条消息创建会话
  try {
    const data = await messageApi.send({
      receiverId: u.id,
      content: '你好！'
    })
    await fetchConversations()
    // 打开新创建的会话
    const conv = conversations.value.find(c => c.id === data.id)
    if (conv) {
      openConversation(conv)
    } else if (data.id) {
      activeConversationId.value = data.id
      activeConversation.value = { ...data, otherUserId: u.id, otherUserName: u.nickname, otherUserAvatar: u.avatar }
      messages.value = [{ id: Date.now(), conversationId: data.id, senderId: userStore.userInfo.id, senderName: userStore.userInfo.nickname, content: '你好！', isRead: false, createdAt: new Date().toISOString() }]
    }
  } catch (e) { /* handled */ }
}

function scrollToBottom() {
  if (msgListRef.value) {
    msgListRef.value.scrollTop = msgListRef.value.scrollHeight
  }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString()
}
</script>

<style scoped>
.messages-page {
  display: flex;
  height: calc(100vh - 64px - 200px);
  min-height: 500px;
  max-width: 1000px;
  margin: 0 auto;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--color-surface);
  border: 1px solid rgba(0, 0, 0, 0.04);
  margin-top: 20px;
}

/* === Left Panel === */
.conversation-panel {
  width: 340px;
  min-width: 340px;
  border-right: 1px solid var(--color-border-light);
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text);
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  gap: 12px;
  padding: 14px 20px;
  cursor: pointer;
  transition: background var(--transition-fast);
  border-bottom: 1px solid var(--color-border-light);
}

.conversation-item:hover {
  background: var(--color-bg-alt);
}

.conversation-item.active {
  background: rgba(16, 185, 129, 0.06);
}

.conv-avatar {
  position: relative;
  flex-shrink: 0;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  background: var(--color-danger);
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.conv-body {
  flex: 1;
  min-width: 0;
}

.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 3px;
}

.conv-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text);
}

.conv-time {
  font-size: 11px;
  color: var(--color-text-muted);
  flex-shrink: 0;
}

.conv-preview {
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 2px;
}

.conv-product {
  font-size: 11px;
  color: var(--color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* === Right Panel === */
.message-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.msg-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-bottom: 1px solid var(--color-border-light);
  background: var(--color-bg-alt);
}

.back-btn {
  display: none;
}

.msg-header-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  min-width: 0;
}

.header-product-img {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.msg-header-info strong {
  font-size: var(--text-sm);
  color: var(--color-text);
  display: block;
}

.header-product-title {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 250px;
  display: block;
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--color-bg-alt);
}

.msg-item {
  display: flex;
  max-width: 75%;
}

.msg-item.msg-mine {
  align-self: flex-end;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
  position: relative;
}

.msg-mine .msg-bubble {
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary));
  color: #fff;
}

.msg-text {
  font-size: var(--text-sm);
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-mine .msg-text {
  color: #fff;
}

.msg-time {
  font-size: 10px;
  color: var(--color-text-muted);
  display: block;
  margin-top: 4px;
  text-align: right;
}

.msg-mine .msg-time {
  color: rgba(255, 255, 255, 0.7);
}

.loading-more, .no-more {
  text-align: center;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  padding: 8px;
}

.msg-input-area {
  display: flex;
  gap: 10px;
  padding: 14px 20px;
  border-top: 1px solid var(--color-border-light);
  align-items: flex-end;
  background: var(--color-surface);
}

.msg-input-area :deep(.el-textarea__inner) {
  border-radius: var(--radius-sm);
}

.send-btn {
  flex-shrink: 0;
  height: 40px;
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* === Mobile === */
/* === New Message Dialog === */
.new-msg-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.new-msg-user:hover {
  background: var(--color-bg-alt);
}

.new-msg-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.new-msg-name {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text);
}

.new-msg-campus {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

@media (max-width: 768px) {
  .messages-page {
    height: calc(100vh - 64px);
    margin-top: 0;
    border-radius: 0;
    border: none;
  }

  .conversation-panel {
    width: 100%;
    min-width: 100%;
  }

  .conversation-panel.hidden {
    display: none;
  }

  .message-panel.hidden {
    display: none;
  }

  .back-btn {
    display: flex;
  }
}
</style>
