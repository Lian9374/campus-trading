<template>
  <Layout>
    <div class="my-orders-page">
      <h2 class="page-title">我的订单</h2>

      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="我买到的" name="buyer" />
        <el-tab-pane label="我卖出的" name="seller" />
      </el-tabs>

      <div v-if="orders.length > 0" class="order-list">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-header">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <el-tag :type="statusTag(order.status)" size="small">{{ statusText(order.status) }}</el-tag>
          </div>
          <div class="order-body">
            <div class="order-product" @click="$router.push(`/product/${order.productId}`)">
              <img :src="order.productCover || 'https://placehold.co/80x80/f5f7fa/999?text=No+Image'" class="product-img" />
              <div class="product-info">
                <h4>{{ order.productTitle }}</h4>
                <span class="amount">¥{{ order.amount }}</span>
              </div>
            </div>
            <div class="order-actions" v-if="userStore.userInfo">
              <!-- 买家视角：PENDING 可取消 -->
              <el-button
                v-if="order.status === 'PENDING' && order.buyerId === userStore.userInfo.id"
                type="danger" size="small" @click="handleCancel(order)"
              >
                取消订单
              </el-button>
              <!-- 卖家视角：PENDING 可确认 -->
              <el-button
                v-if="order.status === 'PENDING' && order.sellerId === userStore.userInfo.id"
                type="primary" size="small" @click="handleConfirm(order)"
              >
                确认订单
              </el-button>
              <!-- 买家视角：CONFIRMED 可确认收货 -->
              <el-button
                v-if="order.status === 'CONFIRMED' && order.buyerId === userStore.userInfo.id"
                type="success" size="small" @click="handleComplete(order)"
              >
                确认收货
              </el-button>
              <!-- 联系对方 -->
              <el-button
                size="small" plain
                @click="$router.push(`/messages?productId=${order.productId}&receiverId=${order.buyerId === userStore.userInfo?.id ? order.sellerId : order.buyerId}`)"
              >
                <el-icon><ChatDotRound /></el-icon> 联系{{ order.buyerId === userStore.userInfo?.id ? '卖家' : '买家' }}
              </el-button>
            </div>
          </div>
          <div class="order-footer">
            <span>{{ order.buyerName }} 向 {{ order.sellerName }} 购买</span>
            <span class="order-time">{{ order.createdAt }}</span>
          </div>
        </div>

        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            background
            @current-change="fetchOrders"
          />
        </div>
      </div>
      <el-empty v-else description="暂无订单" />
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import Layout from '../components/Layout.vue'
import { useUserStore } from '../stores/user'
import { orderApi } from '../api/order'

const userStore = useUserStore()
const activeTab = ref('all')
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => fetchOrders())

function onTabChange() {
  fetchOrders(1)
}

async function fetchOrders(page = 1) {
  // 防止 tab-change 传入 tab name 字符串
  if (typeof page !== 'number') page = 1
  currentPage.value = page
  try {
    const data = await orderApi.myOrders({
      role: activeTab.value,
      page: page - 1,
      size: pageSize.value
    })
    orders.value = data.content
    total.value = data.totalElements
  } catch (e) { /* handled */ }
}

function statusTag(status) {
  const map = { PENDING: 'warning', CONFIRMED: 'primary', COMPLETED: 'success', CANCELLED: 'info' }
  return map[status] || 'info'
}

function statusText(status) {
  const map = { PENDING: '待确认', CONFIRMED: '已确认', COMPLETED: '已完成', CANCELLED: '已取消' }
  return map[status] || status
}

async function handleCancel(order) {
  try {
    await orderApi.cancel(order.id)
    ElMessage.success('订单已取消')
    fetchOrders(currentPage.value)
  } catch (e) { /* handled */ }
}

async function handleConfirm(order) {
  try {
    await orderApi.confirm(order.id)
    ElMessage.success('已确认订单')
    fetchOrders(currentPage.value)
  } catch (e) { /* handled */ }
}

async function handleComplete(order) {
  try {
    await orderApi.complete(order.id)
    ElMessage.success('已确认收货，交易完成！')
    fetchOrders(currentPage.value)
  } catch (e) { /* handled */ }
}
</script>

<style scoped>
.my-orders-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
}

.order-card {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  border: 1px solid rgba(0, 0, 0, 0.04);
  padding: 18px 20px;
  margin-bottom: 14px;
  transition: box-shadow var(--transition-fast);
}

.order-card:hover {
  box-shadow: var(--shadow-md);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: 14px;
}

.order-no {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  font-family: var(--font-mono);
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-product {
  display: flex;
  gap: 14px;
  cursor: pointer;
}

.product-img {
  width: 68px;
  height: 68px;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.product-info h4 {
  font-size: var(--text-base);
  font-weight: 500;
  margin-bottom: 4px;
  color: var(--color-text);
}

.amount {
  color: var(--color-primary-dark);
  font-size: 18px;
  font-weight: 700;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 14px;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
