<template>
  <div class="product-card" @click="$router.push(`/product/${product.id}`)">
    <div class="card-image">
      <el-image
        :src="product.coverImage || 'https://placehold.co/400x400/f0fdf4/10b981?text=No+Image'"
        fit="cover"
        class="cover-img"
      >
        <template #error>
          <div class="image-placeholder">
            <el-icon :size="40"><Picture /></el-icon>
          </div>
        </template>
      </el-image>

      <!-- 图片底部渐变遮罩 -->
      <div class="image-overlay"></div>

      <!-- 折扣标签 -->
      <span v-if="product.originalPrice && product.originalPrice > product.price" class="discount-badge">
        {{ Math.round((1 - product.price / product.originalPrice) * 100) }}% OFF
      </span>

      <!-- 状态标签 -->
      <span v-if="product.status !== 'ON_SALE'" class="status-badge">
        {{ product.status === 'SOLD' ? '已售出' : '已下架' }}
      </span>
    </div>
    <div class="card-body">
      <h3 class="title">{{ product.title }}</h3>
      <div class="price-row">
        <span class="price">{{ product.price }}</span>
        <span v-if="product.originalPrice" class="original-price">{{ product.originalPrice }}</span>
      </div>
      <div class="meta">
        <div class="seller-info">
          <span class="seller-avatar">{{ (product.sellerName || '?')[0] }}</span>
          <span class="seller">{{ product.sellerName }}</span>
        </div>
        <span class="views"><el-icon><View /></el-icon> {{ product.viewCount }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  product: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.product-card {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  border: 1px solid rgba(0, 0, 0, 0.06);
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
  border-color: transparent;
}

.product-card:hover .cover-img {
  transform: scale(1.08);
}

.card-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: var(--color-bg-alt);
}

.cover-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.image-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary-lighter);
  background: #f0fdf4;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: linear-gradient(to top, rgba(0,0,0,0.2), transparent);
  pointer-events: none;
  opacity: 0;
  transition: opacity var(--transition-base);
}

.product-card:hover .image-overlay {
  opacity: 1;
}

.discount-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: var(--color-accent);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: var(--radius-sm);
  letter-spacing: 0.02em;
}

.status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(8px);
  color: #fff;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 500;
}

.card-body {
  padding: 14px;
}

.title {
  font-size: var(--text-sm);
  font-weight: 500;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 10px;
  min-height: 42px;
  color: var(--color-text);
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.price {
  color: var(--color-primary-dark);
  font-size: 20px;
  font-weight: 700;
}

.price::before {
  content: '¥';
  font-size: 14px;
  font-weight: 600;
  margin-right: 1px;
}

.original-price {
  color: var(--color-text-muted);
  font-size: 12px;
  text-decoration: line-through;
}

.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--color-text-muted);
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.seller-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--color-primary-lighter);
  color: var(--color-primary-dark);
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.views {
  display: flex;
  align-items: center;
  gap: 3px;
}
</style>
