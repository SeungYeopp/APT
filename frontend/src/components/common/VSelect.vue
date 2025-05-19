<script setup>
import { ref, computed, watch } from "vue";
const props = defineProps({ selectOption: {
    type: Array,
    required: true,
  },
  modelValue: {
    type: String,
    default: "", // Provide a default value
  }, });

const key = ref(props.modelValue);

const emit = defineEmits(["onKeySelect", "update:modelValue"]);
const onSelect = () => {
  //console.log(key.value + "선택!!!");
  emit("onKeySelect", key.value);
  emit("update:modelValue", key.value);
};

watch(
  () => props.modelValue,
  (newValue) => {
    key.value = newValue;
  }
);
</script>

<template>
  <select v-model="key" class="filter-select" @change="onSelect">
    <option v-for="option in selectOption" :key="option.value" :value="option.value" :disabled="option.value === ''">
      {{ option.text }}
    </option>
  </select>
</template>

<style scoped>
.filter-select {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-color: #ffffff;
  border: 1px solid #ddd;
  text-align: center;
  flex: 1;
  padding: 4px;
  font-size: 16px;
  width: 100%;
  cursor: pointer;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'%3E%3Cpath fill='gray' d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 16px;
}

::-webkit-scrollbar {
  display: none;
  /* Chrome, Safari */
}
</style>
