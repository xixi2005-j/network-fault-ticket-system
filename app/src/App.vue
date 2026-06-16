<template>
  <div id="app">
    <div class="global-bg"></div>
    <router-view v-slot="{ Component }">
      <transition name="page" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script setup lang="ts">
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.global-bg {
  position: fixed;
  inset: 0;
  background: url('@/assets/bj.jpg') center / cover no-repeat;
  z-index: -1;
}

.page-enter-active,
.page-leave-active {
  transition: transform 0.5s ease, opacity 0.3s ease;
}

.page-enter-from {
  opacity: 0;
  transform: scale(0.96) translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: scale(1.02) translateY(-10px);
}

.page-enter-to,
.page-leave-from {
  opacity: 1;
  transform: scale(1) translateY(0);
}
</style>
