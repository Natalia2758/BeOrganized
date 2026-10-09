import { createRouter, createWebHistory } from 'vue-router'
import http from '@/common/api/http'
import DashboardView from '@/features/dashboard/views/DashboardView.vue'
import TaskView from '@/features/task/views/TaskView.vue'
import TaskDetailsView from '@/features/task/views/TaskDetailsView.vue'
import TaskCreationView from '@/features/task/views/TaskCreationView.vue'
import TaskEditView from '@/features/task/views/TaskEditView.vue'
import CategoryView from '@/features/category/views/CategoryView.vue'
import RegisterView from '@/features/auth/views/RegisterView.vue'
import LoginView from '@/features/auth/views/LoginView.vue'
import RemindersView from '@/features/reminder/views/ReminderView.vue'
import CategoryCreationView from '@/features/category/views/CategoryCreationView.vue'
import EventView from '@/features/event/views/EventView.vue'
import EventCreationView from '@/features/event/views/EventCreationView.vue'
import ReminderCreationView from '@/features/reminder/views/ReminderCreationView.vue'
import EventDetailsView from '@/features/event/views/EventDetailsView.vue'
import CalendarView from '@/features/calendar/views/CalendarView.vue'
import NoteView from '@/features/note/views/NoteView.vue'
import NoteCreationView from '@/features/note/views/NoteCreationView.vue'
import NoteEditView from '@/features/note/views/NoteEditView.vue'
import NoteDetailsView from '@/features/note/views/NoteDetailsView.vue'
import axios from 'axios'
import EventEditView from '@/features/event/views/EventEditView.vue'
import ReminderEditView from '@/features/reminder/views/ReminderEditView.vue'
import ScheduleView from '@/features/schedule/views/ScheduleView.vue'
import ErrorView from '@/common/views/ErrorView.vue'
import { useAuthStore } from '@/features/auth/stores/useAuthStore'

// Track authentication state to avoid redundant API calls
let cachedAuthState: boolean | null = null
let authCheckTimeout: ReturnType<typeof setTimeout> | null = null

/**
 * Checks if a user is logged in and authenticated.
 * @returns true is a user is authenticated, false if not
 */
async function isAuthenticated(): Promise<boolean> {
  
  if (cachedAuthState !== null) {
    return cachedAuthState
  }

  try {
    const response = await http.get('/auth/status')
    cachedAuthState = true
    
    // clear cache after 30 seconds to ensure fresh check periodically
    if (authCheckTimeout) {
      clearTimeout(authCheckTimeout)
    }
    authCheckTimeout = setTimeout(() => {
      cachedAuthState = null
    }, 30000)
    
    return true
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      cachedAuthState = false
      if (authCheckTimeout) {
        clearTimeout(authCheckTimeout)
      }
      authCheckTimeout = setTimeout(() => {
        cachedAuthState = null
      }, 30000)
      return false
    }
    cachedAuthState = false
    return false
  }
}
/**
 * Responsible for handling routing to different application URL paths. Each path is assigned to a view (vue component) that is displayed when the path is selected
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/calendar',
      name: 'calendar',
      component: CalendarView,
      meta: { requiresAuth: true, showQuickAddition: true }
    },
    {
      path: '/categories',
      name: 'categories',
      component: CategoryView,
      meta: { requiresAuth: true, showQuickAddition: true }
    },
    {
      path: '/categories/create',
      name: 'category-creation',
      component: CategoryCreationView,
      meta: { requiresAuth: true }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true, showQuickAddition: true }
    },
    {
      path: '/events',
      name: 'events',
      component: EventView,
      meta: { requiresAuth: true, showQuickAddition: true }
    },
    {
      path: '/events/create',
      name: 'event-creation',
      component: EventCreationView,
      meta: { requiresAuth: true }
    },
    {
      path: '/events/:idEvent',
      name: 'event-details',
      component: EventDetailsView,
      meta: { requiresAuth: true }
    },
    {
      path: '/events/:idEvent/update',
      name: 'event-update',
      component: EventEditView,
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresAuth: false }
    },
    {
      path: '/notes',
      name: 'notes',
      component: NoteView,
      meta: { requiresAuth: true, showQuickAddition: true }
    },
    {
      path: '/notes/create',
      name: 'note-creation',
      component: NoteCreationView,
      meta: { requiresAuth: true }
    },
    {
      path: '/notes/:idNote',
      name: 'note-details',
      component: NoteDetailsView,
      meta: { requiresAuth: true }
    },
    {
      path: '/notes/:idNote/update',
      name: 'note-update',
      component: NoteEditView,
      meta: { requiresAuth: true }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { requiresAuth: false }
    },
    {
      path: '/reminders',
      name: 'reminders',
      component: RemindersView,
      meta: { requiresAuth: true, showQuickAddition: true }
    },
    {
      path: '/reminders/create/:idEventTask?',
      name: 'reminder-creation',
      component: ReminderCreationView,
      meta: { requiresAuth: true }
    },
    {
      path: '/reminders/:idReminder/update',
      name: 'reminder-update',
      component: ReminderEditView,
      meta: { requiresAuth: true }
    },
    {
      path: '/schedule',
      name: 'schedule',
      component: ScheduleView,
      meta: { requiresAuth: true, showQuickAddition: true }
    },
    {
      path: '/tasks',
      name: 'tasks',
      component: TaskView,
      meta: { requiresAuth: true, showQuickAddition: true }
    },
    {
      path: '/tasks/create',
      name: 'task-creation',
      component: TaskCreationView,
      meta: { requiresAuth: true }
    },
    {
      path: '/tasks/:idTask',
      name: 'task-details',
      component: TaskDetailsView,
      meta: { requiresAuth: true }
    },
    {
      path: '/tasks/:idTask/update',
      name: 'task-update',
      component: TaskEditView,
      meta: { requiresAuth: true }
    },
    {
      // Handling unknown URLs
      path: '/:pathMatch(.*)*',
      name: 'error',
      component: ErrorView,
      meta: { requiresAuth: true }
    }
  ],
})

/**
 * Runs before every route change.<br>
 * destination - contains inf. about the route the user is trying to navigate to.
 * Used to check if the destination route requires user to be authenticated.
 * 
 */
router.beforeEach(async (destination) => {
  const requiresAuth = destination.matched.some((record) => record.meta.requiresAuth)
  const authStore = useAuthStore()

  if (requiresAuth && authStore.isAuthenticated) {
    return
  }

  if (requiresAuth && !(await isAuthenticated())) {
    return { name: 'login' }
  }
})

export default router
