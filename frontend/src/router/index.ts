import { createRouter, createWebHistory } from 'vue-router'
import ReviewCreateView from '@/views/ReviewCreateView.vue'
import ReviewProgressView from '@/views/ReviewProgressView.vue'
import ReviewResultView from '@/views/ReviewResultView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/reviews/new',
    },
    {
      path: '/reviews/new',
      name: 'review-create',
      component: ReviewCreateView,
    },
    {
      path: '/reviews/:id/progress',
      name: 'review-progress',
      component: ReviewProgressView,
    },
    {
      path: '/reviews/:id/result',
      name: 'review-result',
      component: ReviewResultView,
    },
  ],
})

export default router
