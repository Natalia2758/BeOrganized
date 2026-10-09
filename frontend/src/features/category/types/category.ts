export interface Category {
    idCategory: number
    name: string | null
    color: string | null
}

export interface CategoryRequest {
    name: string
    color: string
}