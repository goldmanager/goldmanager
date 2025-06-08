import { createRouter, createWebHistory } from 'vue-router';
import Prices from '../components/PricesComponent.vue';
import UserLogin from '../components/LoginComponent.vue';
import Metals from '../components/MetalsComponent.vue';
import Units from '../components/UnitsComponent.vue';
import ItemTypes from '../components/ItemTypes.vue';
import Items from '../components/ItemsComponent.vue';
import Users from '../components/UsersComponent.vue';
import ItemStorages from '../components/ItemStorages.vue';
import PriceHistory from '../components/PriceHistoryComponent.vue'
import DataExport from '../components/DataExportComponent.vue';
import DataImport from '../components/DataImportComponent.vue';
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
