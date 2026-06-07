/**
 * Vue Router 配置（Hash 模式）
 * 惰性加载所有页面组件，路由守卫使用 AuthStore 校验管理员登录状态
 */
import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 惰性加载页面组件（按需加载）
const AdminLayout = () => import('@/layout/AdminLayout.vue')
const Login = () => import('@/views/Login.vue')
const Dashboard = () => import('@/views/Dashboard.vue')
const OrderList = () => import('@/views/OrderList.vue')
const FoodList = () => import('@/views/FoodList.vue')
const FoodEdit = () => import('@/views/FoodEdit.vue')
const CategoryList = () => import('@/views/CategoryList.vue')
const UserList = () => import('@/views/UserList.vue')
const Settings = () => import('@/views/Settings.vue')
const Password = () => import('@/views/Password.vue')

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        {
            path: '/login',
            name: 'Login',
            component: Login,
            meta: { public: true }
        },
        {
            path: '/',
            component: AdminLayout,
            redirect: '/dashboard',
            children: [
                { path: 'dashboard', name: 'Dashboard', component: Dashboard, meta: { title: '首页' } },
                { path: 'orders/pending', name: 'OrdersPending', component: OrderList, meta: { title: '待取餐', pending: true } },
                { path: 'orders', name: 'Orders', component: OrderList, meta: { title: '订单管理' } },
                { path: 'foods', name: 'Foods', component: FoodList, meta: { title: '菜品管理' } },
                { path: 'foods/edit/:id?', name: 'FoodEdit', component: FoodEdit, meta: { title: '编辑菜品' } },
                { path: 'categories', name: 'Categories', component: CategoryList, meta: { title: '分类管理' } },
                { path: 'users', name: 'Users', component: UserList, meta: { title: '客户管理' } },
                { path: 'settings', name: 'Settings', component: Settings, meta: { title: '系统设置' } },
                { path: 'password', name: 'Password', component: Password, meta: { title: '修改密码' } }
            ]
        }
    ]
})

// 路由守卫：优先读取 AuthStore 状态，仅在未确认时发起 API 请求
router.beforeEach(async (to) => {
    if (to.meta.public) return true
    const auth = useAuthStore()
    if (auth.isLoggedIn) return true
    const valid = await auth.checkSession()
    return valid || '/login'
})

export default router
