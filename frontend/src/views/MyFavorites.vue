<template>
  <Layout>
    <div class="my-favorites-page">
      <h2 class="page-title">我的收藏</h2>

      <div v-if="products.length > 0" class="product-grid">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
      <el-empty v-else description="还没有收藏商品">
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </el-empty>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="fetchFavorites"
        />
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Layout from '../components/Layout.vue'
import ProductCard from '../components/ProductCard.vue'
import { favoriteApi } from '../api/favorite'

const products = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

onMounted(() => fetchFavorites())

async function fetchFavorites(page = 1) {
  currentPage.value = page
  try {
    const data = await favoriteApi.list({ page: page - 1, size: pageSize.value })
    products.value = data.content
    total.value = data.totalElements
  } catch (e) { /* handled */ }
}
</script>

<style scoped>
.my-favorites-page {
  max-width: 1000px;
  margin: 0 auto;
}

.page-title {
  font-size: 20px;
  margin-bottom: 20px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1024px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}

@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
