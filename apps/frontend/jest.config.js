/** @type {import('jest').Config} */
module.exports = {
  preset: 'jest-preset-angular',
  setupFilesAfterEnv: ['<rootDir>/setup-jest.ts'],
  testPathIgnorePatterns: ['<rootDir>/node_modules/', '<rootDir>/dist/'],
  collectCoverageFrom: [
    'src/app/**/*.ts',
    // Solo se excluye código declarativo sin lógica ni specs (no excluir lógica para llegar al umbral).
    '!src/app/**/*.spec.ts',
    '!src/app/app.config.ts',
    '!src/app/**/*.routes.ts',
  ],
  coverageDirectory: 'coverage',
  coverageThreshold: {
    global: { statements: 80, branches: 80, functions: 80, lines: 80 },
  },
};
