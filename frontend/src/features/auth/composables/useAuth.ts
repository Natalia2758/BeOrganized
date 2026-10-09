
import { useAuthStore } from "../stores/useAuthStore";
import { storeToRefs } from "pinia";

/** Exposes authentication state and actions to Vue components. */
export function useAuth() {
  const authStore = useAuthStore()
  const { user, error, username, isAuthenticated } = storeToRefs(authStore)

  return {
    user,
    error,
    username,
    isAuthenticated,
    addUser: authStore.addUser,
    authenticateUser: authStore.authenticateUser,
    loadCurrentUser: authStore.loadCurrentUser,
    logoutUser: authStore.logoutUser,
    removeAccount: authStore.removeAccount,
    editUsername: authStore.editUsername
  }
}