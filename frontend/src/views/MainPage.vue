<script setup>
import TheMainView from './TheMainView.vue';
import TheAIView from './TheAIView.vue';
import TheReviewView from './TheReviewView.vue';
import TheFooter from '@/views/TheFooter.vue';
</script>

<template>
  <div class="main-page">
    <!-- Main Content -->
    <div class="main">
      <section id="main">
        <TheMainView />
      </section>
      <section id="ai" class="center-section">
        <TheAIView />
      </section>
      <section id="reviews">
        <TheReviewView />
      </section>
    </div>
    <!-- Footer -->
    <footer>
      <TheFooter />
    </footer>

  </div>
</template>

<style scoped>

</style>
