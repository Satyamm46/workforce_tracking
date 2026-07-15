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
      // Our pages intentionally trigger data loading from effects (the
      // standard fetch-on-mount/param-change pattern); the loaders set
      // loading/error state synchronously, which this rule flags.
      'react-hooks/set-state-in-effect': 'off',
    },
  },
  {
    // Context modules export a Provider component plus its companion hook
    // (useAuth, useHealth, ...). Fast-refresh purity is not worth splitting
    // each pair into two files.
    files: ['src/context/**/*.{js,jsx}'],
    rules: {
      'react-refresh/only-export-components': 'off',
    },
  },
])
