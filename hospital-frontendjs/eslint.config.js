import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{js,jsx}'],
    extends: [
      js.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      globals: globals.browser,
      parserOptions: { ecmaFeatures: { jsx: true } },
    },
    rules: {
      // Codebase memakai pola fetch-on-mount yang men-set loading di awal
      // effect. Aturan ini adalah saran performa React Compiler, bukan bug
      // fungsional, sehingga diturunkan menjadi warning agar lint tetap
      // memberi sinyal tanpa memblokir build.
      'react-hooks/set-state-in-effect': 'warn',
    },
  },
])
