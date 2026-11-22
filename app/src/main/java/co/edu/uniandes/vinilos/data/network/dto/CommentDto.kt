package co.edu.uniandes.vinilos.data.network.dto

data class CommentDto(val id: Int?, val description: String?, val rating: Int?)

data class CommentCollectorDto(val id: Int)

data class CreateCommentRequestDto(
    val description: String,
    val rating: Int,
    val collector: CommentCollectorDto,
)
