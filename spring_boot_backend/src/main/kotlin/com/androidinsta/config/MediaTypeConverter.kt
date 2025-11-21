package com.androidinsta.config

import com.androidinsta.Model.MediaType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class MediaTypeConverter : AttributeConverter<MediaType, String> {
    
    override fun convertToDatabaseColumn(attribute: MediaType?): String? {
        return attribute?.name
    }
    
    override fun convertToEntityAttribute(dbData: String?): MediaType? {
        if (dbData == null) return null
        
        return try {
            // Convert to uppercase to handle both "image" and "IMAGE"
            MediaType.valueOf(dbData.uppercase())
        } catch (e: IllegalArgumentException) {
            // Default to IMAGE if invalid value
            MediaType.IMAGE
        }
    }
}
