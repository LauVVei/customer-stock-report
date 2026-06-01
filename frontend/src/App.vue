<template>
  <header class="navbar">
    <div class="navbar-brand">
      <span class="brand-icon">📈</span>
      <span class="brand-title">客户股票日报系统</span>
    </div>
    <div class="navbar-menu">
      <nav class="nav-links">
        <router-link class="nav-link" to="/customer-report/accounts">资产总览</router-link>
      </nav>
    </div>
    <div class="navbar-user">
      <span class="user-phone">日报分析</span>
    </div>
  </header>
  <router-view />
</template>
