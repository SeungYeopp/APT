<script setup>
import { ref } from "vue";
import { useUserStore } from "@/stores/user";
import { storeToRefs } from "pinia";

const store = useUserStore();

const {getUser:user} = storeToRefs(store);

</script>

<template>
    <div class="container text-center mt-3">
        <RouterView/>
    </div>
</template>

<style scoped>

</style>