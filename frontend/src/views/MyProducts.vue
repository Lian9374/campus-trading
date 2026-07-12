<template>
  <Layout>
    <div class="my-products-page">
      <div class="page-header">
        <h2 class="page-title">我的发布</h2>
        <el-button type="primary" @click="$router.push('/publish')">发布商品</el-button>
      </div>

      <div v-if="products.length > 0" class="product-grid">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
      <el-empty v-else description="还没有发布商品">
        <el-button type="primary" @click="$router.push('/publish')">发布第一个商品</el-button>
      </el-empty>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="fetchProducts"
        />
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Layout from '../components/Layout.vue'
import ProductCard from '../components/ProductCard.vue'
import { productApi } from '../api/product'

const products = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

onMounted(() => fetchProducts())

async function fetchProducts(page = 1) {
  currentPage.value = page
  try {
    const data = await productApi.myProducts({ page: page - 1, size: pageSize.value })
    products.value = data.content
    total.value = data.totalElements
  } catch (e) { /* handled */ }
}
</script>

<style scoped>
.my-products-page {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
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
