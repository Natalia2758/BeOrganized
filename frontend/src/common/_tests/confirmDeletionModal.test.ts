import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ConfirmDeletionModal from '../components/ui/ConfirmDeletionModal.vue'

describe('ConfirmDeletionModal', () => {
    it('emits yes when deletion is confirmed', async () => {
        const wrapper = mount(ConfirmDeletionModal, {
            props: { object: 'task' }
        })

        await wrapper.get('.btn-danger').trigger('click')

        expect(wrapper.emitted('yes')).toHaveLength(1)
        expect(wrapper.emitted('cancel')).toBeUndefined()
    })

    it('emits cancel from the cancel button, close button, and backdrop', async () => {
        const wrapper = mount(ConfirmDeletionModal, {
            props: { object: 'task' }
        })

        await wrapper.get('.btn-secondary').trigger('click')
        await wrapper.get('.btn-close').trigger('click')
        await wrapper.get('.confirm-deletion-modal__backdrop').trigger('click')

        expect(wrapper.emitted('cancel')).toHaveLength(3)
        expect(wrapper.emitted('yes')).toBeUndefined()
    })
})
