<template>
  <Layout>
    <div class="my-orders-page">
      <h2 class="page-title">我的订单</h2>

      <el-tabs v-model="activeTab" @tab-change="fetchOrders">
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

async function fetchOrders(page = 1) {
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
}

.page-title {
  font-size: 20px;
  margin-bottom: 16px;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f5f5f5;
  margin-bottom: 12px;
}

.order-no {
  font-size: 13px;
  color: #999;
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-product {
  display: flex;
  gap: 12px;
  cursor: pointer;
}

.product-img {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  object-fit: cover;
}

.product-info h4 {
  font-size: 15px;
  margin-bottom: 4px;
}

.amount {
  color: #f56c6c;
  font-size: 16px;
  font-weight: 600;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 13px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
