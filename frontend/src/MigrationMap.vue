<script setup lang="ts">
import { migrationModules } from "./viewShell";
</script>

<template>
  <section class="migration-band" aria-label="Migration map">
    <div v-for="module in migrationModules" :key="module.source">
      <strong>{{ module.source }}</strong>
      <span>{{ module.target }}</span>
    </div>
  </section>
</template>
