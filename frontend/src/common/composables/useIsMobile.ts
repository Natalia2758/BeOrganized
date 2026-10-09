import { ref, onMounted, onBeforeUnmount } from 'vue'
/**
 * A composable function used to determine the width of the screen
 * 
 * @returns a reactive variable that indicates if the device is mobile or not
 */
export function useIsMobile() {
    const isMobile = ref(false)

    const mediaQuery = window.matchMedia('(max-width: 767.98px)')

    function update() {
        isMobile.value = mediaQuery.matches
    }

    onMounted(() => {
        update()
        mediaQuery.addEventListener('change', update)
    })

    onBeforeUnmount(() => {
        mediaQuery.removeEventListener('change', update)
    })

    return {
        isMobile
    }
}