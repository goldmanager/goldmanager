import { FlatCompat } from '@eslint/eslintrc';
import js from '@eslint/js';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const compat = new FlatCompat({
  baseDirectory: __dirname,
  resolvePluginsRelativeTo: __dirname,
  recommendedConfig: js.configs.recommended
});

export default [
  ...compat.extends('plugin:vue/recommended', 'eslint:recommended'),
  ...compat.plugins('vue'),
  ...compat.env({ node: true }),
  ...compat.config({
    parser: 'vue-eslint-parser',
    parserOptions: {
      parser: '@babel/eslint-parser',
      requireConfigFile: false,
      ecmaVersion: 2020,
      sourceType: 'module'
    },
    rules: {}
  })
];
