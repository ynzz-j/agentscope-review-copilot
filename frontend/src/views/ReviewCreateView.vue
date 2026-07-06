<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { categoryLabels, diffModeLabels } from '@/i18n/zhCN'
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
      <h1>发起本地 Git diff 评审</h1>
      <p class="lead">
        输入本地仓库路径后，系统会读取 diff、补充源码上下文、通过 SSE 推送评审进度，并生成可复制的 Markdown 报告。
      </p>
    </div>

    <form class="panel" @submit.prevent="submit">
      <label>
        仓库路径
        <input v-model="form.repoPath" required placeholder="D:\workspace\demo-project" />
      </label>

      <div class="row">
        <label>
          Diff 范围
          <select v-model="form.diffMode">
            <option value="WORKING_TREE">{{ diffModeLabels.WORKING_TREE }}</option>
            <option value="STAGED">{{ diffModeLabels.STAGED }}</option>
            <option value="BASE_REF">{{ diffModeLabels.BASE_REF }}</option>
          </select>
        </label>
        <label>
          基准分支
          <input v-model="form.baseRef" :disabled="form.diffMode !== 'BASE_REF'" placeholder="main" />
        </label>
      </div>

      <label>
        会话 ID
        <input v-model="form.sessionId" placeholder="demo-session" />
      </label>

      <fieldset>
        <legend>评审重点</legend>
        <button
          v-for="category in reviewCategories"
          :key="category"
          type="button"
          class="chip"
          :class="{ active: form.focusCategories.includes(category) }"
          @click="toggleCategory(category)"
        >
          {{ categoryLabels[category] }}
        </button>
      </fieldset>

      <p v-if="store.error" class="error">{{ store.error }}</p>
      <button class="primary" type="submit" :disabled="store.loading">
        {{ store.loading ? '创建中...' : '创建评审任务' }}
      </button>
    </form>
  </section>
</template>
