import { createRouter, createWebHistory } from 'vue-router';
import AccountsOverview from '@/pages/AccountsOverview.vue';
import AccountDetail from '@/pages/AccountDetail.vue';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/customer-report/accounts',
    },
    {
      path: '/customer-report/accounts',
      name: 'accounts-overview',
      component: AccountsOverview,
    },
    {
      path: '/customer-report/accounts/:tradeAccount',
      name: 'account-detail',
      component: AccountDetail,
      props: true,
    },
  ],
});
