<!--
  根组件：仅渲染 Vue Router 视图
  根据当前路由（/login 或 /admin/*）动态切换 Login 或 AdminLayout
-->
<template>
  <router-view />
</template>
