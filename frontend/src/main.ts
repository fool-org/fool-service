import { createApp } from "vue";
import PrimeVue from "primevue/config";
import App from "./App.vue";
import { FoolTheme } from "./theme";
import "primeicons/primeicons.css";
import "./style.css";

createApp(App)
  .use(PrimeVue, {
    theme: {
      preset: FoolTheme,
      options: {
        darkModeSelector: false,
        cssLayer: {
          name: "primevue",
          order: "reset, primevue, app"
        }
      }
    }
  })
  .mount("#app");
