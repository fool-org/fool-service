<script setup lang="ts">
import type { LegacyAuthItem } from "./api";
import { legacyAuthNo, legacyAuthText, legacyAuthViewId } from "./viewWorkflow";

defineProps<{
  currentViewId: number;
  disabled: boolean;
  expandedAuthCode: string;
  items: LegacyAuthItem[];
  label: string;
  notifyCount: (item: LegacyAuthItem) => number;
  subItems: LegacyAuthItem[];
}>();

defineEmits<{
  select: [item: LegacyAuthItem];
}>();
</script>

<template>
  <nav v-if="items.length" class="nav-list" :aria-label="label">
    <div v-for="item in items" :key="legacyAuthNo(item) || legacyAuthText(item)" class="nav-group">
      <button
        type="button"
        :class="{
          active: legacyAuthViewId(item) === currentViewId,
          expanded: legacyAuthNo(item) === expandedAuthCode
        }"
        :disabled="disabled"
        @click="$emit('select', item)"
      >
        <span>{{ legacyAuthText(item) || legacyAuthNo(item) }}</span>
        <strong v-if="notifyCount(item)" class="nav-count">{{ notifyCount(item) }}</strong>
      </button>
      <div v-if="legacyAuthNo(item) === expandedAuthCode && subItems.length" class="nav-sublist">
        <button
          v-for="child in subItems"
          :key="legacyAuthNo(child) || legacyAuthText(child)"
          type="button"
          :class="{ active: legacyAuthViewId(child) === currentViewId }"
          :disabled="disabled || !legacyAuthViewId(child)"
          @click="$emit('select', child)"
        >
          <span>{{ legacyAuthText(child) || legacyAuthNo(child) }}</span>
          <strong v-if="notifyCount(child)" class="nav-count">{{ notifyCount(child) }}</strong>
        </button>
      </div>
    </div>
  </nav>
</template>
