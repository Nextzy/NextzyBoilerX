package com.nextzy.library.boilerx.network.core

import android.os.Bundle
import kotlinx.coroutines.Deferred
import retrofit2.Response

class RequestContainer<InputType, ResponseType>(
    var input: InputType,
    var data: Bundle?,
    var action: () -> Response<ResponseType>
)