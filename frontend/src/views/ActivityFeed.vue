<template>
  <Layout>
    <div class="feed-page">
      <h2 class="page-title">动态</h2>
      <div class="feed-list" v-if="activities.length > 0">
        <div v-for="a in activities" :key="a.id" class="feed-item" @click="goToTarget(a)">
          <el-avatar :size="36" class="feed-avatar">{{ (getUserName(a.userId) || '?')[0] }}</el-avatar>
          <div class="feed-body">
            <p class="feed-summary">
              <strong class="feed-actor">{{ getUserName(a.userId) || '用户' }}</strong>
              {{ getActionText(a.type) }}
            </p>
            <p class="feed-desc" v-if="a.summary">{{ a.summary }}</p>
            <span class="feed-time">{{ formatTime(a.createdAt) }}</span>
          </div>
        </div>
        <el-pagination
          v-if="total > 20"
          v-model:current-page="currentPage"
          :page-size="20"
          :total="total"
          layout="prev, pager, next"
          small background
          class="pagination"
          @current-change="fetchFeed"
        />
      </div>
      <el-empty v-else description="关注更多卖家，查看最新动态" :image-size="80" />
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Layout from '../components/Layout.vue'
import { activityApi } from '../api/activity'

const router = useRouter()
const activities = ref([])
const currentPage = ref(1)
const total = ref(0)

onMounted(() => fetchFeed())

async function fetchFeed(page = 1) {
  currentPage.value = page
  try {
    const data = await activityApi.feed({ page: page - 1, size: 20 })
    activities.value = data.content
    total.value = data.totalElements
  } catch (e) { /* ignore */ }
}

function getUserName(userId) {
  // 简单返回，实际需要从数据中获取
  return ''
}

function getActionText(type) {
  const map = {
    NEW_PRODUCT: '发布了新商品',
    SOLD: '售出了一件商品',
    NEW_REVIEW: '发表了新评价',
    PRICE_DROP: '降价了'
  }
  return map[type] || ''
}

function goToTarget(a) {
  if (a.type === 'NEW_PRODUCT' || a.type === 'PRICE_DROP') {
    router.push(`/product/${a.targetId}`)
  } else if (a.type === 'NEW_REVIEW') {
    router.push(`/user/${a.userId}`)
  }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now - d
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return d.toLocaleDateString()
}
</script>

<style scoped>
.feed-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 24px;
}

.feed-item {
  display: flex;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.feed-item:hover {
  background: var(--color-bg-alt);
  margin: 0 -12px;
  padding-left: 12px;
  padding-right: 12px;
  border-radius: var(--radius-sm);
}

.feed-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary));
  color: #fff;
}

.feed-body { flex: 1; min-width: 0; }

.feed-summary { font-size: var(--text-sm); color: var(--color-text); margin-bottom: 2px; }
.feed-actor { color: var(--color-primary-dark); }

.feed-desc {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.feed-time {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.pagination {
  justify-content: center;
  margin-top: 24px;
}
</style>
