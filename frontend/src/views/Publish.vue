<template>
  <Layout>
    <div class="publish-page">
      <h2 class="page-title">{{ isEdit ? '编辑商品' : '发布商品' }}</h2>
      <div class="form-card">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <el-form-item label="商品标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入商品标题" maxlength="200" show-word-limit />
          </el-form-item>

          <el-form-item label="商品分类" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="请选择分类" clearable>
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </el-form-item>

          <el-form-item label="售价" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" placeholder="请输入售价" />
            <span class="unit">元</span>
          </el-form-item>

          <el-form-item label="原价">
            <el-input-number v-model="form.originalPrice" :min="0" :precision="2" placeholder="选填" />
            <span class="unit">元</span>
          </el-form-item>

          <el-form-item label="封面图片">
            <div class="upload-area">
              <el-upload
                class="cover-uploader"
                :show-file-list="false"
                :before-upload="beforeUpload"
                :http-request="uploadCover"
              >
                <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" />
                <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <span class="upload-tip">建议尺寸 800x800，支持 JPG/PNG</span>
            </div>
          </el-form-item>

          <el-form-item label="商品图片">
            <div class="upload-area">
              <el-upload
                list-type="picture-card"
                :file-list="imageFileList"
                :before-upload="beforeUpload"
                :http-request="uploadImage"
                :on-remove="removeImage"
                multiple
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <span class="upload-tip">可上传多张，第一张将作为封面</span>
            </div>
          </el-form-item>

          <el-form-item label="商品描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="6"
              placeholder="请描述商品的使用情况、新旧程度等..."
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
              {{ isEdit ? '保存修改' : '发布商品' }}
            </el-button>
            <el-button size="large" @click="$router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Layout from '../components/Layout.vue'
import { productApi, categoryApi } from '../api/product'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  categoryId: null,
  price: null,
  originalPrice: null,
  coverImage: '',
  description: '',
  images: []
})

const imageFileList = ref([])

const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

onMounted(async () => {
  try {
    categories.value = await categoryApi.list()
  } catch (e) { /* ignore */ }

  if (isEdit.value) {
    try {
      const product = await productApi.detail(route.params.id)
      form.title = product.title
      form.categoryId = product.categoryId
      form.price = product.price
      form.originalPrice = product.originalPrice
      form.coverImage = product.coverImage || ''
      form.description = product.description || ''
      form.images = product.images || []
      imageFileList.value = (product.images || []).map((url, i) => ({
        name: `image-${i}`,
        url
      }))
    } catch (e) {
      router.push('/')
    }
  }
})

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

async function uploadCover(options) {
  try {
    const res = await productApi.uploadImage(options.file)
    form.coverImage = res.url
    ElMessage.success('封面上传成功')
  } catch (e) { /* handled */ }
}

async function uploadImage(options) {
  try {
    const res = await productApi.uploadImage(options.file)
    form.images.push(res.url)
    ElMessage.success('图片上传成功')
  } catch (e) { /* handled */ }
}

function removeImage(file) {
  const idx = form.images.indexOf(file.url)
  if (idx > -1) form.images.splice(idx, 1)
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 如果有多张图片但没有封面，用第一张做封面
  if (!form.coverImage && form.images.length > 0) {
    form.coverImage = form.images[0]
  }

  submitting.value = true
  try {
    const data = {
      title: form.title,
      categoryId: form.categoryId,
      price: form.price,
      originalPrice: form.originalPrice,
      coverImage: form.coverImage,
      description: form.description,
      images: form.images
    }

    if (isEdit.value) {
      await productApi.update(route.params.id, data)
      ElMessage.success('修改成功')
    } else {
      await productApi.create(data)
      ElMessage.success('发布成功')
    }
    router.push('/my/products')
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}
</script>

<style scoped>
.publish-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
}

.form-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(0, 0, 0, 0.04);
  padding: 36px;
}

.unit {
  margin-left: 8px;
  color: var(--color-text-secondary);
}

.upload-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cover-uploader {
  width: 160px;
  height: 160px;
  border: 2px dashed var(--color-border);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color var(--transition-fast), background var(--transition-fast);
}

.cover-uploader:hover {
  border-color: var(--color-primary);
  background: rgba(16, 185, 129, 0.03);
}

.cover-uploader-icon {
  font-size: 28px;
  color: var(--color-text-muted);
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.upload-tip {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}
</style>
