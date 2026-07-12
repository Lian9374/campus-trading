<template>
  <el-header class="navbar">
    <div class="navbar-left">
      <router-link to="/" class="logo">
        <el-icon :size="24"><ShoppingBag /></el-icon>
        <span>校园二手</span>
      </router-link>
    </div>

    <div class="navbar-center" v-if="showSearch">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品..."
        clearable
        @keyup.enter="doSearch"
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <div class="navbar-right">
      <template v-if="userStore.isLoggedIn">
        <router-link to="/publish" class="nav-btn">
          <el-button type="primary" size="small">发布商品</el-button>
        </router-link>

        <el-dropdown trigger="click">
          <span class="user-dropdown">
            <el-avatar :size="32" :src="userStore.userInfo?.avatar" />
            <span class="username">{{ userStore.userInfo?.nickname }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/my/orders')">我的订单</el-dropdown-item>
              <el-dropdown-item @click="$router.push('/my/products')">我的发布</el-dropdown-item>
              <el-dropdown-item @click="$router.push('/my/favorites')">我的收藏</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template v-else>
        <router-link to="/login" class="nav-link">登录</router-link>
        <router-link to="/register">
          <el-button type="primary" size="small">注册</el-button>
        </router-link>
      </template>
    </div>
  </el-header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchKeyword = ref('')

const showSearch = computed(() => route.name === 'Home')

function doSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'Home', query: { keyword: searchKeyword.value.trim() } })
  }
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
  height: 60px;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
}

.navbar-center {
  flex: 1;
  max-width: 480px;
  margin: 0 24px;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nav-link {
  color: #666;
  font-size: 14px;
}

.nav-link:hover {
  color: #409eff;
}
</style>
