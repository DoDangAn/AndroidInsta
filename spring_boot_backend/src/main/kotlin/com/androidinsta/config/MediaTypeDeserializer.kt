package com.androidinsta.config

import com.androidinsta.Model.MediaType
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class MediaTypeDeserializer : JsonDeserializer<MediaType>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MediaType {
        val value = p.text.uppercase()
        return try {
            MediaType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            // Default to IMAGE if invalid value
            MediaType.IMAGE
        }
    }
}
