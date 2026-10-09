<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import AppHeader from '@/common/components/layout/AppHeader.vue'
import AppFooter from '@/common/components/layout/AppFooter.vue'
import { computed } from 'vue'
import InitialHeader from '@/common/components/layout/InitialHeader.vue'
import InitialFooter from '@/common/components/layout/InitialFooter.vue'
import AppQuickAddition from '@/common/components/ui/AppQuickAddition.vue'

const route = useRoute()
const currentPagePath = computed(() => route.path)
const showQuickAddition = computed(() =>
  route.matched.some((record) => record.meta.showQuickAddition),
)

</script>

<template>
  <div class="app-shell container-fluid px-0">
    <header class="w-100">
      <AppHeader v-if="currentPagePath !== '/login' && currentPagePath !== '/register'" />
      <InitialHeader v-else />
    </header>

    <div class="app-shell__content flex-grow-1">
      <div v-if="showQuickAddition">
        <AppQuickAddition />
      </div>

      <main>
        <section>
            <RouterView />
        </section>
      </main>
    </div>

    <footer class="w-100 text-muted">
      <AppFooter v-if="currentPagePath !== '/login' && currentPagePath !== '/register'" />
      <InitialFooter v-else />
    </footer>
  </div>
  
</template>

<style scoped>
.app-shell {
  
  --app-header-height: 4.5rem;

  display: flex;
  flex-direction: column;
  padding-top: var(--app-header-height);
  min-height: 100dvh;
  width: 100%;
}

.app-shell__content {
  width: min(100%, 1400px);
  max-width: 100%;
  min-height: 100dvh;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--app-gap-lg);
  margin: 0 auto;
  padding: 0 0 3rem;
}

@media (max-width: 768px) {
  .app-shell {
    --app-header-height: 1.5rem;
  }

  .app-shell__content {
    padding-inline: 0.25rem;
  }
}


</style>
