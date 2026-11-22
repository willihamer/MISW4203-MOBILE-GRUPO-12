package co.edu.uniandes.vinilos.data.model

data class Comment(val id: Int, val description: String, val rating: Int)

data class NewComment(val description: String, val rating: Int, val collectorId: Int)
