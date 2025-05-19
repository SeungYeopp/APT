<script setup>
defineProps({ article: Object });

// 로컬 스토리지에서 사용자 정보 가져오기
const user = JSON.parse(localStorage.getItem("user")) || null;

// 현재 로그인된 사용자의 ID
const currentUserId = user ? user.id : null;
</script>

<template>
  <tr class="text-center" v-if="article.userId === currentUserId">
    <th scope="row">{{ article.articleNo }}</th>
    <td class="text-start">
      <router-link
        :to="{ name: 'article-view', params: { articleno: article.articleNo } }"
        class="article-title link-dark"
      >
        {{ article.subject }}
      </router-link>
    </td>
    <td>{{ article.userName }}</td>
    <td>{{ article.hit }}</td>
    <td>{{ article.registerTime }}</td>
  </tr>
</template>

<style scoped>
a {
  text-decoration: none;
}
</style>
