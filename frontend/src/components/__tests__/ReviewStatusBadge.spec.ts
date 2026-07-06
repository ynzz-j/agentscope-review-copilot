import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ReviewStatusBadge from '../ReviewStatusBadge.vue'

describe('ReviewStatusBadge', () => {
  it('renders the review status', () => {
    const wrapper = mount(ReviewStatusBadge, {
      props: {
        status: 'COMPLETED',
      },
    })

    expect(wrapper.text()).toContain('COMPLETED')
    expect(wrapper.classes()).toContain('completed')
  })
})
