import { createApp } from 'vue';
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';
import App from './App.vue';
import { router } from './router';
import { loadRuntimeConfig } from './config/runtime';
import './styles.css';

async function bootstrap() {
  await loadRuntimeConfig();
  const app = createApp(App);
  app.use(Antd);
  app.use(router);
  app.mount('#app');
}

bootstrap();
