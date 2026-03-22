/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class', // BẮT BUỘC PHẢI CÓ DÒNG NÀY (Có dấu phẩy ở cuối)
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}