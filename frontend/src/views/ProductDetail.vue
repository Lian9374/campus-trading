<template>
  <Layout>
    <div class="detail-page" v-if="product">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <router-link to="/">首页</router-link>
        <el-icon><ArrowRight /></el-icon>
        <span v-if="product.categoryName">{{ product.categoryName }}</span>
        <span v-else>全部商品</span>
        <el-icon><ArrowRight /></el-icon>
        <span class="current">{{ product.title }}</span>
      </div>

      <!-- 主内容：图 | 信息 | 卖家 -->
      <div class="detail-main">
        <!-- 图片 -->
        <div class="detail-gallery">
          <div class="main-image-wrap">
            <el-image
              :src="currentImage || product.coverImage || 'https://placehold.co/500x500/f0fdf4/10b981?text=No+Image'"
              fit="contain"
              class="main-image"
            />
          </div>
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
            <span class="price">{{ product.price }}</span>
            <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">{{ product.originalPrice }}</span>
            <el-tag v-if="product.originalPrice && product.originalPrice > product.price" type="warning" size="small" effect="plain">
              省{{ (product.originalPrice - product.price).toFixed(2) }}
            </el-tag>
          </div>

          <div class="info-list">
            <div class="info-row">
              <span class="label">分类</span>
              <span>{{ product.categoryName || '未分类' }}</span>
            </div>
            <div class="info-row">
              <span class="label">发布时间</span>
              <span>{{ product.createdAt }}</span>
            </div>
            <div class="info-row">
              <span class="label">浏览</span>
              <span>{{ product.viewCount }} 次</span>
            </div>
            <div class="info-row">
              <span class="label">状态</span>
              <el-tag :type="product.status === 'ON_SALE' ? 'success' : 'info'" size="small">
                {{ statusText }}
              </el-tag>
            </div>
          </div>

          <div class="actions" v-if="userStore.isLoggedIn">
            <template v-if="product.sellerId !== userStore.userInfo?.id && product.status === 'ON_SALE'">
              <el-button type="primary" size="large" class="btn-buy" @click="showOrderDialog = true">
                立即购买
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
          <div class="actions" v-else>
            <el-button type="primary" size="large" @click="$router.push('/login')">登录后购买</el-button>
          </div>
        </div>

        <!-- 卖家卡片 -->
        <div class="seller-sidebar">
          <div class="seller-card">
            <div class="seller-avatar">
              <el-avatar :size="56">{{ (product.sellerName || '?')[0] }}</el-avatar>
            </div>
            <h4 class="seller-name">{{ product.sellerName }}</h4>
            <div class="seller-rating" v-if="sellerRating > 0">
              <el-rate :model-value="sellerRating" disabled show-score :max="5" size="small" />
              <span class="rating-count">({{ sellerReviewCount }}条)</span>
            </div>
            <p class="seller-bio" v-else>校园二手认证卖家</p>
            <div class="seller-stats">
              <div class="stat"><strong>{{ sellerProductCount }}</strong><span>在售</span></div>
            </div>
            <el-button plain size="small" class="seller-btn" @click="$router.push('/')">
              查看全部商品
            </el-button>
          </div>
          <!-- 举报 -->
          <div class="report-box" v-if="userStore.isLoggedIn && userStore.userInfo?.id !== product.sellerId">
            <el-button text type="danger" size="small" @click="showReportDialog = true">
              <el-icon><WarningFilled /></el-icon> 举报商品
            </el-button>
          </div>
        </div>
      </div>

      <!-- 商品描述 -->
      <div class="detail-description">
        <h3>商品描述</h3>
        <div class="description-content">{{ product.description || '卖家很懒，没有写描述...' }}</div>
      </div>

      <!-- 卖家评价 -->
      <div class="detail-reviews" v-if="reviews.length > 0">
        <h3>卖家评价 <span class="review-count">({{ reviewTotal }})</span></h3>
        <div class="review-list">
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <div class="review-header">
              <span class="review-author">{{ r.reviewerName }}</span>
              <el-rate :model-value="r.rating" disabled size="small" />
              <span class="review-date">{{ r.createdAt?.slice(0, 10) }}</span>
            </div>
            <p class="review-content" v-if="r.content">{{ r.content }}</p>
          </div>
        </div>
      </div>

      <!-- 下单弹窗 -->
      <el-dialog v-model="showOrderDialog" title="确认下单" width="420px">
        <div class="order-confirm">
          <p><strong>商品：</strong>{{ product.title }}</p>
          <p><strong>价格：</strong>¥{{ product.price }}</p>
          <p><strong>卖家：</strong>{{ product.sellerName }}</p>
          <el-input v-model="orderRemark" type="textarea" :rows="3" placeholder="给卖家留言（选填）" maxlength="500" show-word-limit />
        </div>
        <template #footer>
          <el-button @click="showOrderDialog = false">取消</el-button>
          <el-button type="primary" :loading="ordering" @click="handleOrder">确认下单</el-button>
        </template>
      </el-dialog>

      <!-- 举报弹窗 -->
      <el-dialog v-model="showReportDialog" title="举报商品" width="420px">
        <el-form :model="reportForm" label-width="80px">
          <el-form-item label="举报原因">
            <el-select v-model="reportForm.reason" placeholder="请选择举报原因">
              <el-option label="虚假信息" value="虚假信息" />
              <el-option label="违禁品" value="违禁品" />
              <el-option label="侵权" value="侵权" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="详细说明">
            <el-input v-model="reportForm.detail" type="textarea" :rows="3" placeholder="请描述具体问题（选填）" maxlength="500" show-word-limit />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showReportDialog = false">取消</el-button>
          <el-button type="danger" :loading="reporting" @click="handleReport">提交举报</el-button>
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
import { reviewApi } from '../api/review'
import { reportApi } from '../api/report'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const isFavorited = ref(false)
const showOrderDialog = ref(false)
const orderRemark = ref('')
const ordering = ref(false)
const currentImage = ref('')
const sellerProductCount = ref(0)

// 评价
const reviews = ref([])
const reviewTotal = ref(0)
const sellerRating = ref(0)
const sellerReviewCount = ref(0)

// 举报
const showReportDialog = ref(false)
const reporting = ref(false)
const reportForm = ref({ reason: '', detail: '' })

const allImages = computed(() => {
  if (!product.value) return []
  const imgs = []
  if (product.value.coverImage) imgs.push(product.value.coverImage)
  if (product.value.images) imgs.push(...product.value.images)
  return imgs
})

const statusText = computed(() => {
  const map = { ON_SALE: '在售', SOLD: '已售出', REMOVED: '已下架' }
  return map[product.value?.status] || product.value?.status
})

onMounted(async () => {
  try {
    product.value = await productApi.detail(route.params.id)
    currentImage.value = product.value.coverImage || ''

    // 加载卖家评价
    if (product.value.sellerId) {
      loadSellerReviews()
    }

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

async function loadSellerReviews() {
  try {
    const data = await reviewApi.getUserReviews(product.value.sellerId, { page: 0, size: 5 })
    reviews.value = data.content
    reviewTotal.value = data.totalElements
    // 从评价计算评分
    if (data.totalElements > 0) {
      // 取所有评价算平均分
      const allData = await reviewApi.getUserReviews(product.value.sellerId, { page: 0, size: Math.min(data.totalElements, 100) })
      const ratings = allData.content.map(r => r.rating)
      sellerRating.value = Math.round(ratings.reduce((a, b) => a + b, 0) / ratings.length * 10) / 10
      sellerReviewCount.value = ratings.length
    }
  } catch (e) { /* ignore */ }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
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
    await orderApi.create({ productId: product.value.id, remark: orderRemark.value })
    ElMessage.success('下单成功！')
    showOrderDialog.value = false
    router.push('/my/orders')
  } catch (e) { /* handled */ }
  finally { ordering.value = false }
}

async function handleRemove() {
  try {
    await ElMessageBox.confirm('确定要下架该商品吗？', '确认操作', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await productApi.updateStatus(product.value.id, 'REMOVED')
    ElMessage.success('已下架')
    product.value.status = 'REMOVED'
  } catch (e) { /* cancelled */ }
}

async function handleReport() {
  if (!reportForm.value.reason) {
    ElMessage.warning('请选择举报原因')
    return
  }
  reporting.value = true
  try {
    await reportApi.create({
      productId: product.value.id,
      reason: reportForm.value.reason,
      detail: reportForm.value.detail
    })
    ElMessage.success('举报已提交，我们会尽快处理')
    showReportDialog.value = false
    reportForm.value = { reason: '', detail: '' }
  } catch (e) { /* handled */ }
  finally { reporting.value = false }
}
</script>

<style scoped>
.detail-page {
  max-width: 1100px;
  margin: 0 auto;
}

/* ===== Breadcrumb ===== */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 0 20px;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.breadcrumb a {
  color: var(--color-text-muted);
  transition: color var(--transition-fast);
}

.breadcrumb a:hover { color: var(--color-primary); }
.breadcrumb .current { color: var(--color-text); font-weight: 500; }

/* ===== Main ===== */
.detail-main {
  display: grid;
  grid-template-columns: 390px 1fr 200px;
  gap: 32px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(0, 0, 0, 0.04);
  padding: 28px;
  margin-bottom: 24px;
}

/* Gallery */
.main-image-wrap {
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-bg-alt);
}

.main-image { width: 100%; height: 390px; }

.thumb-list {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.thumb-item {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color var(--transition-fast);
}

.thumb-item:hover { border-color: var(--color-primary-lighter); }
.thumb-item.active { border-color: var(--color-primary); }
.thumb-item img { width: 100%; height: 100%; object-fit: cover; }

/* Info */
.product-title {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.45;
  margin-bottom: 18px;
  color: var(--color-text);
}

.price-section {
  margin-bottom: 20px;
  padding: 16px 18px;
  background: linear-gradient(135deg, #ecfdf5, #f0fdf4);
  border-radius: var(--radius-md);
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.price { font-size: 30px; font-weight: 800; color: var(--color-primary-dark); }
.price::before { content: '¥'; font-size: 16px; font-weight: 600; }

.original-price {
  font-size: 14px;
  color: var(--color-text-muted);
  text-decoration: line-through;
}

.info-list {
  margin-bottom: 24px;
  background: var(--color-bg-alt);
  border-radius: var(--radius-sm);
  padding: 4px 14px;
}

.info-row {
  display: flex;
  padding: 9px 0;
  border-bottom: 1px solid var(--color-border-light);
  font-size: var(--text-sm);
}

.info-row:last-child { border-bottom: none; }
.info-row .label { color: var(--color-text-muted); width: 72px; flex-shrink: 0; }
.info-row .label + span { color: var(--color-text); font-weight: 500; }

.actions { display: flex; gap: 10px; flex-wrap: wrap; }
.actions .el-button { border-radius: var(--radius-sm); font-weight: 600; }
.btn-buy { padding: 12px 32px; }

/* Seller sidebar */
.seller-sidebar {
  display: flex;
  flex-direction: column;
}

.seller-card {
  background: var(--color-bg-alt);
  border-radius: var(--radius-md);
  padding: 24px 16px;
  text-align: center;
  border: 1px solid var(--color-border-light);
}

.seller-avatar { margin-bottom: 12px; }
.seller-avatar :deep(.el-avatar) { background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary)); }

.seller-name { font-size: var(--text-base); font-weight: 600; margin-bottom: 4px; }
.seller-bio { font-size: var(--text-xs); color: var(--color-text-muted); margin-bottom: 14px; }

.seller-stats {
  display: flex;
  justify-content: center;
  margin-bottom: 14px;
}

.stat { text-align: center; }
.stat strong { display: block; font-size: 18px; color: var(--color-primary-dark); }
.stat span { font-size: 11px; color: var(--color-text-muted); }

.seller-btn { width: 100%; }

/* Description */
.detail-description {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(0, 0, 0, 0.04);
  padding: 28px;
}

.detail-description h3 {
  font-size: var(--text-lg);
  font-weight: 600;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 2px solid var(--color-border-light);
}

.description-content {
  font-size: var(--text-sm);
  line-height: 1.9;
  color: var(--color-text-secondary);
  white-space: pre-wrap;
}

.order-confirm p { margin-bottom: 12px; font-size: var(--text-sm); }

/* === Reviews === */
.detail-reviews {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(0, 0, 0, 0.04);
  padding: 28px;
  margin-bottom: 24px;
}

.detail-reviews h3 {
  font-size: var(--text-lg);
  font-weight: 600;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 2px solid var(--color-border-light);
}

.review-count {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  font-weight: 400;
}

.review-item {
  padding: 14px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.review-item:last-child { border-bottom: none; }

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.review-author {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text);
}

.review-date {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  margin-left: auto;
}

.review-content {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  line-height: 1.6;
  padding-left: 2px;
}

/* === Seller Rating === */
.seller-rating {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  margin-bottom: 10px;
}

.rating-count {
  font-size: 11px;
  color: var(--color-text-muted);
}

/* === Report === */
.report-box {
  margin-top: 10px;
  text-align: center;
}

@media (max-width: 1024px) {
  .detail-main { grid-template-columns: 1fr; gap: 24px; }
  .main-image { height: 300px; }
  .seller-sidebar { display: none; }
}

@media (max-width: 768px) {
  .detail-main { padding: 16px; }
}
</style>
