import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import NoteContainerItem from '../components/NoteContainerItem.vue'
import type { Note } from '../types/note'

describe('NoteContainerItem', () => {
    it('uses a fallback title and default color when values are missing', () => {
        const note: Note = {
            idNote: 5,
            content: 'Remember this'
        }
        const wrapper = mount(NoteContainerItem, {
            props: { note },
            global: {
                stubs: {
                    RouterLink: {
                        props: ['to'],
                        template: '<a :data-to="JSON.stringify(to)"><slot /></a>'
                    }
                }
            }
        })

        expect(wrapper.text()).toContain('No title')
        expect(wrapper.get('.note').element.getAttribute('style')).toContain('background-color: rgb(255, 231, 174)')
        expect(JSON.parse(wrapper.get('a').attributes('data-to')!)).toEqual({
            name: 'note-details',
            params: { idNote: 5 }
        })
    })

    it('emits the selected note when deletion is requested', async () => {
        const note: Note = {
            idNote: 5,
            title: 'Ideas',
            content: 'Remember this'
        }
        const wrapper = mount(NoteContainerItem, {
            props: { note },
            global: { stubs: { RouterLink: true } }
        })

        await wrapper.get('button').trigger('click')

        expect(wrapper.emitted('deleteRequest')).toEqual([[note]])
    })
})
