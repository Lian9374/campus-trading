<template>
  <Layout>
    <div class="profile-page" v-if="profile">
      <!-- 头部卡片 -->
      <div class="profile-header">
        <el-avatar :size="72" :src="profile.avatar" class="profile-avatar">
          {{ (profile.nickname || '?')[0] }}
        </el-avatar>
        <h1 class="profile-name">{{ profile.nickname }}</h1>
        <el-tag v-if="profile.campus" size="small" class="campus-tag">
          <el-icon><Location /></el-icon> {{ profile.campus }}
        </el-tag>
        <div class="profile-rating" v-if="profile.ratingCount > 0">
          <el-rate :model-value="Math.round(profile.ratingAvg)" disabled show-score :max="5" size="small" />
          <span class="rating-text">{{ profile.ratingAvg?.toFixed(1) }} ({{ profile.ratingCount }}条评价)</span>
        </div>
        <span class="no-rating" v-else>暂无评价</span>

        <!-- 关注按钮 -->
        <div class="profile-actions" v-if="userStore.isLoggedIn && userStore.userInfo?.id !== profile.id">
          <el-button
            :type="isFollowing ? 'default' : 'primary'"
            size="small"
            @click="toggleFollow"
          >
            {{ isFollowing ? '已关注' : '+ 关注' }}
          </el-button>
          <el-button size="small" plain @click="goToChat">
            <el-icon><ChatDotRound /></el-icon> 发私信
          </el-button>
          <el-button text type="danger" size="small" @click="showReportDialog = true">
            <el-icon><WarningFilled /></el-icon> 举报
          </el-button>
        </div>
        <!-- 编辑资料（本人） -->
        <div class="profile-actions" v-if="userStore.isLoggedIn && userStore.userInfo?.id == profile.id">
          <el-button type="primary" size="small" @click="showEditDialog = true">
            <el-icon><Edit /></el-icon> 编辑资料
          </el-button>
        </div>
      </div>

      <!-- 统计 -->
      <div class="profile-stats">
        <div class="stat-item" @click="activeTab = 'products'">
          <strong>{{ profile.productCount }}</strong>
          <span>在售</span>
        </div>
        <div class="stat-item" @click="activeTab = 'reviews'">
          <strong>{{ profile.soldCount }}</strong>
          <span>已售</span>
        </div>
        <div class="stat-item" @click="activeTab = 'followers'">
          <strong>{{ followStats.followers }}</strong>
          <span>粉丝</span>
        </div>
        <div class="stat-item" @click="activeTab = 'following'">
          <strong>{{ followStats.following }}</strong>
          <span>关注</span>
        </div>
      </div>

      <!-- Tab 内容 -->
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane label="在售商品" name="products">
          <div class="product-grid" v-if="products.length > 0">
            <ProductCard v-for="p in products" :key="p.id" :product="p" />
          </div>
          <el-empty v-else description="暂无在售商品" />
        </el-tab-pane>
        <el-tab-pane label="买家评价" name="reviews">
          <div class="review-list" v-if="reviews.length > 0">
            <div v-for="r in reviews" :key="r.id" class="review-item">
              <div class="review-header">
                <span class="review-author">{{ r.reviewerName }}</span>
                <el-rate :model-value="r.rating" disabled size="small" />
                <span class="review-date">{{ r.createdAt?.slice(0, 10) }}</span>
              </div>
              <p class="review-content" v-if="r.content">{{ r.content }}</p>
              <span class="review-product" v-if="r.productTitle">商品：{{ r.productTitle }}</span>
            </div>
            <el-pagination
              v-if="reviewTotal > 5"
              v-model:current-page="reviewPage"
              :page-size="5"
              :total="reviewTotal"
              layout="prev, pager, next"
              small
              background
              class="review-pagination"
              @current-change="loadReviews"
            />
          </div>
          <el-empty v-else description="暂无评价" />
        </el-tab-pane>
      </el-tabs>

      <!-- 举报弹窗 -->
      <el-dialog v-model="showReportDialog" title="举报用户" width="400px">
        <el-form :model="reportForm" label-width="80px">
          <el-form-item label="举报原因">
            <el-select v-model="reportForm.reason" placeholder="请选择举报原因">
              <el-option label="虚假身份" value="虚假身份" />
              <el-option label="骚扰行为" value="骚扰行为" />
              <el-option label="违规交易" value="违规交易" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          <el-form-item label="详细说明">
            <el-input v-model="reportForm.detail" type="textarea" :rows="3" placeholder="请描述具体问题（选填）" maxlength="500" show-word-limit />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showReportDialog = false">取消</el-button>
          <el-button type="danger" :loading="reporting" @click="handleReportUser">提交举报</el-button>
        </template>
      </el-dialog>

      <!-- 编辑资料弹窗 -->
      <el-dialog v-model="showEditDialog" title="编辑个人资料" width="440px">
        <div class="edit-avatar-section">
          <el-avatar :size="80" :src="editForm.avatar" class="edit-avatar">
            {{ (editForm.nickname || '?')[0] }}
          </el-avatar>
          <el-upload
            :show-file-list="false"
            :before-upload="handleAvatarUpload"
            accept="image/jpeg,image/png,image/gif,image/webp"
            action="#"
          >
            <el-button size="small" type="primary" plain>更换头像</el-button>
          </el-upload>
        </div>
        <el-form :model="editForm" label-width="70px" style="margin-top: 20px">
          <el-form-item label="昵称">
            <el-input v-model="editForm.nickname" placeholder="你的昵称" maxlength="50" />
          </el-form-item>
          <el-form-item label="校区">
            <el-input v-model="editForm.campus" placeholder="如：仙林校区" maxlength="100" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="editForm.phone" placeholder="选填" maxlength="20" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from '../components/Layout.vue'
import ProductCard from '../components/ProductCard.vue'
import { useUserStore } from '../stores/user'
import { userApi } from '../api/user'
import { productApi } from '../api/product'
import { reviewApi } from '../api/review'
import { reportApi } from '../api/report'
import { followApi } from '../api/follow'

const route = useRoute()
const userStore = useUserStore()

const profile = ref(null)
const products = ref([])
const reviews = ref([])
const reviewTotal = ref(0)
const reviewPage = ref(1)
const activeTab = ref('products')
const isFollowing = ref(false)
const followStats = ref({ followers: 0, following: 0 })

// 编辑资料
const showEditDialog = ref(false)
const saving = ref(false)
const editForm = ref({ nickname: '', campus: '', phone: '', avatar: '' })

// 举报
const showReportDialog = ref(false)
const reporting = ref(false)
const reportForm = ref({ reason: '', detail: '' })

onMounted(async () => {
  const userId = route.params.id
  try {
    profile.value = await userApi.getProfile(userId)

    loadUserProducts(userId)
    loadReviews()
    loadFollowStats(userId)

    if (userStore.isLoggedIn && userStore.userInfo?.id != userId) {
      checkFollowStatus(userId)
    }
  } catch (e) {
    ElMessage.error('用户不存在')
  }
})

async function loadUserProducts(userId) {
  try {
    const data = await productApi.list({ page: 0, size: 50 })
    products.value = data.content.filter(p => p.sellerId == userId)
  } catch (e) { /* ignore */ }
}

async function loadReviews(page = 1) {
  reviewPage.value = page
  try {
    const data = await reviewApi.getUserReviews(route.params.id, { page: page - 1, size: 5 })
    reviews.value = data.content
    reviewTotal.value = data.totalElements
  } catch (e) { /* ignore */ }
}

async function loadFollowStats(userId) {
  try {
    const data = await followApi.stats(userId)
    followStats.value = data
  } catch (e) { /* ignore */ }
}

async function checkFollowStatus(userId) {
  try {
    const data = await followApi.check(userId)
    isFollowing.value = data.following
  } catch (e) { /* ignore */ }
}

async function toggleFollow() {
  const userId = route.params.id
  try {
    if (isFollowing.value) {
      await followApi.unfollow(userId)
      isFollowing.value = false
      followStats.value.followers--
      ElMessage.success('已取消关注')
    } else {
      await followApi.follow(userId)
      isFollowing.value = true
      followStats.value.followers++
      ElMessage.success('关注成功')
    }
  } catch (e) { /* handled */ }
}

// 编辑资料 — 弹出时初始化表单
watch(showEditDialog, (val) => {
  if (val && userStore.userInfo) {
    editForm.value = {
      nickname: userStore.userInfo.nickname || '',
      campus: userStore.userInfo.campus || '',
      phone: userStore.userInfo.phone || '',
      avatar: userStore.userInfo.avatar || ''
    }
  }
})

async function handleAvatarUpload(file) {
  try {
    const url = await productApi.uploadImage(file)
    editForm.value.avatar = url
    ElMessage.success('头像上传成功')
  } catch (e) { /* handled */ }
  return false // 阻止默认上传行为
}

async function saveProfile() {
  saving.value = true
  try {
    await userStore.updateUserInfo({
      nickname: editForm.value.nickname,
      campus: editForm.value.campus,
      phone: editForm.value.phone,
      avatar: editForm.value.avatar
    })
    // 更新本地 profile
    if (profile.value) {
      profile.value.nickname = editForm.value.nickname
      profile.value.campus = editForm.value.campus
      profile.value.avatar = editForm.value.avatar
    }
    ElMessage.success('资料已更新')
    showEditDialog.value = false
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

function goToChat() {
  router.push(`/messages?receiverId=${profile.value.id}`)
}

async function handleReportUser() {
  if (!reportForm.value.reason) {
    ElMessage.warning('请选择举报原因')
    return
  }
  reporting.value = true
  try {
    await reportApi.create({
      productId: null,
      reason: reportForm.value.reason,
      detail: reportForm.value.detail
    })
    ElMessage.success('举报已提交')
    showReportDialog.value = false
    reportForm.value = { reason: '', detail: '' }
  } catch (e) { /* handled */ }
  finally { reporting.value = false }
}
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 20px;
}

.profile-header {
  text-align: center;
  padding-bottom: 24px;
}

.profile-avatar {
  box-shadow: 0 0 0 4px var(--color-primary-lighter);
  margin-bottom: 14px;
}

.profile-avatar :deep(img) {
  object-fit: cover;
}

.profile-name {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 8px;
}

.campus-tag {
  margin-bottom: 10px;
}

.profile-rating {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}

.rating-text {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.no-rating {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  display: block;
  margin-bottom: 12px;
}

.profile-actions {
  margin-top: 12px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

/* 统计 */
.profile-stats {
  display: flex;
  justify-content: center;
  gap: 32px;
  padding: 20px 0;
  margin-bottom: 20px;
  background: var(--color-bg-alt);
  border-radius: var(--radius-md);
}

.stat-item {
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.stat-item:hover {
  color: var(--color-primary);
}

.stat-item strong {
  display: block;
  font-size: 24px;
  color: var(--color-primary-dark);
  font-weight: 700;
}

.stat-item span {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

/* Tabs */
.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

/* Product Grid */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.edit-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  margin-bottom: 8px;
}

.edit-avatar {
  box-shadow: 0 0 0 3px var(--color-primary-lighter);
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}

/* Reviews */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.review-author {
  font-size: var(--text-sm);
  font-weight: 600;
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
  margin-bottom: 4px;
}

.review-product {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.review-pagination {
  justify-content: center;
  margin-top: 20px;
}
</style>
