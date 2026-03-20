import { defineConfig, devices } from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",
  timeout: 30000,
  use: {
    baseURL: "http://localhost:3000",
    video: "on",
    screenshot: "on",
    viewport: { width: 390, height: 844 }, // iPhone 14 Pro
    trace: "on",
  },
  outputDir: "./e2e/results",
  projects: [
    {
      name: "Mobile Chrome",
      use: { ...devices["Pixel 7"] },
    },
  ],
  webServer: {
    command: "npm run dev",
    port: 3000,
    reuseExistingServer: true,
  },
});
