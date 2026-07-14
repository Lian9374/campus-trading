<template>
  <Layout>
    <div class="follow-page">
      <h2 class="page-title">我的关注</h2>
      <div class="user-list" v-if="users.length > 0">
        <div v-for="u in users" :key="u.userId" class="user-item" @click="$router.push(`/user/${u.userId}`)">
          <el-avatar :size="44" :src="u.avatar">{{ (u.nickname || '?')[0] }}</el-avatar>
          <div class="user-body">
            <span class="user-name">{{ u.nickname }}</span>
            <span class="user-campus" v-if="u.campus">{{ u.campus }}</span>
          </div>
          <div class="user-meta">
            <span>{{ u.productCount }} 在售</span>
          </div>
        </div>
        <el-pagination
          v-if="total > pageSize"
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          small background
          class="pagination"
          @current-change="fetchData"
        />
      </div>
      <el-empty v-else description="还没有关注任何人" />
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Layout from '../components/Layout.vue'
import { useUserStore } from '../stores/user'
import { followApi } from '../api/follow'

const userStore = useUserStore()
const users = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

onMounted(() => fetchData())

async function fetchData(page = 1) {
  currentPage.value = page
  if (!userStore.userInfo) return
  try {
    const data = await followApi.following(userStore.userInfo.id, { page: page - 1, size: pageSize.value })
    users.value = data.content
    total.value = data.totalElements
  } catch (e) { /* */ }
}
</script>

<style scoped>
.follow-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.user-item:hover {
  background: var(--color-bg-alt);
}

.user-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text);
}

.user-campus {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.user-meta {
  font-size: var(--text-xs);
  color: var(--color-primary);
}

.pagination {
  justify-content: center;
  margin-top: 24px;
}
</style>
