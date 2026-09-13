package com.example.composelearning.todo.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TodoApi {

    /** POST api/my/todos */
    @POST("api/my/todos")
    suspend fun create(@Body body: TodoDto): TodoDto

    /**
     * GET api/my/todos?q=isDone:true+startDate:1+endDate:2
     *
     * `encoded = true` matters: the server's separators are literal `+` and `:`, and the
     * default Retrofit encoding would turn `+` into `%2B`, which the parser on the other
     * side does not expect. We hand it a string that is already in the wire format.
     */
    @GET("api/my/todos")
    suspend fun getTodos(@Query("q", encoded = true) q: String? = null): List<TodoDto>
}
