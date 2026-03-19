import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import tseslint from 'typescript-eslint';

export default tseslint.config(
    { ignores: ['dist'] },
    {
        extends: [js.configs.recommended, ...tseslint.configs.recommended],
        files: ['**/*.{ts,tsx}'],
        languageOptions: {
            ecmaVersion: 2020,
            globals: globals.browser,
        },
        plugins: {
            'react-hooks': reactHooks,
            'react-refresh': reactRefresh,
        },
        rules: {
            ...reactHooks.configs.recommended.rules,
            'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
            'react-hooks/set-state-in-effect': 'off', // This rule incorrectly detects async functions as synchronous
            'no-restricted-imports': [
                'error',
                {
                    patterns: [
                        {
                            // S'han reportat problemes (EMFILE: too many open files) per a executar els tests a Windows
                            // en usar imports directament de @mui/icons-material.
                            regex: '^@mui/icons-material+$',
                            message:
                                'Avoid importing from @mui/icons-material directly due to performance concerns. Import individual icons instead (e.g. @mui/icons-material/Add).',
                        },
                    ],
                },
            ],
        },
    }
);
