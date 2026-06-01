export interface RuntimeConfig {
  apiBaseUrl: string;
}

let runtimeConfig: RuntimeConfig = {
  apiBaseUrl: '/api',
};

export async function loadRuntimeConfig() {
  try {
    const response = await fetch('/runtime-config.json', { cache: 'no-store' });
    if (response.ok) {
      runtimeConfig = await response.json();
    }
  } catch (error) {
    console.warn('Failed to load runtime config, fallback to defaults.', error);
  }
}

export function getRuntimeConfig() {
  return runtimeConfig;
}
