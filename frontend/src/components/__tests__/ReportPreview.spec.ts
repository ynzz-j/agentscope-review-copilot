import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ReportPreview from '../ReportPreview.vue'

describe('ReportPreview', () => {
  it('renders markdown when a report is available', () => {
    const wrapper = mount(ReportPreview, {
      props: {
        markdown: '# 评审报告\n\n- 发现项：测试缺口',
      },
    })

    expect(wrapper.text()).toContain('# 评审报告')
    expect(wrapper.text()).toContain('测试缺口')
  })

  it('renders a stable empty state before the report is ready', () => {
    const wrapper = mount(ReportPreview, {
      props: {
        markdown: '',
      },
    })

    expect(wrapper.text()).toContain('报告尚未生成。')
  })
})
