<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{ fields: { label: string; text: string }[] }>();
const rows = computed(() => Array.from(
  { length: Math.max(6, Math.ceil(props.fields.length / 2)) },
  (_, index) => [props.fields[index * 2], props.fields[index * 2 + 1]]
));
</script>

<template>
  <div class="table-wrap sudoku-panel-body">
    <table class="legacy-item-table">
      <tbody>
        <tr v-for="(row, rowIndex) in rows" :key="rowIndex">
          <template v-for="(item, columnIndex) in row" :key="`${rowIndex}-${columnIndex}`">
            <template v-if="item">
              <th>{{ item.label }}</th>
              <td>{{ item.text }}</td>
            </template>
            <template v-else>
              <td colspan="2">&nbsp;</td>
            </template>
          </template>
        </tr>
      </tbody>
    </table>
  </div>
</template>
