package com.hellmund.api

data class GenresResponse(val genres: List<ApiGenre>)

data class ApiGenre(val id: Int, val name: String)
