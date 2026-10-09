import { describe, expect, it } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import NoteForm from '../components/NoteForm.vue'
import type { Category } from '@/features/category/types/category'
import type { Note } from '../types/note'

const categories: Category[] = [
    { idCategory: 6, name: 'Personal', color: '#00ff00' }
]

describe('NoteForm', () => {
    it('requires non-empty content', async () => {
        const wrapper = mount(NoteForm, {
            props: { categories }
        })

        await wrapper.get('form').trigger('submit')

        expect(wrapper.text()).toContain('Note cannot be empty')
        expect(wrapper.emitted('submit')).toBeUndefined()
    })

    it('submits valid data initialized from the note', async () => {
        const initialNote: Note = {
            idNote: 3,
            title: 'Ideas',
            content: 'Plan the trip',
            date: '2026-10-12',
            category: categories[0]
        }
        const wrapper = mount(NoteForm, {
            props: {
                categories,
                initialNote
            }
        })

        await wrapper.get('form').trigger('submit')

        expect(wrapper.emitted('submit')).toEqual([[
            {
                title: 'Ideas',
                content: 'Plan the trip',
                date: '2026-10-12',
                idCategory: 6
            }
        ]])
    })

    it('uses the supplied initial date and emits cancel', async () => {
        const wrapper = mount(NoteForm, {
            props: {
                categories,
                initialDate: '2026-10-12'
            }
        })
        await flushPromises()

        expect((wrapper.get('#date').element as HTMLInputElement).value).toBe('2026-10-12')

        await wrapper.get('.btn-danger').trigger('click')

        expect(wrapper.emitted('cancel')).toHaveLength(1)
    })
})
