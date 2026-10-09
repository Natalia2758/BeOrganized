import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AuthForm from '../components/AuthForm.vue'

describe('AuthForm', () => {
    it('emits login credentials without a username', async () => {
        const wrapper = mount(AuthForm, {
            props: { isRegister: false, buttonText: 'Log in' }
        })

        await wrapper.get('#email').setValue('person@example.com')
        await wrapper.get('#password').setValue('password123')
        await wrapper.get('form').trigger('submit')

        expect(wrapper.emitted('login')).toEqual([[
            { email: 'person@example.com', password: 'password123' }
        ]])
        expect(wrapper.emitted('register')).toBeUndefined()
    })

    it('blocks invalid credentials and reports validation errors', async () => {
        const wrapper = mount(AuthForm, {
            props: { isRegister: false, buttonText: 'Log in' }
        })

        await wrapper.get('#email').setValue('invalid-email')
        await wrapper.get('#password').setValue('short')
        await wrapper.get('form').trigger('submit')

        expect(wrapper.text()).toContain('Invalid email')
        expect(wrapper.text()).toContain('Password must be at least 8 characters')
        expect(wrapper.emitted('login')).toBeUndefined()
    })

    it('emits all registration fields when registration is valid', async () => {
        const wrapper = mount(AuthForm, {
            props: { isRegister: true, buttonText: 'Register' }
        })

        await wrapper.get('#email').setValue('person@example.com')
        await wrapper.get('#username').setValue('Person')
        await wrapper.get('#password').setValue('password123')
        await wrapper.get('form').trigger('submit')

        expect(wrapper.emitted('register')).toEqual([[
            {
                email: 'person@example.com',
                username: 'Person',
                password: 'password123'
            }
        ]])
    })
})
