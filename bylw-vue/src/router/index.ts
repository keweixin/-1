import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import UserLayout from '@/components/layout/UserLayout.vue'
import { useAuthStore } from '@/stores/auth'

const publicRoutes = ['/login', '/register', '/admin-login']

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
  },
  {
    path: '/admin-login',
    component: () => import('@/views/auth/AdminLogin.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
  },
  {
    path: '/',
    component: UserLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/user/Home.vue'),
      },
      {
        path: 'food-hall',
        name: 'FoodHall',
        component: () => import('@/views/user/FoodHall.vue'),
      },
      {
        path: 'food/:id',
        name: 'FoodDetail',
        component: () => import('@/views/user/FoodDetail.vue'),
      },
      {
        path: 'encyclopedia',
        name: 'Encyclopedia',
        component: () => import('@/views/user/Encyclopedia.vue'),
      },
      {
        path: 'encyclopedia/:id',
        name: 'EncyclopediaDetail',
        component: () => import('@/views/user/EncyclopediaDetail.vue'),
      },
      {
        path: 'recipes',
        name: 'Recipes',
        component: () => import('@/views/user/Recipes.vue'),
      },
      {
        path: 'recipe/:id',
        name: 'RecipeDetail',
        component: () => import('@/views/user/RecipeDetail.vue'),
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/user/Community.vue'),
      },
      {
        path: 'community/:id',
        name: 'CommunityDetail',
        component: () => import('@/views/user/CommunityDetail.vue'),
      },
      {
        path: 'points',
        name: 'PointsMall',
        component: () => import('@/views/user/PointsMall.vue'),
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/user/Orders.vue'),
      },
      {
        path: 'orders/pay-success',
        name: 'PaySuccess',
        component: () => import('@/views/user/PaySuccess.vue'),
      },
      {
        path: 'orders/:id',
        name: 'OrderDetail',
        component: () => import('@/views/user/OrderDetail.vue'),
      },
      {
        path: 'appeals',
        name: 'Appeals',
        component: () => import('@/views/user/Appeal.vue'),
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
      },
      {
        path: 'recommendations',
        name: 'Recommendations',
        component: () => import('@/views/user/Recommendations.vue'),
      },
    ],
  },
  {
    path: '/rider',
    component: () => import('@/components/layout/RiderLayout.vue'),
    meta: { requiresAuth: true, roleType: 2 },
    children: [
      {
        path: '',
        name: 'RiderHome',
        component: () => import('@/views/rider/Home.vue'),
      },
      {
        path: 'hall',
        name: 'RiderHall',
        component: () => import('@/views/rider/Hall.vue'),
      },
      {
        path: 'tasks',
        name: 'RiderTasks',
        component: () => import('@/views/rider/Tasks.vue'),
      },
      {
        path: 'history',
        name: 'RiderHistory',
        component: () => import('@/views/rider/History.vue'),
      },
      {
        path: 'profile',
        name: 'RiderProfile',
        component: () => import('@/views/rider/Profile.vue'),
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/views/auth/AdminShell.vue'),
    meta: { requiresAuth: true, roleType: 99 },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
      },
      {
        path: 'foods',
        name: 'AdminFoodMgmt',
        component: () => import('@/views/admin/FoodMgmt.vue'),
      },
      {
        path: 'orders',
        name: 'AdminOrderMgmt',
        component: () => import('@/views/admin/OrderMgmt.vue'),
      },
      {
        path: 'users',
        name: 'AdminUserMgmt',
        component: () => import('@/views/admin/UserMgmt.vue'),
      },
      {
        path: 'community',
        name: 'AdminCommunityMgmt',
        component: () => import('@/views/admin/CommunityMgmt.vue'),
      },
      {
        path: 'points',
        name: 'AdminPointsMgmt',
        component: () => import('@/views/admin/PointsMgmt.vue'),
      },
      {
        path: 'content',
        name: 'AdminContentMgmt',
        component: () => import('@/views/admin/ContentMgmt.vue'),
      },
      {
        path: 'settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/SystemSettings.vue'),
      },
      {
        path: 'appeals',
        name: 'AdminAppealMgmt',
        component: () => import('@/views/admin/AppealMgmt.vue'),
      },
      {
        path: 'dispatch',
        name: 'AdminDispatchMgmt',
        component: () => import('@/views/admin/DispatchMgmt.vue'),
      },
    ],
  },
  {
    path: '/merchant',
    component: () => import('@/components/layout/MerchantLayout.vue'),
    meta: { requiresAuth: true, roleType: 3 },
    children: [
      {
        path: '',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/Dashboard.vue'),
      },
      {
        path: 'foods',
        name: 'MerchantFoodMgmt',
        component: () => import('@/views/merchant/FoodMgmt.vue'),
      },
      {
        path: 'orders',
        name: 'MerchantOrderMgmt',
        component: () => import('@/views/merchant/OrderMgmt.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  if ((to.path === '/admin' || to.path.startsWith('/admin/')) && !authStore.isAdmin()) {
    next('/admin-login')
    return
  }

  if (to.path.startsWith('/rider') && authStore.roleType !== 2) {
    next('/login')
    return
  }

  if (to.path.startsWith('/merchant') && authStore.roleType !== 3) {
    next('/login')
    return
  }

  // Require auth for user pages that need login
  const authRequiredUserRoutes = ['/orders', '/appeals', '/points', '/recommendations', '/profile']
  if (authRequiredUserRoutes.some(p => to.path === p || to.path.startsWith(p + '/')) && !authStore.isAuthenticated()) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated() && !publicRoutes.includes(to.path)) {
    next('/login')
  } else {
    next()
  }
})

export default router
