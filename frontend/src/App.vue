<template>
  <div id="app">
    <router-view />
    <Toast />
  </div>
</template>

<script setup>
import Toast from './components/Toast.vue'
</script>

<style>
#app {
  min-height: 100vh;
}
</style>
