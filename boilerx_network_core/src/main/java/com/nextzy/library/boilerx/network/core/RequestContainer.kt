package com.nextzy.library.boilerx.network.core

import android.os.Bundle
import kotlinx.coroutines.Deferred
import retrofit2.Response

class RequestContainer<InputType, ResponseType>(
    var job: Deferred<Response<ResponseType>>,
    var input: InputType,
    var data: Bundle?
)