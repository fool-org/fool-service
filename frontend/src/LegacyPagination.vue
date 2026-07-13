<script setup lang="ts">
import Paginator, { type PageState } from "primevue/paginator";

withDefaults(defineProps<{
  disabled: boolean;
  pageIndex: number;
  pageSize: number;
  recordLabel?: string;
  showPager?: boolean;
  totalItems: number;
}>(), {
  recordLabel: "",
  showPager: true
});

const emit = defineEmits<{
  page: [pageIndex: number];
}>();

function changePage(event: PageState) {
  emit("page", event.page + 1);
}
</script>

<template>
  <div class="legacy-pagination">
    <span class="record-info">{{ recordLabel || `共${totalItems}条记录` }}</span>
    <Paginator
      v-if="showPager"
      :first="Math.max(0, (pageIndex - 1) * pageSize)"
      :page-link-size="7"
      :rows="pageSize"
      :total-records="totalItems"
      :disabled="disabled"
      template="PrevPageLink PageLinks NextPageLink"
      @page="changePage"
    >
      <template #previcon><span aria-hidden="true">&laquo;</span></template>
      <template #nexticon><span aria-hidden="true">&raquo;</span></template>
    </Paginator>
  </div>
</template>
