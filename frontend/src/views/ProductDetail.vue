<template>
  <Layout>
    <div class="detail-page" v-if="product">
      <div class="detail-main">
        <!-- 图片展示 -->
        <div class="detail-gallery">
          <el-image
            :src="currentImage || product.coverImage || 'https://placehold.co/600x600/f5f7fa/999?text=No+Image'"
            fit="contain"
            class="main-image"
          />
          <div class="thumb-list" v-if="allImages.length > 1">
            <div
              v-for="(img, idx) in allImages"
              :key="idx"
              class="thumb-item"
              :class="{ active: currentImage === img }"
              @click="currentImage = img"
            >
              <img :src="img" />
            </div>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="detail-info">
          <h1 class="product-title">{{ product.title }}</h1>
          <div class="price-section">
            <span class="price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
          </div>

          <div class="info-items">
            <div class="info-item">
              <span class="label">分类</span>
              <span>{{ product.categoryName || '未分类' }}</span>
            </div>
            <div class="info-item">
              <span class="label">卖家</span>
              <span>{{ product.sellerName }}</span>
            </div>
            <div class="info-item">
              <span class="label">发布时间</span>
              <span>{{ product.createdAt }}</span>
            </div>
            <div class="info-item">
              <span class="label">浏览</span>
              <span>{{ product.viewCount }} 次</span>
            </div>
            <div class="info-item">
              <span class="label">状态</span>
              <el-tag :type="product.status === 'ON_SALE' ? 'success' : 'info'" size="small">
                {{ product.status === 'ON_SALE' ? '在售' : product.status === 'SOLD' ? '已售出' : '已下架' }}
              </el-tag>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="actions" v-if="userStore.isLoggedIn">
            <template v-if="product.sellerId !== userStore.userInfo?.id && product.status === 'ON_SALE'">
              <el-button type="primary" size="large" @click="showOrderDialog = true">
                <el-icon><ShoppingCart /></el-icon> 立即购买
              </el-button>
              <el-button
                size="large"
                :type="isFavorited ? 'warning' : 'default'"
                @click="toggleFavorite"
              >
                <el-icon><Star /></el-icon>
                {{ isFavorited ? '已收藏' : '收藏' }}
              </el-button>
            </template>
            <template v-else-if="product.sellerId === userStore.userInfo?.id">
              <el-button type="primary" @click="$router.push(`/publish/${product.id}`)">编辑商品</el-button>
              <el-button v-if="product.status === 'ON_SALE'" type="danger" @click="handleRemove">下架商品</el-button>
            </template>
          </div>
        </div>
      </div>

      <!-- 商品描述 -->
      <div class="detail-description">
        <h3>商品描述</h3>
        <div class="description-content">{{ product.description || '卖家很懒，没有写描述...' }}</div>
      </div>

      <!-- 下单弹窗 -->
      <el-dialog v-model="showOrderDialog" title="确认下单" width="420px">
        <div class="order-confirm">
          <p><strong>商品：</strong>{{ product.title }}</p>
          <p><strong>价格：</strong>¥{{ product.price }}</p>
          <p><strong>卖家：</strong>{{ product.sellerName }}</p>
          <el-input
            v-model="orderRemark"
            type="textarea"
            :rows="3"
            placeholder="给卖家留言（选填）"
            maxlength="500"
            show-word-limit
          />
        </div>
        <template #footer>
          <el-button @click="showOrderDialog = false">取消</el-button>
          <el-button type="primary" :loading="ordering" @click="handleOrder">确认下单</el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '../components/Layout.vue'
import { useUserStore } from '../stores/user'
import { productApi } from '../api/product'
import { orderApi } from '../api/order'
import { favoriteApi } from '../api/favorite'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const isFavorited = ref(false)
const showOrderDialog = ref(false)
const orderRemark = ref('')
const ordering = ref(false)
const currentImage = ref('')

const allImages = computed(() => {
  if (!product.value) return []
  const imgs = []
  if (product.value.coverImage) imgs.push(product.value.coverImage)
  if (product.value.images) imgs.push(...product.value.images)
  return imgs
})

onMounted(async () => {
  try {
    product.value = await productApi.detail(route.params.id)
    currentImage.value = product.value.coverImage || ''

    if (userStore.isLoggedIn) {
      try {
        const res = await favoriteApi.check(product.value.id)
        isFavorited.value = res.favorited
      } catch (e) { /* ignore */ }
    }
  } catch (e) {
    router.push('/')
  }
})

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    if (isFavorited.value) {
      await favoriteApi.remove(product.value.id)
      ElMessage.success('已取消收藏')
    } else {
      await favoriteApi.add(product.value.id)
      ElMessage.success('收藏成功')
    }
    isFavorited.value = !isFavorited.value
  } catch (e) { /* handled */ }
}

async function handleOrder() {
  ordering.value = true
  try {
    await orderApi.create({
      productId: product.value.id,
      remark: orderRemark.value
    })
    ElMessage.success('下单成功！')
    showOrderDialog.value = false
    router.push('/my/orders')
  } catch (e) { /* handled */ }
  finally { ordering.value = false }
}

async function handleRemove() {
  try {
    await ElMessageBox.confirm('确定要下架该商品吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await productApi.updateStatus(product.value.id, 'REMOVED')
    ElMessage.success('已下架')
    product.value.status = 'REMOVED'
  } catch (e) { /* cancelled or error */ }
}
</script>

<style scoped>
.detail-page {
  max-width: 1000px;
  margin: 0 auto;
}

.detail-main {
  display: flex;
  gap: 40px;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.detail-gallery {
  flex: 0 0 400px;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  background: #f5f7fa;
}

.thumb-list {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.thumb-item {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
}

.thumb-item.active {
  border-color: #409eff;
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  flex: 1;
}

.product-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 16px;
}

.price-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #fef0f0;
  border-radius: 8px;
}

.price {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-left: 12px;
}

.info-items {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 14px;
}

.info-item .label {
  color: #999;
  width: 80px;
  flex-shrink: 0;
}

.actions {
  display: flex;
  gap: 12px;
}

.detail-description {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.detail-description h3 {
  font-size: 16px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.description-content {
  font-size: 14px;
  line-height: 1.8;
  color: #666;
  white-space: pre-wrap;
}

.order-confirm p {
  margin-bottom: 12px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .detail-main {
    flex-direction: column;
  }
  .detail-gallery {
    flex: none;
  }
}
</style>
