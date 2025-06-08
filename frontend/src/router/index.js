import { createRouter, createWebHistory } from 'vue-router';
const Prices = () => import('../components/PricesComponent.vue');
const UserLogin = () => import('../components/LoginComponent.vue');
const Metals = () => import('../components/MetalsComponent.vue');
const Units = () => import('../components/UnitsComponent.vue');
const ItemTypes = () => import('../components/ItemTypes.vue');
const Items = () => import('../components/ItemsComponent.vue');
const Users = () => import('../components/UsersComponent.vue');
const ItemStorages = () => import('../components/ItemStorages.vue');
const PriceHistory = () => import('../components/PriceHistoryComponent.vue');
const DataExport = () => import('../components/DataExportComponent.vue');
const DataImport = () => import('../components/DataImportComponent.vue');
// Create the router
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: Prices, meta: { requiresAuth: true } },
    { path: '/login', component: UserLogin },
    { path: '/metals', component: Metals, meta: { requiresAuth: true } },
    { path: '/units', component: Units, meta: { requiresAuth: true } },
    { path: '/itemTypes', component: ItemTypes, meta: { requiresAuth: true } },
	{ path: '/itemStorages', component: ItemStorages, meta: { requiresAuth: true } },
    { path: '/items', component: Items, meta: { requiresAuth: true } },
    { path: '/users', component: Users, meta: { requiresAuth: true } },
        { path: '/priceHistory', component: PriceHistory, meta:{requiresAuth: true}},
    { path: '/dataexport', component: DataExport, meta: { requiresAuth: true } },
    { path: '/dataimport', component: DataImport, meta: { requiresAuth: true } }
    // Add additional routes here
  ]
});

// Router guard to check the authentication status
router.beforeEach((to, from, next) => {
  const isAuthenticated = !!sessionStorage.getItem('jwt-token'); // Check whether the user is authenticated

  // If the route requires authentication
  if (to.meta.requiresAuth) {
    if (isAuthenticated) {
      next(); // User is authenticated, proceed to the route
    } else {
      next('/login'); // User is not authenticated, redirect to the login page
    }
  } else {
    next(); // No authentication required, continue to the route
  }
});

export default router;
