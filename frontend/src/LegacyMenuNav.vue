<script setup lang="ts">
import type { LegacyAuthItem } from "./api";
import { legacyAuthImageUrl, legacyAuthNo, legacyAuthText, legacyAuthViewId } from "./viewWorkflow";

defineProps<{
  expandedAuthCode: string;
  horizontal?: boolean;
  items: LegacyAuthItem[];
  label: string;
  subItems: LegacyAuthItem[];
}>();

defineEmits<{
  select: [item: LegacyAuthItem];
}>();
</script>

<template>
  <nav v-if="items.length" class="nav-list" :class="{ 'nav-list-horizontal': horizontal }" :aria-label="label">
    <div v-for="item in items" :key="legacyAuthNo(item) || legacyAuthText(item)" class="nav-group">
      <button
        type="button"
        :class="{ expanded: legacyAuthNo(item) === expandedAuthCode }"
        :aria-expanded="legacyAuthViewId(item) ? undefined : legacyAuthNo(item) === expandedAuthCode"
        @click="$emit('select', item)"
      >
        <span class="nav-label">
          <img v-if="legacyAuthImageUrl(item)" class="nav-menu-image" :src="legacyAuthImageUrl(item)" alt="" />
          <span>{{ legacyAuthText(item) || legacyAuthNo(item) }}</span>
        </span>
      </button>
      <div v-if="legacyAuthNo(item) === expandedAuthCode && subItems.length" class="nav-sublist">
        <template
          v-for="child in subItems"
          :key="legacyAuthNo(child) || legacyAuthText(child)"
        >
          <button v-if="legacyAuthViewId(child)" type="button" @click="$emit('select', child)">
            <span class="nav-label">
              <img v-if="legacyAuthImageUrl(child)" class="nav-menu-image" :src="legacyAuthImageUrl(child)" alt="" />
              <span>{{ legacyAuthText(child) || legacyAuthNo(child) }}</span>
            </span>
          </button>
          <span v-else class="nav-static-item">
            <span class="nav-label">
              <img v-if="legacyAuthImageUrl(child)" class="nav-menu-image" :src="legacyAuthImageUrl(child)" alt="" />
              <span>{{ legacyAuthText(child) || legacyAuthNo(child) }}</span>
            </span>
          </span>
        </template>
      </div>
    </div>
  </nav>
</template>
