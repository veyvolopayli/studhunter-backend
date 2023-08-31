package com.studhunter.api.chat.features

import com.studhunter.api.chat.model.MessageDTO
import com.studhunter.api.chat.model.OfferRequest
import com.studhunter.api.chat.model.OfferResponse

sealed class TextFrameType(val data: Any) {
    class TMessage(data: MessageDTO) : TextFrameType(data)
    class TOfferRequest(data: OfferRequest) : TextFrameType(data)
    class TOfferResponse(data: OfferResponse) : TextFrameType(data)
    class TOther(data: Unit) : TextFrameType(data)
}
