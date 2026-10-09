
/**
 * Converts date string from the storage format to the format appropriate for display.
 * 
 * @param date  a date string to be converted
 * @returns     a reformatted date string
 */
export function formatDateForDisplay(date: string) {

    if (!date) {
        throw new Error('formatDateForDisplay: date is undefined or empty')
    }

    const [year, month, day] = date.split('-')
    return `${day}-${month}-${year}`
}

/**
 * Converts date string from the display format to the format appropriate for storage.
 * 
 * @param date  a date string to be converted
 * @returns     a reformatted date string
 */
export function formatDateForStorage(date: string) {
    if (!date) {
        throw new Error('formatDateForDisplay: date is undefined or empty')
    }

    const [day, month, year] = date.split('-')
    return `${year}-${month}-${day}`
}

/**
 * Verifies if a date string is in a valid ISO format.
 * @param date  a date string to be validated
 * @returns     true if a string is ina valid ISO format, false if it is not
 */
export function isValidIsoDate(date: string): boolean {
    return /^\d{4}-\d{2}-\d{2}$/.test(date)
}

/**
 * Converts a time string from the storage format to the format appropriate for display.
 * 
 * @param date  a date string to be fconverted
 * @returns     a reformatted date string
 */
export function formatTimeForDisplay(time: string) {
    if (!time) {
        throw new Error('formatTimeForDisplay: time is undefined or empty')
    }

    const [hour, minute, second] = time.split(':')

    return `${hour}:${minute}`
}

/**
 * Converts date string from the storage format to the format for display.
 * 
 * @param date  a date string to be fconverted
 * @returns     a reformatted date string
 */
export function formatTimeForStorage(time: string) {

    if (!time) {
        throw new Error('formatTimeForDisplay: time is undefined or empty')
    }
    const [hour, minute, second] = time.split(':')

    return `${hour}:${minute}`
}

/**
 * Verifies if a time string is in a valid format.
 * @param date  a time string to be validated
 * @returns     true if a string is ina valid format, false if it is not
 */
export function isValidTime(time: string): boolean {
    return /^(?:[01]\d|2[0-3]):[0-5]\d$/.test(time)

}

/**
 * Converts a Date object to the display format.
 * 
 * @param date  a Date object to be converted
 * @returns     a reformatted date string
 */
export function formatDateTimeForDisplay(value: Date) {
    if (!value) {
        throw new Error('formatDateTimeForDisplay: date is undefined or empty')
    }

    const date = new Date(value)
    const hour = date.getHours().toString()
    const minutes = date.getMinutes().toString()
    const day = date.getDate().toString()
    const month = date.getMonth().toString()
    const year = date.getFullYear().toString()

    return hour + ':' + minutes + ', ' + day + '-' + month + '-' + year
}

/**
 * Verifies if the two Date objects are on the same calendar day.
 * 
 * @param leftDate  a first Date object to be compared
 * @param rightDate a second Date object to be compared 
 * @returns         a boolean value indicating that leftDate and rightDate are on the same day or not
 */
export function isSameDay(leftDate: Date, rightDate: Date) {
    return leftDate.getFullYear() === rightDate.getFullYear()
        && leftDate.getMonth() === rightDate.getMonth()
        && leftDate.getDate() === rightDate.getDate()
}