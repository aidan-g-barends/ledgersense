// Tells Next.js to run CSS through Tailwind v4's PostCSS plugin.
// Without this file, @import "tailwindcss" and @apply do nothing.
const config = {
  plugins: {
    "@tailwindcss/postcss": {},
  },
};

export default config;