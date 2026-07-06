<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div>
        <p class="product-kicker">AgentScope-Java 2.0.0-RC4</p>
        <h1>Review Copilot</h1>
        <p>Local Git diff review assistant for Java engineering quality.</p>
      </div>
      <nav>
        <RouterLink to="/reviews/new">New review</RouterLink>
      </nav>
    </aside>

    <main class="content">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  background: #f4f7fb;
}

.sidebar {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 28px;
  background: #0f172a;
  color: #e2e8f0;
}

.product-kicker {
  margin: 0 0 10px;
  color: #5eead4;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.sidebar h1 {
  margin: 0 0 12px;
  color: #fff;
  font-size: 28px;
  line-height: 1.2;
}

.sidebar p {
  color: #cbd5e1;
}

nav a {
  display: inline-flex;
  min-height: 40px;
  align-items: center;
  padding: 0 12px;
  border-radius: 6px;
  background: #1e293b;
  color: #fff;
  text-decoration: none;
  font-weight: 700;
}

.content {
  min-width: 0;
  padding: 32px;
}

@media (max-width: 760px) {
  .app-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    gap: 20px;
  }

  .content {
    padding: 18px;
  }
}
</style>
