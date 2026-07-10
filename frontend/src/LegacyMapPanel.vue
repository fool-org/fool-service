<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue";
import type { LegacyMapMarker } from "./viewWorkflow";

const props = defineProps<{ markers: LegacyMapMarker[] }>();
const mapElement = ref<HTMLElement | null>(null);
const mapError = ref("");
let map: import("leaflet").Map | undefined;

onMounted(async () => {
  try {
    const [leaflet] = await Promise.all([
      import("leaflet"),
      import("leaflet/dist/leaflet.css")
    ]);
    if (!mapElement.value) return;

    const points = props.markers
      .map((marker) => ({ marker, latitude: Number(marker.latitude), longitude: Number(marker.longitude) }))
      .filter(({ latitude, longitude }) =>
        Number.isFinite(latitude) && Number.isFinite(longitude) &&
        latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180
      );
    if (!points.length) {
      mapError.value = "No valid map locations.";
      return;
    }

    map = leaflet.map(mapElement.value, { scrollWheelZoom: false });
    leaflet.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      maxZoom: 19
    }).addTo(map);

    for (const { marker, latitude, longitude } of points) {
      leaflet.circleMarker([latitude, longitude], {
        color: "#0f766e",
        fillColor: "#14b8a6",
        fillOpacity: 0.85,
        radius: 8,
        weight: 2
      }).bindPopup(markerPopup(marker)).addTo(map);
    }

    const bounds = leaflet.latLngBounds(points.map(({ latitude, longitude }) => [latitude, longitude]));
    if (points.length === 1) map.setView(bounds.getCenter(), 13);
    else map.fitBounds(bounds, { maxZoom: 15, padding: [24, 24] });
    window.requestAnimationFrame(() => map?.invalidateSize());
  } catch {
    mapError.value = "Map unavailable.";
  }
});

onUnmounted(() => map?.remove());

function markerPopup(marker: LegacyMapMarker) {
  const content = document.createElement("div");
  const title = document.createElement("strong");
  title.textContent = marker.title?.text || marker.title?.label || "Location";
  content.append(title);
  for (const item of marker.info) {
    const row = document.createElement("div");
    row.textContent = `${item.label}: ${item.text}`;
    content.append(row);
  }
  return content;
}

function markerName(marker: LegacyMapMarker) {
  return marker.title?.text || marker.title?.label || "Location";
}
</script>

<template>
  <div class="legacy-map-panel">
    <div
      ref="mapElement"
      class="legacy-map"
      role="region"
      :aria-label="`${markers.length} map locations`"
    ></div>
    <p v-if="mapError" class="error-message">{{ mapError }}</p>
    <ul class="map-location-list">
      <li v-for="marker in markers" :key="`${marker.longitude}-${marker.latitude}-${markerName(marker)}`">
        <strong>{{ markerName(marker) }}</strong>
        <span>{{ marker.latitude }}, {{ marker.longitude }}</span>
      </li>
    </ul>
  </div>
</template>
