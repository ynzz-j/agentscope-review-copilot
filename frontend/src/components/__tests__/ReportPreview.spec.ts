import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ReportPreview from '../ReportPreview.vue'

describe('ReportPreview', () => {
  it('renders markdown when a report is available', () => {
    const wrapper = mount(ReportPreview, {
      props: {
        markdown: '# Review\n\n- Finding: test-gap',
      },
    })

    expect(wrapper.text()).toContain('# Review')
    expect(wrapper.text()).toContain('test-gap')
  })

  it('renders a stable empty state before the report is ready', () => {
    const wrapper = mount(ReportPreview, {
      props: {
        markdown: '',
      },
    })

    expect(wrapper.text()).toContain('Report is not ready yet.')
  })
})
