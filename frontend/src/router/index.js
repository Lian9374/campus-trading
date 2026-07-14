import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('../views/ProductDetail.vue')
  },
  {
    path: '/publish',
    name: 'Publish',
    component: () => import('../views/Publish.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/publish/:id',
    name: 'EditProduct',
    component: () => import('../views/Publish.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my/orders',
    name: 'MyOrders',
    component: () => import('../views/MyOrders.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my/products',
    name: 'MyProducts',
    component: () => import('../views/MyProducts.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my/favorites',
    name: 'MyFavorites',
    component: () => import('../views/MyFavorites.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('../views/Messages.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user/:id',
    name: 'UserProfile',
    component: () => import('../views/UserProfile.vue')
  },
  {
    path: '/my/following',
    name: 'MyFollowings',
    component: () => import('../views/MyFollowings.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my/followers',
    name: 'MyFollowers',
    component: () => import('../views/MyFollowers.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/feed',
    name: 'ActivityFeed',
    component: () => import('../views/ActivityFeed.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/users',
    name: 'DiscoverUsers',
    component: () => import('../views/DiscoverUsers.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    ElMessage.warning('请先登录')
    next('/login')
  } else if (to.meta.guest && token) {
    next('/')
  } else {
    next()
  }
})

export default router
