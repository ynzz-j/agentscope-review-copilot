<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { reviewCategories, useReviewStore } from '@/stores/reviewStore'
import type { DiffMode, ReviewCategory } from '@/types/review'

const router = useRouter()
const store = useReviewStore()

const form = reactive({
  repoPath: '',
  diffMode: 'WORKING_TREE' as DiffMode,
  baseRef: '',
  sessionId: 'demo-session',
  focusCategories: [...reviewCategories] as ReviewCategory[],
})

function toggleCategory(category: ReviewCategory) {
  if (form.focusCategories.includes(category)) {
    form.focusCategories = form.focusCategories.filter((item) => item !== category)
  } else {
    form.focusCategories.push(category)
  }
}

async function submit() {
  const job = await store.create({
    repoPath: form.repoPath,
    diffMode: form.diffMode,
    baseRef: form.baseRef,
    sessionId: form.sessionId,
    focusCategories: form.focusCategories,
  })
  await router.push(`/reviews/${job.id}/progress`)
}
</script>

<template>
  <section class="page-grid">
    <div>
      <p class="eyebrow">AgentScope-Java 2.0.0-RC4</p>
      <h1>Start a local Git diff review</h1>
      <p class="lead">
        Create a review job from a local repository path. The assistant reads the diff, checks file context, streams review events, and writes a Markdown report.
      </p>
    </div>

    <form class="panel" @submit.prevent="submit">
      <label>
        Repository path
        <input v-model="form.repoPath" required placeholder="D:\workspace\demo-project" />
      </label>

      <div class="row">
        <label>
          Diff mode
          <select v-model="form.diffMode">
            <option value="WORKING_TREE">Working tree</option>
            <option value="STAGED">Staged</option>
            <option value="BASE_REF">Base ref</option>
          </select>
        </label>
        <label>
          Base ref
          <input v-model="form.baseRef" :disabled="form.diffMode !== 'BASE_REF'" placeholder="main" />
        </label>
      </div>

      <label>
        Session ID
        <input v-model="form.sessionId" placeholder="demo-session" />
      </label>

      <fieldset>
        <legend>Focus categories</legend>
        <button
          v-for="category in reviewCategories"
          :key="category"
          type="button"
          class="chip"
          :class="{ active: form.focusCategories.includes(category) }"
          @click="toggleCategory(category)"
        >
          {{ category }}
        </button>
      </fieldset>

      <p v-if="store.error" class="error">{{ store.error }}</p>
      <button class="primary" type="submit" :disabled="store.loading">
        {{ store.loading ? 'Creating...' : 'Create review job' }}
      </button>
    </form>
  </section>
</template>
