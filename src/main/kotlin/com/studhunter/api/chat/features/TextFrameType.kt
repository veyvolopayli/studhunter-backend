package com.studhunter.api.chat.features

import com.studhunter.api.chat.model.*

sealed class TextFrameType(val data: Any) {
    class TMessage(data: MessageDTO) : TextFrameType(data)
    class TOfferRequest(data: OfferRequestDTO) : TextFrameType(data)
    class TOfferResponse(data: OfferResponseDTO) : TextFrameType(data)
    class TOther(data: Unit) : TextFrameType(data)
}
