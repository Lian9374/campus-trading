<template>
  <Layout>
    <div class="home-page">
      <!-- Hero — 全宽 -->
      <section class="hero">
        <div class="hero-inner">
          <h1 class="hero-title">
            让闲置<span class="gradient-text">流转</span>，<br />让价值<span class="gradient-text">延续</span>
          </h1>
          <p class="hero-subtitle">安全、便利的校园二手交易社区</p>

          <!-- 搜索 -->
          <div class="hero-search">
            <el-input
              v-model="keyword"
              placeholder="搜索你想要的二手好物..."
              size="large"
              clearable
              @keyup.enter="doSearch"
              @clear="doSearch"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
              <template #append>
                <el-button type="primary" @click="doSearch">搜索</el-button>
              </template>
            </el-input>
          </div>

          <!-- 分类 icon 网格 -->
          <div class="hero-categories">
            <div
              class="hero-cat-item"
              :class="{ active: selectedCategory === null }"
              @click="selectedCategory = null; doSearch()"
            >
              <div class="hero-cat-icon"><el-icon :size="22"><Grid /></el-icon></div>
              <span>全部</span>
            </div>
            <div
              v-for="cat in categories"
              :key="cat.id"
              class="hero-cat-item"
              :class="{ active: selectedCategory === cat.id }"
              @click="selectedCategory = cat.id; doSearch()"
            >
              <div class="hero-cat-icon">
                <el-icon :size="22"><component :is="catIcons[(cat.id - 1) % catIcons.length]" /></el-icon>
              </div>
              <span>{{ cat.name }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 内容区 — 容器化 -->
      <div class="container">
        <!-- 热门推荐 -->
        <section v-if="hotProducts.length > 0 && !loading && !keyword && selectedCategory === null && !minPrice && !maxPrice" class="section">
          <div class="section-header">
            <h2 class="section-title">热门推荐</h2>
            <span class="section-sub">最受关注的二手好物</span>
          </div>
          <div class="product-grid">
            <div
              v-for="(p, idx) in hotProducts"
              :key="'h'+p.id"
              class="stagger-item"
              :style="{ animationDelay: idx * 0.06 + 's' }"
            >
              <ProductCard :product="p" />
            </div>
          </div>
        </section>

        <!-- 筛选栏 -->
        <div class="filter-row">
          <div class="filter-left">
            <span class="filter-label">价格</span>
            <el-input-number v-model="minPrice" :min="0" placeholder="最低" size="small" @change="doSearch" controls-position="right" />
            <span class="sep">—</span>
            <el-input-number v-model="maxPrice" :min="0" placeholder="最高" size="small" @change="doSearch" controls-position="right" />
            <span class="filter-divider"></span>
            <span class="filter-label">校区</span>
            <el-input v-model="campus" placeholder="输入校区" size="small" clearable @change="doSearch" style="width: 120px" />
          </div>
          <div class="filter-right">
            <span class="filter-label">排序</span>
            <el-select v-model="sortBy" size="small" @change="doSearch" style="width: 130px">
              <el-option label="最新发布" value="newest" />
              <el-option label="价格从低到高" value="price_asc" />
              <el-option label="价格从高到低" value="price_desc" />
            </el-select>
            <span class="result-count" v-if="total > 0">共 {{ total }} 件</span>
          </div>
        </div>

        <!-- 骨架屏 -->
        <div v-if="loading" class="product-grid">
          <div v-for="i in 8" :key="'s'+i" class="skeleton-card">
            <div class="skeleton skeleton-img"></div>
            <div class="skeleton-body">
              <div class="skeleton skeleton-title"></div>
              <div class="skeleton skeleton-price"></div>
              <div class="skeleton skeleton-meta"></div>
            </div>
          </div>
        </div>

        <!-- 全部商品 -->
        <div v-else-if="products.length > 0" class="product-grid">
          <div
            v-for="(p, idx) in products"
            :key="p.id"
            class="stagger-item"
            :style="{ animationDelay: (idx % 8) * 0.06 + 's' }"
          >
            <ProductCard :product="p" />
          </div>
        </div>
        <el-empty v-else description="暂无商品" />

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
const campus = ref('')
const sortBy = ref('newest')
const categories = ref([])
const products = ref([])
const hotProducts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const catIcons = ['Monitor', 'Reading', 'Present', 'ShoppingBag', 'Basketball', 'MoreFilled']

onMounted(async () => {
  if (route.query.keyword) keyword.value = route.query.keyword
  await loadCategories()
  await Promise.all([fetchProducts(), fetchHotProducts()])
})

watch(() => route.query.keyword, (val) => {
  if (val) { keyword.value = val; fetchProducts() }
})

async function loadCategories() {
  try { categories.value = await categoryApi.list() } catch (e) { /* */ }
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
      campus: campus.value || undefined,
      sortBy: sortBy.value || 'newest',
      page: page - 1,
      size: pageSize.value
    })
    products.value = data.content
    total.value = data.totalElements
  } catch (e) { /* */ } finally { loading.value = false }
}

async function fetchHotProducts() {
  try {
    const data = await productApi.list({ page: 0, size: 4 })
    hotProducts.value = data.content
  } catch (e) { /* */ }
}

function doSearch() {
  currentPage.value = 1
  fetchProducts(1)
}
</script>

<style scoped>
/* ===== Hero ===== */
.hero {
  background: linear-gradient(170deg, #ecfdf5 0%, #d1fae5 35%, #a7f3d0 70%, #f0fdf4 100%);
  padding: 52px 24px 40px;
  margin-bottom: 40px;
}

.hero-inner {
  max-width: 900px;
  margin: 0 auto;
  text-align: center;
}

.hero-title {
  font-size: clamp(32px, 5vw, 48px);
  font-weight: 800;
  line-height: 1.25;
  letter-spacing: -0.03em;
  color: var(--color-text);
  margin-bottom: 12px;
}

.hero-subtitle {
  font-size: var(--text-lg);
  color: var(--color-text-secondary);
  margin-bottom: 28px;
}

.hero-search {
  max-width: 520px;
  margin: 0 auto 32px;
}

.hero-search :deep(.el-input__wrapper) {
  border-radius: var(--radius-full);
  border: 2px solid var(--color-primary-lighter);
  box-shadow: var(--shadow-md);
  background: #fff;
}

.hero-search :deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary-light);
}

.hero-search :deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.1);
}

.hero-search :deep(.el-input-group__append) {
  border-radius: 0 var(--radius-full) var(--radius-full) 0;
  background: var(--color-primary);
  border: none;
  padding: 0;
}

.hero-search :deep(.el-input-group__append .el-button) {
  border: none;
  background: transparent;
  color: #fff;
  padding: 8px 24px;
  font-weight: 600;
}

.hero-categories {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-cat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px 4px;
  min-width: 72px;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
}

.hero-cat-item:hover {
  background: rgba(255, 255, 255, 0.7);
  color: var(--color-primary);
}

.hero-cat-item.active {
  color: var(--color-primary-dark);
}

.hero-cat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}

.hero-cat-item:hover .hero-cat-icon,
.hero-cat-item.active .hero-cat-icon {
  background: #fff;
  color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

/* ===== Container ===== */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* ===== Sections ===== */
.section {
  margin-bottom: 36px;
}

.section-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 20px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
}

.section-sub {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

/* ===== Filter ===== */
.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border-light);
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  margin-right: 4px;
}

.sep { color: var(--color-text-muted); }

.filter-divider {
  width: 1px;
  height: 20px;
  background: var(--color-border-light);
  margin: 0 4px;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-count {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

/* ===== Grid ===== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1024px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
  .hero { padding: 36px 16px 28px; }
}
@media (max-width: 480px) { .product-grid { grid-template-columns: 1fr; } }

/* ===== Skeleton ===== */
.skeleton-card {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  overflow: hidden;
}
.skeleton-img { width: 100%; padding-top: 100%; border-radius: 0; }
.skeleton-body { padding: 14px; }
.skeleton-title { height: 18px; width: 85%; margin-bottom: 10px; }
.skeleton-price { height: 24px; width: 45%; margin-bottom: 10px; }
.skeleton-meta { height: 14px; width: 60%; }

/* ===== Pagination ===== */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 36px;
  padding-bottom: 16px;
}
</style>
