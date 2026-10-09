import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ConfirmCancellingModal from '../components/ui/ConfirmCancellingModal.vue'

describe('ConfirmCancellingModal', () => {
    it('emits yes when discard is confirmed', async () => {
        const wrapper = mount(ConfirmCancellingModal, {
            props: { object: 'task' }
        })

        await wrapper.get('.btn-danger').trigger('click')

        expect(wrapper.emitted('yes')).toHaveLength(1)
        expect(wrapper.emitted('no')).toBeUndefined()
    })

    it('emits no from the no button, close button, and backdrop', async () => {
        const wrapper = mount(ConfirmCancellingModal, {
            props: { object: 'task' }
        })

        await wrapper.get('.btn-secondary').trigger('click')
        await wrapper.get('.btn-close').trigger('click')
        await wrapper.get('.confirm-cancel-modal__backdrop').trigger('click')

        expect(wrapper.emitted('no')).toHaveLength(3)
        expect(wrapper.emitted('yes')).toBeUndefined()
    })
})
