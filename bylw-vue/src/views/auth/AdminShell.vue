<template>
  <AdminLayout v-if="authStore.isAdmin()" />
  <AdminLogin v-else />
</template>

<script setup lang="ts">
import AdminLayout from '@/components/layout/AdminLayout.vue'
import AdminLogin from '@/views/auth/AdminLogin.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
</script>
