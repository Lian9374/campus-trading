<template>
  <Layout>
    <div class="home-page">
      <!-- 搜索区域 -->
      <div class="search-section">
        <el-input
          v-model="keyword"
          placeholder="搜索你想买的二手商品..."
          size="large"
          clearable
          @keyup.enter="doSearch"
          @clear="doSearch"
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button type="primary" @click="doSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 分类筛选 -->
      <div class="category-bar">
        <el-radio-group v-model="selectedCategory" @change="doSearch" size="small">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button v-for="cat in categories" :key="cat.id" :value="cat.id">
            {{ cat.name }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <!-- 价格筛选 -->
      <div class="filter-bar">
        <span class="filter-label">价格：</span>
        <el-input-number v-model="minPrice" :min="0" placeholder="最低价" size="small" @change="doSearch" />
        <span class="filter-sep">—</span>
        <el-input-number v-model="maxPrice" :min="0" placeholder="最高价" size="small" @change="doSearch" />
      </div>

      <!-- 商品列表 -->
      <div v-if="products.length > 0" class="product-grid">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
      <el-empty v-else-if="!loading" description="暂无商品" />

      <!-- 分页 -->
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
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import Layout from '../components/Layout.vue'
import ProductCard from '../components/ProductCard.vue'
import { productApi, categoryApi } from '../api/product'

const route = useRoute()

const keyword = ref('')
const selectedCategory = ref(null)
const minPrice = ref(null)
const maxPrice = ref(null)
const categories = ref([])
const products = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

onMounted(async () => {
  // 从 URL 参数读取 keyword
  if (route.query.keyword) {
    keyword.value = route.query.keyword
  }
  await loadCategories()
  await fetchProducts()
})

// 监听路由变化（搜索）
watch(() => route.query.keyword, (val) => {
  if (val) {
    keyword.value = val
    fetchProducts()
  }
})

async function loadCategories() {
  try {
    categories.value = await categoryApi.list()
  } catch (e) { /* error handled */ }
}

async function fetchProducts(page = 1) {
  currentPage.value = page
  loading.value = true
  try {
    const data = await productApi.list({
      keyword: keyword.value || undefined,
      categoryId: selectedCategory.value,
      minPrice: minPrice.value,
      maxPrice: maxPrice.value,
      page: page - 1,
      size: pageSize.value
    })
    products.value = data.content
    total.value = data.totalElements
  } catch (e) { /* error handled */ }
  finally { loading.value = false }
}

function doSearch() {
  currentPage.value = 1
  fetchProducts(1)
}
</script>

<style scoped>
.search-section {
  margin-bottom: 20px;
}

.search-input {
  max-width: 640px;
  margin: 0 auto;
}

.category-bar {
  margin-bottom: 16px;
  display: flex;
  justify-content: center;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
}

.filter-label {
  font-size: 14px;
  color: #666;
}

.filter-sep {
  color: #999;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
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

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 40px;
}
</style>
