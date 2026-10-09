import axios from 'axios'

/**
 * Centralized Axios API used for sending HTTP requests to the server.
 */
const http = axios.create({
    baseURL: '/api',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json'
    }
})

export default http