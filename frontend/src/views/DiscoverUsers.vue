<template>
  <Layout>
    <div class="discover-page">
      <div class="discover-header">
        <h2>发现用户</h2>
        <p class="subtitle">找到你感兴趣的同学，关注他们，建立联系</p>
        <div class="search-bar">
          <el-input
            v-model="keyword"
            placeholder="搜索昵称或用户名..."
            size="large"
            clearable
            @input="onSearchInput"
            @clear="keyword = ''; fetchUsers(1)"
          >
            <template #prefix><el-icon :size="20"><Search /></el-icon></template>
          </el-input>
        </div>
      </div>

      <!-- 用户卡片网格 -->
      <div class="user-grid" v-if="users.length > 0">
        <div
          v-for="u in users"
          :key="u.id"
          class="user-card"
        >
          <div class="user-card-top" @click="$router.push(`/user/${u.id}`)">
            <el-avatar :size="64" :src="u.avatar">
              {{ (u.nickname || '?')[0] }}
            </el-avatar>
            <h4 class="uc-name">{{ u.nickname }}</h4>
            <el-tag v-if="u.campus" size="small" type="info" class="uc-campus">
              <el-icon><Location /></el-icon> {{ u.campus }}
            </el-tag>
          </div>
          <div class="user-card-actions">
            <el-button
              size="small" plain
              @click="goToChat(u)"
              v-if="userStore.isLoggedIn && userStore.userInfo?.id !== u.id"
            >
              <el-icon><ChatDotRound /></el-icon> 私信
            </el-button>
            <el-button
              v-if="userStore.isLoggedIn && userStore.userInfo?.id !== u.id"
              size="small"
              :type="u.isFollowing ? 'default' : 'primary'"
              @click="toggleFollow(u)"
            >
              {{ u.isFollowing ? '已关注' : '+ 关注' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-else-if="!loading" :description="keyword ? '未找到匹配的用户' : '暂无用户'" :image-size="80" />

      <!-- 骨架屏 -->
      <div class="user-grid" v-if="loading">
        <div v-for="i in 8" :key="'sk'+i" class="user-card skeleton-card">
          <div class="skeleton skeleton-avatar"></div>
          <div class="skeleton skeleton-name"></div>
          <div class="skeleton skeleton-tag"></div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="fetchUsers"
        />
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from '../components/Layout.vue'
import { useUserStore } from '../stores/user'
import { userApi } from '../api/user'
import { followApi } from '../api/follow'

const router = useRouter()
const userStore = useUserStore()

const keyword = ref('')
const users = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

let searchTimer = null

onMounted(() => fetchUsers())

async function fetchUsers(page = 1) {
  currentPage.value = page
  loading.value = true
  try {
    const kw = keyword.value.trim() || undefined
    const data = await userApi.discoverUsers(kw, { page: page - 1, size: pageSize.value })
    users.value = data.content.map(u => ({ ...u, isFollowing: false }))
    total.value = data.totalElements
    // 批量检查关注状态
    if (userStore.isLoggedIn && users.value.length > 0) {
      const results = await Promise.allSettled(
        users.value.map(u => followApi.check(u.id))
      )
      results.forEach((r, i) => {
        if (r.status === 'fulfilled' && r.value?.following) {
          users.value[i].isFollowing = true
        }
      })
    }
  } catch (e) { /* */ }
  finally { loading.value = false }
}

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => fetchUsers(1), 350)
}

async function toggleFollow(u) {
  try {
    if (u.isFollowing) {
      await followApi.unfollow(u.id)
      u.isFollowing = false
      ElMessage.success('已取消关注')
    } else {
      await followApi.follow(u.id)
      u.isFollowing = true
      ElMessage.success('关注成功')
    }
  } catch (e) { /* handled */ }
}

function goToChat(u) {
  router.push(`/messages?receiverId=${u.id}`)
}
</script>

<style scoped>
.discover-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 20px;
}

.discover-header {
  text-align: center;
  margin-bottom: 32px;
}

.discover-header h2 {
  font-size: 28px;
  font-weight: 800;
  color: var(--color-text);
  margin-bottom: 6px;
}

.subtitle {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin-bottom: 20px;
}

.search-bar {
  max-width: 440px;
  margin: 0 auto;
}

.search-bar :deep(.el-input__wrapper) {
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-sm);
  border: 2px solid var(--color-border-light);
  transition: border-color var(--transition-fast);
}

.search-bar :deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary-lighter);
}

.search-bar :deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

/* User Grid */
.user-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.user-card {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  border: 1px solid rgba(0, 0, 0, 0.04);
  padding: 24px 16px 16px;
  text-align: center;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.user-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.user-card-top {
  cursor: pointer;
}

.uc-name {
  font-size: var(--text-base);
  font-weight: 600;
  margin: 12px 0 6px;
  color: var(--color-text);
}

.uc-campus {
  margin-bottom: 12px;
}

.user-card-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-light);
}

/* Skeleton */
.skeleton-card {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.skeleton-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
}

.skeleton-name {
  width: 80px;
  height: 18px;
  border-radius: 6px;
}

.skeleton-tag {
  width: 60px;
  height: 14px;
  border-radius: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 1024px) { .user-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .user-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .user-grid { grid-template-columns: 1fr; } }
</style>
