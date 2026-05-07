import axios from 'axios';
import { getRuntimeConfig } from '@/config/runtime';

export const http = axios.create({
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  config.baseURL = getRuntimeConfig().apiBaseUrl;
  return config;
});
