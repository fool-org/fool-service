import { definePreset } from "@primeuix/themes";
import Nora from "@primeuix/themes/nora";

export const FoolTheme = definePreset(Nora, {
  primitive: {
    borderRadius: {
      xs: "4px",
      sm: "6px",
      md: "8px",
      lg: "12px",
      xl: "16px"
    }
  },
  semantic: {
    primary: {
      50: "#eef5fb",
      100: "#d9e9f7",
      200: "#b6d5ee",
      300: "#88b9df",
      400: "#5e9bcf",
      500: "#337ab7",
      600: "#2e6da4",
      700: "#286090",
      800: "#204d74",
      900: "#193d5d",
      950: "#10283e"
    },
    focusRing: {
      width: "2px",
      style: "solid",
      color: "{primary.color}",
      offset: "2px",
      shadow: "none"
    },
    formField: {
      borderRadius: "{border.radius.md}",
      focusRing: {
        width: "2px",
        style: "solid",
        color: "{primary.color}",
        offset: "-1px",
        shadow: "none"
      }
    },
    content: {
      borderRadius: "{border.radius.lg}"
    },
    colorScheme: {
      light: {
        surface: {
          0: "#ffffff",
          50: "{slate.50}",
          100: "{slate.100}",
          200: "{slate.200}",
          300: "{slate.300}",
          400: "{slate.400}",
          500: "{slate.500}",
          600: "{slate.600}",
          700: "{slate.700}",
          800: "{slate.800}",
          900: "{slate.900}",
          950: "{slate.950}"
        },
        primary: {
          color: "{primary.500}",
          contrastColor: "#ffffff",
          hoverColor: "{primary.700}",
          activeColor: "{primary.800}"
        },
        formField: {
          borderColor: "{surface.300}",
          hoverBorderColor: "{primary.400}",
          focusBorderColor: "{primary.500}",
          color: "{surface.900}",
          placeholderColor: "{surface.500}"
        },
        text: {
          color: "{surface.900}",
          hoverColor: "{surface.950}",
          mutedColor: "{surface.500}",
          hoverMutedColor: "{surface.600}"
        },
        content: {
          background: "{surface.0}",
          hoverBackground: "{surface.50}",
          borderColor: "{surface.200}",
          color: "{text.color}",
          hoverColor: "{text.hover.color}"
        }
      }
    }
  }
});
