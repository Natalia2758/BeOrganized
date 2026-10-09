import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import QuickNoteForm from '../components/QuickNoteForm.vue'

describe('QuickNoteForm', () => {
    it('requires note content', async () => {
        const wrapper = mount(QuickNoteForm, {
            props: { isSubmitted: false },
            global: {
                stubs: {
                    CommonButton: {
                        template: '<button type="submit"><slot /></button>'
                    }
                }
            }
        })

        await wrapper.get('form').trigger('submit')

        expect(wrapper.text()).toContain('Note cannot be empty')
        expect(wrapper.emitted('submit')).toBeUndefined()
    })

    it('emits title and content when valid', async () => {
        const wrapper = mount(QuickNoteForm, {
            props: { isSubmitted: false },
            global: {
                stubs: {
                    CommonButton: {
                        template: '<button type="submit"><slot /></button>'
                    }
                }
            }
        })

        await wrapper.get('#title').setValue('Ideas')
        await wrapper.get('textarea').setValue('Plan the trip')
        await wrapper.get('form').trigger('submit')

        expect(wrapper.emitted('submit')).toEqual([[
            { title: 'Ideas', content: 'Plan the trip' }
        ]])
    })

    it('disables cancel while submitting', async () => {
        const wrapper = mount(QuickNoteForm, {
            props: { isSubmitted: true },
            global: {
                stubs: {
                    CommonButton: {
                        template: '<button type="submit" :disabled="$attrs.disabled"><slot /></button>',
                        inheritAttrs: false
                    }
                }
            }
        })

        expect(wrapper.get('.btn-danger').attributes('disabled')).toBeDefined()

        await wrapper.get('.btn-danger').trigger('click')

        expect(wrapper.emitted('cancel')).toBeUndefined()
    })
})
