<template>
  <div class="product-card" @click="$router.push(`/product/${product.id}`)">
    <div class="card-image">
      <el-image
        :src="product.coverImage || 'https://placehold.co/300x300/f5f7fa/999?text=No+Image'"
        fit="cover"
        class="cover-img"
      >
        <template #error>
          <div class="image-placeholder">
            <el-icon :size="40"><Picture /></el-icon>
          </div>
        </template>
      </el-image>
      <span v-if="product.status !== 'ON_SALE'" class="status-badge">
        {{ product.status === 'SOLD' ? '已售出' : '已下架' }}
      </span>
    </div>
    <div class="card-body">
      <h3 class="title">{{ product.title }}</h3>
      <div class="price-row">
        <span class="price">¥{{ product.price }}</span>
        <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
      </div>
      <div class="meta">
        <span class="seller">{{ product.sellerName }}</span>
        <span class="views">{{ product.viewCount }} 次浏览</span>
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
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.card-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: #f5f7fa;
}

.cover-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
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
  color: #ccc;
}

.status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.card-body {
  padding: 12px;
}

.title {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
  min-height: 40px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 600;
}

.original-price {
  color: #999;
  font-size: 12px;
  text-decoration: line-through;
}

.meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}
</style>
